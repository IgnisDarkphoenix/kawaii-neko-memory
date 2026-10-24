# 🔐 Firma, AAB y versionado (para publicar en Play Store)

Esta guía cubre el paso 4 del upgrade. **La parte de Gradle ya está hecha** en `android/build.gradle`;
lo único que falta son dos pasos en el workflow, que tienes que pegar tú porque el token de la
sesión no tiene permiso `workflows` sobre `.github/`.

> Trabajas desde el móvil, así que todo está pensado para hacerlo desde la app de GitHub:
> los secretos se crean en **Settings → Secrets and variables → Actions** y el resultado del
> keystore se lee en el **Summary** del run (se ve en la app).

---

## 1. Qué ya está configurado en Gradle

```gradle
// android/build.gradle
versionCode = APP_VERSION_CODE  || -PappVersionCode || gradle.properties:appVersionCode
versionName = APP_VERSION_NAME  || -PappVersionName || gradle.properties:appVersionName
signingConfig release  →  solo si existe keystore.properties (raíz del repo)
release.ndk.abiFilters = arm64-v8a, armeabi-v7a   // x86 solo en debug
bundle.language.enableSplit = false
```

- Sin `keystore.properties`, el build **no falla**: genera un AAB sin firmar y avisa con un warning.
- `keystore.properties` y `*.jks` están en `.gitignore` desde ahora.

Formato de `keystore.properties` (referencia — **nunca** lo subas):

```properties
storeFile=release.jks
storePassword=…
keyAlias=kawaiineko
keyPassword=…
```

---

## 2. Crear el keystore una sola vez (desde el móvil)

Crea el archivo `.github/workflows/generar-keystore.yml`, pégalo y ejecútalo a mano
(**Actions → Generar keystore → Run workflow**). Al terminar, el **Summary** del run muestra
el base64 que necesitas. Después **borra el archivo** (o desactívalo).

```yaml
name: Generar keystore

on:
  workflow_dispatch:

jobs:
  generar:
    runs-on: ubuntu-latest
    steps:
      - name: Crear keystore
        run: |
          PASS="$(openssl rand -base64 18)"
          keytool -genkeypair -v \
            -keystore release.jks \
            -alias kawaiineko \
            -keyalg RSA -keysize 4096 -validity 10000 \
            -storepass "$PASS" -keypass "$PASS" \
            -dname "CN=DarkphoenixTeam, OU=Games, O=DarkphoenixTeam, L=Mexicali, ST=BC, C=MX"
          B64="$(base64 -w0 release.jks)"
          {
            echo "## Keystore generado"
            echo ""
            echo "### Secrets a crear (Settings → Secrets → Actions)"
            echo ""
            echo '| Nombre | Valor |'
            echo '|---|---|'
            echo "| \`KEYSTORE_B64\` | (el bloque de abajo) |"
            echo "| \`KEYSTORE_PASSWORD\` | \`$PASS\` |"
            echo "| \`KEY_ALIAS\` | \`kawaiineko\` |"
            echo "| \`KEY_PASSWORD\` | \`$PASS\` |"
            echo ""
            echo "### KEYSTORE_B64"
            echo ""
            echo '```'
            echo "$B64"
            echo '```'
            echo ""
            echo "> ⚠️ Guarda también el .jks en un lugar seguro: es tu *upload key*."
          } >> "$GITHUB_STEP_SUMMARY"
```

> ⚠️ **Importante:** este keystore es tu *upload key* de Play. Si lo pierdes, puedes pedir
> un reset en Play Console (trámite manual), pero si lo filtras, cualquiera podría subir
> versiones de tu app. Guárdalo en un gestor de contraseñas.

---

## 3. Pegar estos dos pasos en `.github/workflows/android-build.yml`

### a) Antes del paso de build (después de `Grant execute permission`)

```yaml
    - name: Preparar firma
      if: ${{ secrets.KEYSTORE_B64 != '' }}
      env:
        KEYSTORE_B64: ${{ secrets.KEYSTORE_B64 }}
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      run: |
        echo "$KEYSTORE_B64" | base64 -d > release.jks
        cat > keystore.properties <<EOF
        storeFile=release.jks
        storePassword=$KEYSTORE_PASSWORD
        keyAlias=$KEY_ALIAS
        keyPassword=$KEY_PASSWORD
        EOF
```

> `if:` con `secrets` directo no funciona en todos los casos; si te da error de sintaxis,
> quita esa línea y deja el paso sin condición (fallará solo si los secrets no existen).

### b) Después del build de debug

```yaml
    - name: Build AAB firmado
      run: |
        ./gradlew bundleRelease --stacktrace \
          -PappVersionCode=${{ github.run_number }} \
          -PappVersionName=1.0.${{ github.run_number }}

    - name: Upload AAB
      uses: actions/upload-artifact@v4
      with:
        name: app-release-aab
        path: android/build/outputs/bundle/release/*.aab
        retention-days: 90
```

Ese `-PappVersionCode=${{ github.run_number }}` resuelve el problema de tener que subir el
`versionCode` a mano en cada entrega: Play rechaza un AAB con un `versionCode` ya usado.

---

## 4. Verificar antes de subir

En el log del run debe aparecer:

```
[firma] keystore.properties detectado: AAB firmado con alias 'kawaiineko'.
[version] versionCode=42 versionName=1.0.42
```

Si aparece `Sin keystore.properties: el AAB saldra SIN FIRMAR`, los secrets no llegaron al job.

Comprobación de la firma (opcional, en el propio CI):

```yaml
    - name: Verificar firma del AAB
      run: |
        APKSIGNER=$(find $ANDROID_HOME/build-tools -name apksigner | sort -r | head -1)
        cp android/build/outputs/bundle/release/*.aab /tmp/app.zip
        unzip -o -q /tmp/app.zip -d /tmp/aab
        echo "Firmado: $(ls /tmp/aab/META-INF/*.RSA 2>/dev/null | wc -l) bloque(s)"
```

---

## 5. Lo que Play te pedirá igualmente (no es código)

- **AAB**, no APK. ✔ (paso 3b)
- **Upload key** registrada en Play Console (se activa sola en la primera subida).
- **Icono 512×512** + *feature graphic* 1024×500 + capturas.
- **Política de privacidad** con URL pública.
- **Data safety**: declarar que se guarda progreso localmente (`SharedPreferences`), sin
  recogida de datos si aún no hay analytics ni ads.
- **Clasificación de contenido (IARC)**.
- **Prueba cerrada**: 12 testers × 14 días continuos (cuentas personales creadas después
  del 13-nov-2023). *Internal testing* no cuenta.
