# MeandMe Terminal

Terminal real tipo Termux para Android. Nombre visible: **MeandMe Terminal**, package `com.meandme.terminal`.

## Qué es
- Emulador de terminal interactivo usando las librerías open-source de Termux (`terminal-emulator` y `terminal-view` vía JitPack, versión v0.118.0).
- Lanza shell real de Android: `/system/bin/sh` (fallback `/bin/sh`).
- Permite comandos como `ls`, `pwd`, `echo`, `cd`, `cat`, `mkdir`, etc. y ver salida en pantalla.
- UI estilo terminal: fondo oscuro `#0B0F0C`, texto verde/cian, barra de teclas extra (Esc, Tab, Ctrl, Alt, flechas, Home/End, PgUp/PgDn).
- Compila a APK debug instalable.

## Limitaciones honestas
- **No es un clon completo de Termux con `pkg`/`apt`.** No incluye bootstrap Linux, proot, ni gestor de paquetes. Es shell Android base.
- Para entorno Linux completo (node, python, firebase-tools, etc.) se requeriría bootstrap/proot adicional, similar a lo que hace Termux/UserLAnd/Andronix.
- Esta app es solo la terminal; no incluye FEIKS ni firebase-tools.

## Estado de compilación en este entorno
Se preparó el proyecto completo y se instaló JDK 17 + Android SDK (platform-tools, platforms;android-34, build-tools;34.0.0) manualmente vía `curl` (porque `sdkmanager` de Java falla con el proxy de salida).

La compilación local con Gradle **no pudo completarse en esta VM** por una limitación del entorno de ejecución:
- Java abre sockets TCP directos y el sistema de permisos de Muse los bloquea con el mensaje: `muse: Other TCP connections is turned off for this assistant. To allow it, ask the user to open Muse settings -> Permissions -> Direct network protocols and switch other_tcp from Deny to Ask.`
- Por eso Gradle no puede descargar AGP 8.5.2 ni las dependencias de Termux desde Google/Maven/JitPack, y `sdkmanager` también falla con `java.util.NoSuchElementException` en `HttpURLConnection.doTunneling0` al intentar tunelizar vía proxy.
- `curl`/`wget` sí funcionan (usan el proxy permitido), pero Gradle (Java) no.

**Solución:** compilar en un entorno sin esa restricción. El proyecto ya incluye:
- Gradle Wrapper completo (`gradlew`, `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties` para Gradle 8.7)
- Workflow de GitHub Actions (`.github/workflows/build-apk.yml`) que compila el APK en la nube y lo deja como artefacto descargable.

### Opción A: GitHub Actions (recomendado, sin instalar nada)
1. Sube esta carpeta a un repo en GitHub (puedes hacerlo desde github.com > New repository > Upload files, o con `git push` desde una PC).
2. Ve a la pestaña **Actions** del repo, ejecuta el workflow **Build MeandMe Terminal APK**.
3. Descarga el artefacto **MeandMe-Terminal-APK** (contiene `app-debug.apk`).
4. Instala el APK en tu celular (permite orígenes desconocidos).

### Opción B: Android Studio / PC local
1. Abre esta carpeta en Android Studio (o VS Code con extensión Android).
2. Espera a que Gradle sincronice (descargará AGP y dependencias Termux).
3. Build > Build APK(s) o `./gradlew assembleDebug`.
4. APK en `app/build/outputs/apk/debug/app-debug.apk`.

### Opción C: Cloud Shell / otra VM sin bloqueo TCP
Si tienes una VM Linux con internet normal (sin bloqueo de TCP para Java), ejecuta:
```bash
export JAVA_HOME=/ruta/a/jdk17
export ANDROID_HOME=~/android-sdk
./gradlew assembleDebug
```

## Instalar el APK (cuando lo tengas)
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
O copia el APK al celular y ábrelo para instalar.

## Uso
Abre MeandMe Terminal, escribe `ls`, `pwd`, `echo hola`, `cd /sdcard`, etc. La barra inferior tiene Esc, Tab, Ctrl, Alt y flechas.
