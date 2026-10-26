# Economía de nekoins — diseño sin anuncios

Este documento fija los números de la economía para la **beta de 20 personas y
la publicación en Play Store sin anuncios**. Si más adelante se implementa
AdMob, los bonus por rewarded se activan solos (ver §5) sin tocar nada más.

## 1. Qué estaba mal

Había dos problemas, y el segundo era más grave que el "precios altos":

### a) El coste crecía de forma cuadrática (bug de diseño)

`SaveManager.getGachaCost()` y `getTimeAttackUpgradeCost()` recalculaban el
precio sumando **todos** los incrementos anteriores en **cada** compra:

```java
int cost = Constants.GACHA_BASE_COST;
for (int i = 0; i < pulls; i++) {        // se recorre TODAS las tiradas
    cost += (i % 2 == 0) ? 500 : 1000;   // y se suman todos los incrementos
}
```

El resultado no era una progresión, era una explosión:

| Compra | Coste con la fórmula vieja |
|---|---|
| Tirada 1 de gacha | 1.000 |
| Tirada 35 de gacha | **17.500** |
| Las 35 cartas | **477.000** |
| Mejora 48 de Time Attack | **17.950** |
| Las 48 mejoras | **444.000** |

Total para desbloquear todo el contenido: **940.000 nekoins**. La campaña
completa en 3★ paga ~67.000, así que hacían falta **unas 250 pasadas
completas al juego** (o ~5.000 partidas sueltas).

### b) La economía dependía de los anuncios, y no hay anuncios

`AdController` existe como interfaz, pero **en Android no hay ninguna
implementación** (no hay `play-services-ads`, ni `MobileAds`, ni banner, ni
rewarded). Todas las llamadas del core reciben `null` y caen al camino sin
bonus. Consecuencias:

- `TIME_ATTACK_AD_MULTIPLIER = 2.5` → quien **no** ve el anuncio cobra el
  **40 %** de lo previsto en Time Attack.
- Los precios estaban calibrados asumiendo esos multiplicadores.

Es decir: el jugador de la beta jugaba sin red y sin el bonus con el que se
habían calculado los precios.

## 2. Regla de diseño que se aplica ahora

> **Todo el contenido desbloqueable debe caber en la campaña, sin anuncios.**

Los anuncios, cuando lleguen, solo aceleran. Nunca son requisito.

## 3. Costes nuevos (lineales)

Fórmula: `coste = BASE + INCREMENT × (unidades ya compradas)`.

| Sistema | Antes | Ahora | Curva |
|---|---|---|---|
| Gacha (35 cartas) | 1.000 → 17.500, total 477.000 | **200 → 880**, total **18.900** | `200 + 20 × tiradas` |
| Time Attack (48 mejoras) | 500 → 17.950, total 444.000 | **150 → 620**, total **18.480** | `150 + 10 × mejoras` |
| Poderes (5 pistas + 5 congelamientos) | 19.000 | **3.640** | `120 / 200 / 320 / 480 / 700` por unidad |
| **Total del juego** | **940.000** | **41.020** | — |

Cuentas: 35 tiradas de gacha a `200+20k` → 18.900.
48 mejoras de TA a `150+10k` → 18.480. Poderes: `(120+200+320+480+700) × 2` →
3.640.

### Ingreso de referencia de la campaña (3★, primera pasada)

| Tras terminar | Acumulado | Ya se puede pagar |
|---|---|---|
| Fácil | ~5.050 | 1.ª tirada de gacha (200) y varios poderes |
| Normal | ~14.450 | el gacha completo (18.900 casi) |
| Avanzado | ~31.200 | **todas las cartas** y la mitad de Time Attack |
| Difícil | ~73.825 | **todo el contenido del juego** |

Cálculo: `50 × (base + 2×bonusEstrellas + bonusPrimeraVez)` por dificultad más
el valor de las cartas encontradas (2.530 pares de campaña × valor medio 2,5).

## 4. Time Attack sin anuncios

- `TIME_ATTACK_NEKOIN_PER_PAIR`: 5 → **10** (el 60 % del ingreso de TA venía
  del multiplicador de anuncio).
- `TIME_ATTACK_AD_MULTIPLIER`: 2,5 → **1,5**. El rewarded es ahora un bonus
  opcional del +50 %, no un impuesto del −60 % a quien no lo ve.
- El texto del resultado ya no lleva `(x2.5!)` fijo: muestra el valor real de
  la constante.

## 5. Cuando se implementen los anuncios

No hay que reajustar nada. Los puntos de integración ya existen:

| Punto | Dónde | Estado |
|---|---|---|
| Interfaz | `core/.../systems/AdController.java` | lista (banner, interstitial, rewarded) |
| Rewarded en Time Attack | `TimeAttackScreen.onWatchAdClicked()` | ya llama a `isRewardedLoaded()` y oculta el botón si no hay anuncio |
| Bonus ×1,5 | `Constants.TIME_ATTACK_AD_MULTIPLIER` | aplicado solo si `adWatched` |
| Interstitial por derrotas/victorias | `Constants.GAMES_BEFORE_INTERSTITIAL_LOSS/WIN` | definidos, sin usar |

Lo que falta para publicar **con** anuncios, y que no se hace ahora:

1. Implementar `AdMobController implements AdController` en `android/`.
2. Añadir `com.google.android.gms:play-services-ads` al `build.gradle`.
3. Declarar el App ID de AdMob en el manifest.
4. **Adaptarse a familias**: en Play Console hay que declarar si la app
   contiene anuncios y si va dirigida a menores. Con anuncios, el público
   infantil exige solo familias de anuncios certificadas o **quedarse sin
   anuncios**. Un juego de gatitos entra fácil en la categoría "todos los
   públicos", así que la vía sin anuncios es también la más simple de publicar.
5. Política de privacidad que cubra publicidad y consentimiento (UMP/IAB TCF
   en la UE). Sin anuncios, la política es más corta y el formulario de
   *Data safety* no declara publicidad.

**Recomendación para la beta:** publicar sin anuncios. Cero riesgo de rechazo
por familias/consentimiento, cero dependencia de red y la economía ya cuadra.
Los anuncios se pueden añadir después de la versión 1.0 sin migrar partidas.

## 6. Qué mirar en la beta

- ¿Alguien llega a la tirada de gacha 1 antes de terminar Fácil? **Debería.**
- ¿Se compran poderes con naturalidad o se acumulan sin gastar? Si se acumulan,
  el problema es que no se percibe su utilidad, no el precio.
- ¿Cuántas tiradas de gacha hace cada tester en 14 días? Referencia sana:
  **5-12**.
- Si algún tester termina la campaña completa en menos de 14 días, subir
  `GACHA_COST_INCREMENT` a 30 y `TIME_ATTACK_UPGRADE_INCREMENT` a 15.

## 7. Archivos tocados

| Archivo | Cambio |
|---|---|
| `config/Constants.java` | precios, incrementos y multiplicador de anuncio |
| `systems/SaveManager.java` | `getGachaCost()` y `getTimeAttackUpgradeCost()` lineales |
| `screens/TimeAttackScreen.java` | el resultado muestra el multiplicador real |

`POWER_PRICES` mantiene los 5 escalones: el stock máximo sigue siendo 5.
