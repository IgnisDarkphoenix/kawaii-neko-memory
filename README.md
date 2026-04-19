# 🐱 Kawaii Neko Memory

Un adorable juego de memoria con gatitos kawaii desarrollado con libGDX.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-Proprietary-red)
![Platform](https://img.shields.io/badge/platform-Android-green)

---

## 📱 Características

- 🎮 **200 niveles** distribuidos en 4 dificultades
- 🌍 **10 idiomas** soportados
- 🎨 **5 mazos desbloqueables** con gatitos únicos
- 💰 **Sistema de monedas** (Nekoins)
- ⚡ **Powers** comprables y mejorables
- 📢 **Monetización** mediante AppLovin MAX, AdMob y Unity Ads
- 🎵 **Música y efectos** generados con IA

---

## 🏗️ Tecnologías

- **Framework**: libGDX 1.12.1
- **Lenguaje**: Java 17
- **Build**: Gradle 8.4
- **CI/CD**: GitHub Actions
- **Ads**: AppLovin MAX (Mediation)
- **Target**: Android 34 (Min 21)

---

## 📦 Compilación

### Automática (GitHub Actions)
Los commits a `main` disparan compilación automática. Descarga APK en **Actions > Build > Artifacts**.

### Manual (Local)
```bash
# Clonar repositorio
git clone https://github.com/IgnisDarkphoenix/KawaiiNekoMemory.git
cd KawaiiNekoMemory

# Compilar Debug APK
./gradlew assembleDebug

# APK generado en:
# android/build/outputs/apk/debug/android-debug.apk

📁 Estructura del Proyecto
KawaiiNekoMemory/
├── android/          # Módulo Android
├── core/             # Lógica del juego (libGDX)
├── assets/           # Recursos (imágenes, audio, i18n)
├── .github/          # GitHub Actions workflows
└── gradle/           # Gradle wrapper

👥 Equipo
DarkphoenixTeam

Damacus (Eduardo) - Game Designer & Developer

📄 Licencia
Copyright © 2025 DarkphoenixTeam. Todos los derechos reservados.

Este proyecto es software propietario. No se permite la distribución, modificación o uso comercial sin autorización explícita.

📞 Contacto
Para consultas sobre el proyecto: [Pendiente sitio web DarkphoenixTeam]

Made with ❤️ and ☕ by Damacus - DarkphoenixTeam
