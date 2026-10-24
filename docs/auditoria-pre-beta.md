# 🐱 Kawaii Neko Memory — Auditoría pre-beta y pre-Play Store

**Fecha:** 24 de octubre de 2026
**Rama auditada:** `main` @ `9b0090b` (revisada en `arena/40e20a1b-kawaii-neko-memory`)
**Alcance:** revisión estática completa de `core/`, `android/`, `assets/`, CI y configuración de build.
**Objetivo:** dejar el juego listo para una **beta cerrada con 20 personas** y posterior subida a Google Play.

> ⚠️ **Nota metodológica:** el sandbox de revisión no tiene JDK, Android SDK ni acceso a repositorios Maven, así que **no pude compilar ni ejecutar el juego**. Todo lo de abajo viene de análisis estático (lectura de código, inventario de assets, validación de i18n y comparación con los requisitos actuales de Google Play). El último build de CI en `main` sí pasó (run `38978898387`), o sea que el código compila hoy.

---

## 0. Resumen ejecutivo

| Área | Estado | Comentario |
|---|---|---|
| Compilación (debug) | 🟢 OK | CI verde, APK debug se genera |
| Jugabilidad núcleo (tablero, pares, timer) | 🟢 Sólida | Lógica de `GameScreen` bien resuelta |
| Navegación / progresión | 🔴 **Rota** | Faltan assets de pestañas y flechas → **solo se puede jugar Fácil** |
| Memoria / estabilidad | 🔴 **Crítico** | Las pantallas nunca se liberan → crash por memoria en sesiones largas |
| Guardado de progreso | 🟠 Riesgo | Escritura diferida sin flush garantizado → se puede perder el último nivel |
| i18n (10 idiomas) | 🟠 Parcial | Faltan 2 claves en EN/ES; CJK muestra caracteres vacíos (94–177 glifos) |
| Assets de audio | 🟠 Incompleto | 5 de 10 pistas de campaña; 0 de 10 de Time Attack |
| Monetización (ads) | ⚪ No implementada | Solo existe la interfaz `AdController`, sin SDK ni implementación Android |
| Requisitos Play Store | 🔴 **Bloqueante** | `targetSdk 34` (hoy se exige **36**), sin AAB firmado, libGDX 1.12.1 sin soporte 16 KB |
| Tamaño (63 MB de assets) | 🟠 Mejorable | ~120 MB de RAM decodificada; paneles 1628×1628 para 408 px de pantalla |
| Tests / observabilidad | 🔴 Ausente | 0 tests, 0 crash reporting, 0 analytics → beta a ciegas |

**Conclusión:** el núcleo del juego está bien, pero **hoy la beta no serviría**: los testers solo podrían jugar la dificultad Fácil, con riesgo real de crashes por memoria y sin datos de crash para diagnosticar. Hay 6 bloqueadores (sección 1) que hay que cerrar antes de invitar a nadie. Estimación: **~3–5 días de trabajo** para los P0 y P1, más 1 día de re-validación.

---

## 1. 🔴 Bloqueadores (P0) — arreglar antes de la beta

### P0-1. Solo es jugable la dificultad "Fácil" (faltan 4 assets de pestañas)

`LevelSelectScreen.createTabs()` construye las 4 pestañas leyendo `TAB_EASY/NORMAL/ADVANCED/HARD`, pero **esos PNG no existen** en `assets/images/ui/buttons/`. Cada `new Texture(...)` lanza excepción, el `catch` la absorbe y **el botón nunca se crea**:

```java
// LevelSelectScreen.java:168-203
try {
    Texture tabTexture = new Texture(Gdx.files.internal(tabPaths[i]));
    ...
} catch (Exception e) {
    Gdx.app.error(TAG, "Error cargando tab " + i);   // ← se traga el error
}
```

Resultado: `tabButtons` queda vacío, `currentDifficulty` se queda en `EASY` y **Normal / Avanzado / Difícil son inalcanzables** para el jugador. Se pierden 150 de los 200 niveles y todo el sistema de gacha/deck pierde sentido.

**Archivos faltantes:** `tab_easy.png`, `tab_normal.png`, `tab_advanced.png`, `tab_hard.png`
**Referencias:** `config/AssetPaths.java:46-49`, `screens/LevelSelectScreen.java:160-206`

**Recomendación:** no crear 4 PNG nuevos para esto. Dibujar las pestañas con `ShapeRenderer` (ya se usa en esa pantalla) + el nombre de la dificultad con la fuente, coloreadas por dificultad (`LevelData.getColorInt()` ya existe). Elimina 4 dependencias de assets y funciona en los 10 idiomas.

---

### P0-2. Los niveles 41–50 quedan fuera de pantalla y no hay paginación

La rejilla dibuja **50 niveles en 10 filas**, pero el espacio disponible no alcanza:

| Cálculo | Valor |
|---|---|
| `GRID_MARGIN_TOP` = 130 → la rejilla empieza en y = 670 | 670 px |
| 10 filas × (60 + 8) − 8 = **672 px necesarios** | 672 px |
| Espacio hasta el footer (`FOOTER_HEIGHT` = 120) | 540 px |

→ **8 filas caben (niveles 1–40); las filas 9 y 10 (niveles 41–50) caen por debajo de y=0**, encima de los botones "Volver" y "Time Attack" (que están en y≈20). Encima, `maxPages` está fijo en `1` (línea 385) y las flechas de navegación **tampoco existen como archivo** (`btn_arrow_up/down/left/right.png`), así que la paginación es inalcanzable.

Consecuencia: los niveles de rango **MASTERY (41–50)**, la parte más interesante del diseño, **no se pueden jugar**, y los toques en la zona inferior disparan a la vez un nivel y un botón.

**Referencias:** `LevelSelectScreen.java:41-49` (constantes), `:274-284` (layout), `:385` (`maxPages = 1`), `:527-560` (hit-test), `AssetPaths.java:41-44`.

**Recomendación:** paginar de 25 en 25 (5×5), 2 páginas por dificultad, y usar `btn_arrow.png` (que sí existe, 512×512) rotado 90/180/270 para las flechas. Alternativa: rejilla 5×10 con scroll vertical por arrastre (el patrón ya está implementado en `AchievementsScreen`).

---

### P0-3. Las pantallas nunca se liberan → fuga de memoria de GPU (crash probable en beta)

En libGDX, `Game.setScreen()` llama `hide()` de la pantalla anterior **pero nunca `dispose()`**. El repo hace `game.setScreen(new XScreen(game))` en **23 lugares** sin liberar la anterior:

```java
// Patrón repetido en todas las pantallas, p. ej. HomeScreen.java:225
game.setScreen(new LevelSelectScreen(game));   // ← la HomeScreen anterior queda viva
```

Todas las pantallas sí implementan `dispose()` con su `Texture.dispose()`… pero **ese método no se llama nunca**. Y no es memoria pequeña:

| Pantalla | VRAM por visita (estimado) |
|---|---|
| `GameScreen` (fondo + 3 paneles 1628² + iconos + 15 cartas) | **≈ 45–50 MB** |
| `LevelSelectScreen` (patrón + `panel_confirm` 1628²) | ≈ 11 MB |
| `BazaarScreen` (patrón + `icon_gacha` 1024² + …) | ≈ 8 MB |
| `HomeScreen` (logo 1024² + 5 botones 512×256 + iconos) | ≈ 9 MB |

Con 20 testers jugando 20–30 niveles seguidos, esto llega a **cientos de MB de texturas huérfanas** → `OutOfMemoryError` / crash nativo en dispositivos de gama media (justo los que más abundan en una beta).

**Problema secundario en el mismo punto:** cuando sí se llame `dispose()`, **los botones no liberarán sus texturas**, porque `SimpleButton.ownsTexture` es `false` por defecto y `HomeScreen.createButton()` / `LevelSelectScreen` **crean una `Texture` nueva por botón** sin marcarla como propia. Hay que llamar `setOwnsTexture(true)` donde la pantalla crea la textura.

**Recomendación (parche pequeño y de alto impacto):** añadir a `KawaiiNekoMemory` un método

```java
public void changeScreen(Screen next) {
    Screen old = getScreen();
    setScreen(next);
    if (old != null) old.dispose();
}
```

y reemplazar los 23 `setScreen(...)` por `changeScreen(...)`. Después, decidir por pantalla qué texturas son "propias" (dispose) y cuáles vienen de `SharedAssetManager` (release). **Este es el cambio que más crashes evita en la beta.**

---

### P0-4. El progreso se puede perder (flush diferido sin cierre garantizado)

`SaveManager` acumula cambios y solo escribe a disco cada 5 operaciones:

```java
// SaveManager.java:69, 94-105
private static final int FLUSH_THRESHOLD = 5;
private void markDirty() { ... if (pendingOperations >= FLUSH_THRESHOLD) flushNow(); }
```

`flushNow()` **no se llama en ningún otro sitio** (ni en `pause()`, ni en `dispose()`, ni al terminar un nivel). Un cierre de nivel hace típicamente 4 operaciones (`setLevelCompleted`, `addNekoins`, `addPairsFound`, `updateBestCombo`) → **no llega al umbral**. Si el usuario mata la app desde el reciente o Android la destruye en segundo plano, **se pierde el último nivel, las estrellas y los nekoins**.

**Referencias:** `KawaiiNekoMemory.java:69-73` (`pause()` solo pausa música), `SaveManager.java:94-119, 505-520`.

**Recomendación:** (a) `flushNow()` en `KawaiiNekoMemory.pause()` y en `dispose()`; (b) `flushNow()` explícito al cerrar un nivel/partida; (c) bajar el patrón a "escribir todo al salir de la pantalla de resultado". Es 5 líneas y elimina la clase de bug más frustrante para un tester.

---

### P0-5. Google Play rechazaría la subida hoy: `targetSdk 34`

Desde el **31 de agosto de 2026**, Play exige **API 36 (Android 16)** para apps nuevas y actualizaciones en teléfonos/tablets. El proyecto declara:

```gradle
// android/build.gradle:8-13
compileSdk 34
targetSdk 34
```

Además, hay un segundo requisito ya en vigor que también incumple el proyecto:

- **16 KB page size**: obligatorio para apps con código nativo (libGDX) que apunten a Android 15+. **libGDX 1.12.1 (dic-2023) incluye `.so` alineados a 4 KB**; el soporte llegó en la línea 1.13.x. La fecha límite para actualizaciones se movió al **1 de febrero de 2027**, pero un upload nuevo hoy con `targetSdk 36` será marcado.

**Plan mínimo de actualización:**

| Elemento | Actual | Necesario |
|---|---|---|
| `targetSdk` / `compileSdk` | 34 | **36** |
| Android Gradle Plugin | 8.2.0 | **≥ 8.5.1** (recomendado 8.13.x) |
| Gradle wrapper | 8.4 | **8.13** (el que pida el AGP elegido) |
| JDK | 17 | 17 (OK) |
| libGDX + gdx-freetype | 1.12.1 | **1.13.5** (natives 16 KB) |
| Formato de subida | APK debug/unsigned | **AAB firmado** (`bundleRelease`) |
| ABIs empaquetadas | armeabi-v7a, arm64-v8a, x86, x86_64 | solo **arm64-v8a + armeabi-v7a** para tienda (x86 solo para emulador interno) |

**Efectos colaterales a probar tras subir a 36:**
- Android 16 **ignora la restricción de orientación en pantallas grandes** (`screenOrientation="portrait"` en el manifest) → hay que verificar tablet/foldable y que `FitViewport` no deforme.
- Edge-to-edge obligatorio desde `targetSdk 35`: comprobar que la barra de estado/navegación no tapa el HUD (`Constants.HUD_HEIGHT = 80`).

**Referencias:** `android/build.gradle`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`, `core/build.gradle`.
Fuentes: [Target API level requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en) · [16 KB page sizes](https://developer.android.com/guide/practices/page-sizes)

---

### P0-6. Sin observabilidad: la beta sería a ciegas

No hay **crash reporting** (Crashlytics/Sentry), ni analítica de eventos, ni forma de que un tester envíe feedback desde el juego. Con 20 personas jugando 14 días, sin esto solo te enterarás de los bugs que te cuenten por WhatsApp, y los crashes (que van a existir por P0-3) serán invisibles.

**Recomendación:** Firebase Crashlytics (o Sentry, más simple de integrar en libGDX sin Google Services) + 8–10 eventos clave: `level_start`, `level_win` (con `level_id`, `stars`, `time_left`, `mistakes`), `level_lose`, `power_used`, `gacha_pull`, `deck_edit`, `lang_changed`, `screen_enter`. Con eso la sección 7 (balance) deja de ser opinión.

---

## 2. 🟠 Problemas altos (P1) — arreglar durante la beta temprana

### P1-1. Falta de música en la mitad de las partidas y en todo Time Attack

```java
// GameScreen.java:493-498
int trackIndex = MathUtils.random(0, Constants.GAME_MUSIC_TRACKS - 1); // 0..9
String musicPath = AssetPaths.getGameMusicPath(trackIndex);            // game_track_01..10
```

En disco solo hay `game_track_01..05.ogg` → **50 % de las partidas arrancan sin música** (la excepción se captura y solo se registra en log). Y `getTimeAttackMusicPath()` apunta a `timeattack_track_01..10.ogg`, de los que **no existe ninguno** → Time Attack siempre cae al fallback, con otro 50 % de fallo.

**Solución rápida:** que el índice se elija de una lista de pistas realmente presentes (`Gdx.files.internal(path).exists()` o un array explícito), y que `Constants.GAME_MUSIC_TRACKS` / `TIME_ATTACK_MUSIC_TRACKS` reflejen la realidad. **Solución completa:** subir las 20 pistas o reutilizar las 5 de campaña también en Time Attack.

**Referencias:** `Constants.java:168-169`, `AssetPaths.java:96-116`, `GameScreen.java:493-499`, `TimeAttackScreen.java:407-435`.

### P1-2. i18n: dos claves rotas justo en inglés y español

El código usa `game.pause` y `game.elapsed`, pero **`strings.properties` y `strings_es.properties` definen `game.paused` y `game.totaltime`**. Los otros 8 idiomas sí tienen las claves correctas. Resultado: en inglés y español, el título de la pausa y el tiempo total del resultado se ven como **`[game.pause]`** y **`[game.elapsed]`**.

**Referencias:** `GameScreen.java:1130` (`locale.get("game.pause")`), `GameScreen.java:1182` y `:1266` (`locale.format("game.elapsed", …)`).
**Fix:** añadir `game.pause=Paused` / `game.elapsed=Time: {0}` a `strings.properties` y `game.pause=Pausa` / `game.elapsed=Tiempo: {0}` a `strings_es.properties` (o renombrar las claves del código).

### P1-3. CJK con caracteres vacíos (chino, japonés, coreano)

`FontManager` genera las fuentes con un **charset fijo** (`CJK_CHARS`) que **no cubre los textos reales**:

| Idioma | Caracteres no latinos usados en `strings_*.properties` | **Faltan en la fuente** |
|---|---|---|
| `zh` | 208 | **177** |
| `ko` | 194 | **170** |
| `ja` | 220 | **94** |
| latinos / ruso | 1–59 | 0 ✅ |

Los caracteres faltan **solo en el charset generado**, no en el OTF (NotoSansCJK los tiene). Es decir: el chino, japonés y coreano se van a ver llenos de huecos.

**Fix recomendado:** construir el charset a partir del bundle cargado (`bundle.getProperties()` → `characters` + `DEFAULT_CHARS`), cachear el resultado por idioma, y regenerar solo al cambiar de idioma. Elimina el problema para siempre y quita las listas hardcodeadas.

**Referencias:** `FontManager.java:36-78` (`LATIN_CHARS` / `CJK_CHARS`), `FontManager.java:150-175` (`generateFonts`).

### P1-4. Glifos que no existen en ninguna fuente

- `❄` en el HUD cuando el tiempo está congelado (`GameScreen.drawHUD`) → **no está en `LATIN_CHARS` ni en `CJK_CHARS`**.
- `🏆` en el botón de estadísticas de la home (`HomeScreen.createRankingsButton`) → tampoco.
- `★ ☆` sí están contemplados ✅.

Se verán como cuadros vacíos. Cambiar por texto ("FREEZE", "RECORDS") o añadir los glifos al charset.

### P1-5. El logro "Estrella Naciente" es imposible de desbloquear

```java
// SaveManager.java:527-529
if (stars == 3) checkAchievement(Achievement.FIRST_3_STAR);
```

Pero `checkAchievement()` solo implementa `RICH_NEKO`, `BIG_SPENDER`, `PERSISTENT` y `GALLERY_UNLOCK`; para cualquier otro entra en `default: break;` y **nunca desbloquea**. Debería ser `unlockAchievement(...)` directo (como se hace con `FIRST_WIN` o `COMBO_MASTER`).

**Bonus relacionado:** `unlockAchievement()` suma los nekoins de la recompensa pero **no incrementa `KEY_STAT_TOTAL_EARNED`**, así que esos nekoins no cuentan para el logro "Hucha Llena" (`RICH_NEKO`) ni para las estadísticas de "ganado". Inconsistencia menor pero visible.

### P1-6. El botón "Atrás" de Android cierra el juego a mitad de partida

No hay `InputProcessor` ni `onBackPressed` personalizado. En plena partida, el gesto/botón atrás **finaliza la Activity** → se pierde la partida (y por P0-4, a veces el progreso). En un juego de memoria cronometrado es especialmente molesto.

**Fix:** `Gdx.input.setCatchKey(Input.Keys.BACK, true)` + handler que pause el `GameScreen` o vuelva a la pantalla anterior; y en el menú principal, un diálogo de confirmación para salir.

### P1-7. Pausar la app no pausa la partida (exploit + bug de audio)

`KawaiiNekoMemory.pause()` solo pausa la música. El `GameScreen` **sigue en estado `PLAYING`**, así que al volver de segundo plano el temporizador continúa donde estaba: el jugador puede "congelar" el juego para memorizar el tablero y volver. En un modo con récord (Time Attack) es un agujero directo.

Además `resume()` llama `AudioManager.resumeMusic()` incluso si la partida estaba pausada a propósito desde el botón de pausa → la música vuelve mientras el panel de pausa está abierto.

**Fix:** propagar `pause()/resume()` a la pantalla actual y que `GameScreen` pase a `PAUSED` cuando la app pierde el foco.

### P1-8. Bloqueo de progresión en la economía (nekoins)

Los números actuales hacen que el contenido medio sea inalcanzable en una sesión razonable:

| Concepto | Valor actual | Coste en "niveles fáciles" |
|---|---|---|
| Nekoins iniciales | 100 | — |
| Recompensa nivel Fácil (1ª vez, 3★) | 10 + 20 + 50 = 80 + bonus de mazo ≈ **87** | 1 nivel |
| **1 tirada de gacha** | **1 000** (luego +500/+1000 alternos) | **~12–20 niveles** |
| Gacha para pasar de 7 → 15 cartas (8 tiradas) | **28 000 nekoins** | **~400–500 niveles** ❌ |
| 1 hint | 500 | ~6–7 niveles |
| 1 time freeze | 1 000 | ~12–20 niveles |
| Mejora de tiempo Time Attack | 500 → … | ~6–7 niveles |

Y hay un segundo muro de UX: **desbloquear una carta en el gacha no la mete en el mazo**. `LevelSelectScreen` bloquea la dificultad por `getActiveCardCount()` (cartas *activas*), así que tras gastar 1 000 nekoins el jugador sigue viendo Normal bloqueado hasta que entra manualmente al Editor de Mazo y la añade. Un tester va a leer eso como un bug.

**Sugerencias para la beta:** bajar gacha a ~250–400 con incrementos suaves, potencias a 100–250, **añadir la carta desbloqueada al primer hueco libre del mazo automáticamente** y mostrar un toast "Nueva carta añadida a tu mazo". O bien dar una carta gratis cada 10 niveles completados. Y validar el balance con telemetría real (P0-6), no con intuición.

---

## 3. 🟡 Medios (P2) — antes de publicar, no imprescindibles para la beta

| # | Hallazgo | Detalle / referencia |
|---|---|---|
| P2-1 | **Assets de UI sobredimensionados** | 5 paneles a **1628×1628** (10,1 MB de RAM cada uno) que se dibujan a ~408×490 px. Redimensionar a 512×512 baja **50 MB → 5 MB** de VRAM y ~9 MB → ~1 MB de APK. Igual con `icon_gacha`/`icon_hint_hero`/`icon_timefreeze_hero` (1024²) y `slider_knob` (814²). |
| P2-2 | **Fuente CJK de 16 MB duplicada** | `assets/fonts/NotoSansCJKjp-Regular.otf` (16 MB) + copia idéntica en `android/assets/fonts/` (carpeta muerta, ver P2-3). Subsetear a los ~1 500 glifos que usa el juego la deja en ~2 MB. |
| P2-3 | **Carpeta `android/assets/` es código muerto** | 17 MB en el repo (fuente duplicada + solo 2 de 10 archivos i18n). `sourceSets` apunta a `../assets`, así que **no se empaqueta** pero confunde y pesa en el clon. Borrar. |
| P2-4 | **APK/AAB de ~70 MB** | 63 MB de assets (PNG/OGG ya comprimidos) + ~10 MB de nativas × 4 ABIs. Convertir PNG→WebP y limitar ABIs deja el AAB en ~20–25 MB de descarga. |
| P2-5 | **Música a bitrate alto** | 7 pistas = 27,2 MB, medidas a **183–220 kbps** (1,6–3,7 min cada una). A ~120 kbps OGG quedarían en ~16 MB (−11 MB) sin pérdida perceptible en móvil |
| P2-6 | **`timeattack_track_*` inexistentes** | Ya cubierto en P1-1; aquí el punto es que `AssetPaths.getTimeAttackMusicPath()` es código muerto hasta que existan los archivos. |
| P2-7 | **CI no produce entregables de tienda** | `android-build.yml` compila solo `assembleDebug` (APK) y, en tags, `assembleRelease` **sin firmar**. Para Play se necesita `bundleRelease` + keystore en secrets + `versionCode` derivado. |
| P2-8 | **`cleanup_webp.yml` hace push automático a `main`** | Un workflow manual que borra `*.webp` y commitea/pushea solo. Riesgo de borrar assets por accidente. Borrarlo (o pasarlo a PR). |
| P2-9 | **Sin tests** | `core` es un módulo Java puro → JUnit es gratis. Fácil y valioso: `LevelData` (tiempo/estrellas/recompensas para los 200 niveles), `SaveManager` (unlock, gacha, economía), `AssetPaths` (todas las rutas existen), i18n (mismas claves en los 10 idiomas). 15–20 tests detectan regresiones de balance. |
| P2-10 | **README desactualizado** | Dice `minSdk 21` (es 23), anuncia ads de AppLovin/AdMob/Unity (no hay ni una dependencia), 10 idiomas (CJK roto) y 200 niveles (150 inaccesibles hoy). Actualizar antes de compartir el repo con testers. |
| P2-11 | **`minifyEnabled false` + ProGuard gigante** | `proguard-rules.pro` tiene reglas para 3 SDKs de ads inexistentes. Cuando se active R8, revisar; hoy el archivo da una falsa sensación de configuración. |
| P2-12 | **`SharedAssetManager` usa refcounts pero solo 2 pantallas** | `GameScreen`/`TimeAttackScreen` cargan sus propias texturas duplicando lo que ya está en caché (`card_back`, iconos del HUD, patrones). Migrarlas al `SharedAssetManager` ahorra RAM y elimina el riesgo de doble `dispose`. |
| P2-13 | **`String.format` con locale del dispositivo** | `GameScreen.formatTime()` usa el locale por defecto; en un dispositivo en árabe los dígitos pueden salir árabes. Usar `Locale.US` para HUD/timers. |
| P2-14 | **`android:allowBackup="true"`** | Las SharedPreferences se respaldan en la nube; para un juego con progreso, decidir si se quiere (hoy no hay `dataExtractionRules`). |

---

## 4. 🟢 Bajos / cosméticos (P3)

- Título del nivel centrado en el HUD y temporizador a la derecha: en algún idioma (p. ej. "Avanzado 50" en alemán/ruso) pueden solaparse. Truncar o bajar tamaño.
- `GameScreen.loadDeckTextures()` tiene una lógica de relleno incorrecta (`validCardIds.get(validCardIds.size % validCardIds.size)` siempre devuelve el índice 0). Hoy **no se alcanza** porque las dificultades exigen exactamente el nº de cartas necesarias, pero es una bomba de relojería si cambian los requisitos.
- `GameState.SHUFFLING` se resuelve en el mismo frame (`updateShuffling` pone `PLAYING` de inmediato) → el `Constants.SHUFFLE_DURATION = 0.5f` no se usa y el shuffle es un "teletransporte" sin animación. Funciona, pero se siente brusco.
- `useHint()` añade una carta aleatoria extra además del par revelado; comportamiento raro para el jugador (revisar si es intencional).
- El splash no se libera ni se hace `dispose()` al pasar a Home (fuga menor, cubierta por P0-3).
- `Theme.NoTitleBar.Fullscreen` (deprecado en API 30+) → valorar `Theme.Material.NoActionBar.Fullscreen`.
- Falta `LICENSE` aunque el README declara software propietario.

---

## 5. 📱 Checklist de publicación en Google Play (estado real)

| Requisito | Estado | Acción |
|---|---|---|
| `targetSdk` 36 (API 36) desde 31-ago-2026 | 🔴 34 | P0-5 |
| `compileSdk` 36 + AGP ≥ 8.5.1 | 🔴 | P0-5 |
| Soporte 16 KB page size (nativo) | 🔴 libGDX 1.12.1 | P0-5 → libGDX 1.13.5 |
| **AAB firmado** (no APK) | 🔴 solo debug/unsigned | keystore + `bundleRelease` + secrets CI |
| App signing de Play (upload key) | 🔴 | configurar en Play Console |
| 64-bit (arm64-v8a) | 🟢 presente | quitar x86/x86_64 del bundle de tienda |
| `versionCode`/`versionName` únicos | 🟠 fijos (`1` / `1.0.0`) | automatizar por tag |
| Icono 512×512 + feature graphic + capturas | 🔴 no están en el repo | preparar assets de tienda |
| Política de privacidad (URL pública) | 🔴 | necesaria (SharedPreferences + futuros ads) |
| Formulario **Data safety** | 🔴 | declarar datos guardados localmente |
| Declaración de **anuncios** | ⚪ | si no hay ads → "No contiene anuncios"; el README dice lo contrario |
| **Clasificación de contenido** (IARC) | 🔴 | cuestionario en Play Console |
| Público objetivo / "Diseñado para familias" | 🔴 | ⚠️ con estética kawaii puede atraer niños: si se declara para familias, cambian las reglas de ads/privacidad |
| Cuenta de desarrollador verificada | ⚪ | comprobar estado |
| **Prueba cerrada: 12 testers × 14 días** (cuentas personales creadas después del 13-nov-2023) | ⚪ no iniciada | 20 testers está bien (da margen a bajas), pero el mínimo son **12 activos y opt-in continuo**; *internal testing* no cuenta |
| Acuerdo de licencia / copyright | 🟠 | añadir `LICENSE` |

Fuentes: [Target API 36](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en) · [16 KB](https://developer.android.com/guide/practices/page-sizes) · [12 testers / 14 días](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)

> ⚠️ **Sobre el plan de 20 testers:** el requisito bajó de 20 a **12** en diciembre de 2024, con **14 días continuos** de opt-in y evidencia de uso real. 20 sigue siendo buena idea (margen ante bajas), pero desde 2026 Google revisa **engagement**: instalaciones sin uso real no cuentan. Conviene un guion de pruebas (sección 7) precisamente para eso.

---

## 6. 📦 Inventario de assets (referenciados vs. en disco)

**Faltan 8 archivos referenciados en código:**

| Archivo | Referencia | Impacto |
|---|---|---|
| `images/ui/buttons/tab_easy.png` | `AssetPaths:46` | 🔴 P0-1 |
| `images/ui/buttons/tab_normal.png` | `AssetPaths:47` | 🔴 P0-1 |
| `images/ui/buttons/tab_advanced.png` | `AssetPaths:48` | 🔴 P0-1 |
| `images/ui/buttons/tab_hard.png` | `AssetPaths:49` | 🔴 P0-1 |
| `images/ui/buttons/btn_arrow_up.png` | `AssetPaths:41` | 🔴 P0-2 + scroll en Logros |
| `images/ui/buttons/btn_arrow_down.png` | `AssetPaths:42` | 🔴 P0-2 + scroll en Logros |
| `images/ui/buttons/btn_arrow_left.png` | `AssetPaths:43` | ⚪ no usado en código |
| `images/ui/buttons/btn_arrow_right.png` | `AssetPaths:44` | ⚪ no usado en código |

**Faltan de audio:** `game_track_06..10.ogg` (5) y `timeattack_track_01..10.ogg` (10) → P1-1.

**Existe pero no se usa:** `images/ui/buttons/btn_arrow.png` (512×512) → sirve para generar las 4 flechas por rotación.

### Tamaño y memoria

```
assets/            63 MB   (audio 28 · images 19 · fonts 16 · i18n 0,1)
RAM si se decodifica todo:  119,7 MB en RGBA   ← solo los 5 paneles son 50,6 MB
```

Prioridad de optimización: **paneles (50 MB de VRAM) → fuente CJK (16 MB de APK) → música (28 MB de APK)**. Con eso el AAB baja de ~70 MB a ~20–25 MB.

---

## 7. 🧪 Plan de beta con 20 personas (14 días)

### 7.1 Preparación (antes de invitar a nadie)

1. Cerrar **P0-1 … P0-6**. Sin esto la beta produce feedback inútil ("no puedo cambiar de dificultad", "se cierra solo").
2. Compilar un **AAB firmado** y subirlo a *Closed testing* (no *internal*: no cuenta para el requisito).
3. Integrar crash reporting + eventos (P0-6) **antes** del primer build que reciben los testers.
4. Crear un **canal de feedback**: formulario corto (Google Forms, 6 preguntas) + un grupo de WhatsApp/Discord. Pedir siempre: modelo de dispositivo, versión de Android, pantalla donde ocurrió.
5. Construir la matriz de dispositivos (abajo) y asignar testers.

### 7.2 Matriz mínima de dispositivos

| Gama | Ejemplos | Cantidad | Qué busca |
|---|---|---|---|
| Gama baja (2–3 GB RAM, Android 8–11) | Moto E/G, Redmi A | 4 | **Crashes por memoria (P0-3)**, rendimiento |
| Gama media (4 GB, Android 12–14) | Redmi Note, Galaxy A | 6 | Caso típico |
| Gama alta (Android 15/16) | Pixel 8/9, Galaxy S | 3 | `targetSdk 36`, edge-to-edge |
| Tableta / foldable (≥600 dp) | Galaxy Tab, Pixel Fold | 2 | Orientación ignorada en API 36 |
| Con notch / pantalla 20:9 | cualquiera moderno | 5 | HUD y `FitViewport` |
| Idioma | ES, EN, PT, ZH/JA/KO (si se arregla P1-3) | — | i18n |

### 7.3 Guion de sesión (30–40 min, en este orden)

1. Instalación limpia → splash → Home. **¿Se ve todo? ¿Hay cuadros vacíos (`❓`)?**
2. Ajustes: mover los dos sliders, cambiar de idioma 3 veces y volver a Home (**verifica que no se rompan textos ni fuentes**).
3. **Niveles Fácil 1–5** con el cronómetro del tester: ¿tiempo suficiente? ¿estrellas justas?
4. Ir a **Niveles → pestaña Normal / Avanzada / Difícil** (esto es P0-1).
5. **Bajar hasta el nivel 50** de una dificultad (esto es P0-2) y jugar **41–45** (shuffle/maestría).
6. Editor de mazo: quitar y poner cartas, intentar dejar el mazo vacío, salir y entrar.
7. Bazar: comprar un hint y un time freeze, 1 tirada de gacha (**anotar el coste y cuánto costó conseguir el dinero**).
8. Time Attack 12 y 30 (**¿hay música? ¿se guarda el récord?**).
9. Partida + **botón atrás** de Android (P1-6). Partida + **inicio → volver a la app** a los 10 s (P1-7).
10. Pausar y reanudar, ganar, perder, reintentar, siguiente nivel.
11. **Matar la app desde el reciente justo después de ganar** y reabrir → ¿se guardó el nivel? (P0-4).
12. Sesión maratón: 20–25 niveles seguidos sin cerrar la app → vigilar memoria/crash (P0-3).
13. Rotar/desbloquear, notificaciones, auriculares, llamada entrante, modo silencio.

### 7.4 Métricas que hay que poder leer

- Crashes por sesión y **dispositivo/modelo** (top 5).
- % de sesiones que llegan a la dificultad Normal; % que llega al nivel 25+.
- Tiempo medio y **tasa de éxito por nivel** (detectar niveles imposibles).
- Nekoins ganados vs. gastados por sesión; nº de tiradas de gacha necesarias para desbloquear Normal.
- Abandono por pantalla.

### 7.5 Criterios de salida de la beta (para pedir *production access*)

- 0 crashes bloqueantes durante 14 días; crash-free sessions ≥ 99 %.
- ≥ 12 testers con opt-in **continuo** y al menos una sesión registrada cada 48 h.
- Retención D7 ≥ 25 % en el cohorte.
- ≥ 30 respuestas de formulario con feedback cualitativo.
- Todos los P0 y P1 cerrados; P2 con plan declarado.

---

## 8. 🗺️ Orden de trabajo sugerido

| Sprint | Contenido | Resultado |
|---|---|---|
| **A (1–2 días)** | P0-1 (pestañas procedurales), P0-2 (paginación + flechas con `btn_arrow.png`), P1-2 (2 claves i18n), P1-5 (logro roto), P1-6/P1-7 (atrás + pausa real) | El juego se puede **jugar entero** y probar en mano |
| **B (1–2 días)** | P0-3 (`changeScreen` + ownership de texturas), P0-4 (`flushNow`), P1-1 (música robusta) | Beta estable, sin crashes por memoria ni pérdida de progreso |
| **C (1 día)** | P0-6 (Crashlytics/Sentry + eventos) + build firmado + pista de *closed testing* | Beta lista para 20 personas |
| **D (en paralelo / antes de publicar)** | P0-5 (API 36 + AGP + Gradle + libGDX 1.13.5 + AAB), P1-3 (charset dinámico), P2-1..5 (optimización de tamaño), P2-9 (tests) | Cumple Play Store |
| **E (post-beta)** | P1-8 (balance con datos reales), P2-12 (unificar assets), README/tienda | Publicación |

---

## 9. ✅ Cómo verifiqué (reproducible)

```bash
# 1) Rutas de assets referenciadas vs. existentes
grep -rhoE '"(images|audio|fonts|i18n)/[^"]+"' core/src/main/java | sort -u \
  | while read p; do p=${p//\"/}; [ -e "assets/$p" ] || echo "FALTA $p"; done

# 2) Claves i18n usadas en código vs. definidas
grep -rhoE 'locale(Manager)?\.(get|format)\("[^"]+"' core/src/main/java \
  | sed -E 's/.*\("//' | sort -u > /tmp/used.txt

# 3) Claves presentes por idioma
for f in assets/i18n/strings*.properties; do echo "$f: $(grep -c '=' $f)"; done

# 4) RAM decodificada de los PNG (WxHx4)
#    → ejecutado: 119,7 MB total; paneles 5 × 10,1 MB

# 5) ¿Se llama dispose() al cambiar de pantalla?
grep -rn "game.setScreen" core/src/main/java | wc -l   # → 23, ninguno con dispose
```

---

## 10. Anexo: assets faltantes (para el diseñador)

| # | Ruta esperada | Alternativa sin arte nuevo |
|---|---|---|
| 1–4 | `images/ui/buttons/tab_{easy,normal,advanced,hard}.png` | Pestañas con `ShapeRenderer` + color por dificultad |
| 5–6 | `images/ui/buttons/btn_arrow_{up,down}.png` | Rotar `btn_arrow.png` (ya existe) |
| 7–8 | `images/ui/buttons/btn_arrow_{left,right}.png` | Idem (hoy sin uso en código) |
| 9–13 | `audio/music/game_track_06..10.ogg` | Limitar el índice aleatorio a las 5 existentes |
| 14–23 | `audio/music/timeattack_track_01..10.ogg` | Reutilizar `game_track_01..05` en Time Attack |
| 24 | Fuente CJK subseteada | `pyftsubset` a los glifos del juego (~2 MB) |

---

*Informe generado por revisión estática del repositorio. Todos los números de línea corresponden al commit `9b0090b`.*


---

## 11. Estado tras los arreglos (rama `arena/40e20a1b-kawaii-neko-memory`)

Aplicado y **verificado con el build real de GitHub Actions** (PR #1, `Android Build` ✅):

| Ítem | Estado | Nota |
|---|---|---|
| P0-1 Pestañas / dificultades | ✅ Arreglado | Pestañas dibujadas con `ShapeRenderer`; ya no dependen de PNG |
| P0-2 Paginación niveles 41–50 | ✅ Arreglado | 25 por página (5×5), flechas dibujadas, indicador `1/2` |
| P0-3 Fuga de memoria al cambiar pantalla | ✅ Arreglado | `changeScreen()` diferido con `postRunnable` + guard anti-doble-transición |
| P0-4 Guardado (bug del playtest) | ✅ Arreglado | `flushNow()` en compras, fin de nivel, pausa y cierre |
| P1-1 Música (50 % sin pista) | ✅ Arreglado | `GAME_MUSIC_TRACKS = 5` |
| P1-2 Claves i18n EN/ES | ✅ Arreglado | `game.pause`, `game.elapsed` añadidas |
| P1-3 Glifos CJK | ✅ Arreglado | Charset generado desde los `.properties` reales |
| P1-4 Glifos inexistentes | ✅ Arreglado | `❄` y `🏆` sustituidos |
| P1-5 Logro `FIRST_3_STAR` | ✅ Arreglado | `unlockAchievement()` directo |
| P1-6 Botón atrás | ✅ Arreglado | Handler por pantalla |
| P1-7 Pausa en background | ✅ Arreglado | + fix de soft-lock con dos cartas reveladas |
| P0-5 API 36 / 16 KB / AAB | ⏳ Pendiente | Requiere subir AGP, Gradle y libGDX 1.13.5 + firma |
| P0-6 Crash reporting | ⏳ Pendiente | Necesita cuenta/SDK (decisión del autor) |
| P1-8 Balance económico | ⏳ Pendiente | Recomendado medir con telemetría antes de tocar números |
| P2-x Optimización de tamaño | ⏳ Pendiente | Paneles 1628² y fuente de 16 MB son el mayor ahorro |

### Qué probar en tu A54 5G (5 minutos)

1. Abrir Niveles → tocar **Normal / Avanzado / Difícil** (antes no existían).
2. En una dificultad, tocar la **flecha inferior** → página 2 con los niveles 26–50.
3. Ganar un nivel, **matar la app desde el reciente** y reabrir → el nivel y las estrellas deben seguir ahí.
4. Entrar y salir de 10 pantallas seguidas → vigilar que no se cierre por memoria.
5. Poner la app en segundo plano en mitad de una partida → al volver debe estar en pausa.
6. Pulsar **atrás** en mitad de una partida → debe pausar, no cerrar.
7. Pausa en inglés y en español → debe decir "Paused"/"Pausa" (no `[game.pause]`).
