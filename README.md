# Reta2 — Aplicación Android

Paul Mateo Contreras Arias — 01220371027

Aplicación educativa Android, en Kotlin con Jetpack Compose, para preparar las
competencias genéricas del examen **Saber Pro** mediante micro-aprendizaje:
sesiones de cinco preguntas que caben en cualquier rato muerto. Complementa el
programa **Ruta del Mejor Saber** de la Universidad de Santander.

**Panel de gestión y API:** [PaulGODBv/panel-gestion](https://github.com/PaulGODBv/panel-gestion)

**Mínimo:** Android 7.0 (API 24) · **Objetivo:** API 34

---

## Qué hace

- **Cinco competencias**: Lectura Crítica, Razonamiento Cuantitativo,
  Competencias Ciudadanas, Inglés y Comunicación Escrita.
- **Dos modos por nivel.** *Práctica* corrige en el momento, explica el porqué y
  no cuenta para el progreso; *evaluación* es la que desbloquea el nivel
  siguiente, con el umbral del 70 %.
- **Tres formatos de práctica**: opción múltiple, unir parejas y completar
  arrastrando palabras. El formato lo decide el nivel, no la pregunta: el mismo
  banco se presenta de las tres maneras sin duplicar un solo dato.
- **Contrarreloj** sobre las preguntas que ya existen.
- **Racha diaria**, que cuenta días en que se respondió algo y no aciertos:
  fallar no rompe nada. **Se deriva de los intentos, no se guarda en un
  contador**, así que se puede recalcular y auditar — y un día sin responder
  nada no cuenta, aunque se haya abierto la app.
- **Funciona sin conexión.** La red solo hace falta para traer contenido nuevo
  y enviar los reportes.

## De dónde salen las preguntas

**Del panel, no del código.** `CompetencyData` sigue existiendo, pero solo como
*contenido de arranque*: lo que la app trae de fábrica para poder practicar
desde el minuto cero, antes de la primera sincronización. A partir de ahí la
fuente de verdad es el panel.

Desde octubre de 2026 también el **catálogo** se descubre: la app pide
`GET /api/competences/` y de ahí saca qué competencias y niveles existen, en vez
de llevarlos escritos en una constante. Dar de alta un nivel en el panel ya no
exige publicar una versión de la app.

Cada nivel guarda el id que tiene en el panel (`levels.django_id`), que es la
identidad estable entre los dos lados. Derivarlo de su posición habría hecho que
reordenar los niveles en el panel renumerara a todo el mundo y dejara los
intentos ya registrados apuntando a otro sitio.

## Estructura

```
app/src/main/java/com/universidad/reta2/
├── data/
│   ├── local/          entidades Room, DAOs, mappers y migraciones
│   ├── remote/         Retrofit, DTO y ApiConfig
│   ├── repositories/   implementaciones; ContentSyncRepository baja el banco
│   ├── source/         CompetencyData: contenido de arranque
│   └── preferences/    SessionManager (SharedPreferences)
├── domain/
│   ├── models/         User, Question, Competence, Level, UserStats…
│   ├── repositories/   interfaces
│   ├── usecases/       GetQuestionsUseCase, UpdateProgressUseCase…
│   └── LevelRules.kt   umbral de aprobación, en un solo sitio
├── ui/
│   ├── screens/        una carpeta por pantalla, con su ViewModel
│   ├── components/     piezas compartidas
│   ├── navigation/     NavGraph, Screen y Transiciones
│   └── theme/          color, tipografía y Veredicto
├── di/                 módulos Hilt
└── utils/              PasswordHasher, Sonidos, Musica, Vibracion
```

## Base de datos (Room)

**Versión actual: 20.** Entidades: `UserEntity`, `UserStatsEntity`,
`CompetenceEntity`, `LevelEntity`, `QuestionEntity`, `QuestionOptionEntity`,
`QuestionAttemptEntity`, `LevelProgressEntity`.

Las migraciones son **explícitas y no destructivas**. El constructor usa
`fallbackToDestructiveMigrationOnDowngrade()`: si falta una migración al subir
de versión, Room lanza una excepción y la base queda intacta — revienta en
desarrollo, que es donde se arregla, en vez de borrar en silencio la cuenta y la
racha de un estudiante. Ya pasó una vez, en la v9, cuando `password` pasó a
`password_hash`.

El esquema de cada versión se exporta a `app/schemas/` y va a git, para que un
cambio de la base se vea en el diff.

## Seguridad de credenciales

Las contraseñas no se guardan ni se comparan en texto plano.
`utils/PasswordHasher.kt` genera un salt aleatorio de 16 bytes por contraseña y
persiste el resultado como:

```
sha256$<salt en hexadecimal>$<hash en hexadecimal>
```

| Flujo | Comportamiento |
| --- | --- |
| Registro | `RegistrationViewModel` hashea antes de construir el `User` |
| Inicio de sesión | `LoginViewModel` compara con `verifyPassword`, no con `==` |
| Cambio de contraseña | Exige la actual, la verifica y solo entonces guarda la nueva |
| Sincronización | `SessionManager` no guarda credenciales y los DTO no envían la contraseña |

El salt por usuario evita tablas precalculadas y que dos cuentas con la misma
clave compartan hash. SHA-256 sigue siendo una función rápida: para un
despliegue real conviene migrar a PBKDF2 o Argon2, cambio acotado a la función
privada `computeHash()`.

### La clave de la API

No está en el código. Se lee de `local.properties`, que no se versiona, y entra
por `buildConfigField`:

```properties
RETA2_API_KEY=la-clave-que-comparte-con-el-panel
```

Sin ella la compilación funciona pero la API rechaza todas las peticiones. Una
clave anterior quedó expuesta en el historial de git y fue rotada.

### La URL del panel

Tampoco está en el código, y depende de la compilación:

- **`release`** habla por **HTTPS** con el panel desplegado. La URL está en
  `app/build.gradle.kts` y no sale de `local.properties` a propósito: un APK que
  se reparte no puede depender de un fichero que vive en una sola máquina.
- **`debug`** habla con lo que diga `RETA2_BASE_URL`, y con producción si no se
  pone nada.

```properties
RETA2_BASE_URL=http://192.168.1.10:8000/api/
```

Tiene que terminar en barra —Retrofit lo exige— y la compilación falla con ese
mensaje si falta, en vez de romper al construir el cliente.

Hasta el 14/10/2026 esto era una constante en `ApiConfig.kt` con la IP de la LAN
y cinco IPs más comentadas debajo, una por red. Cambiar de sitio significaba
editar código versionado, así que las IPs acabaron en el repositorio.

### El tráfico en claro, solo en desarrollo

`usesCleartextTraffic` vive en `app/src/debug/AndroidManifest.xml`, que solo se
fusiona en las compilaciones de desarrollo: ahí hace falta para hablar con el
`runserver` del panel, que sirve por HTTP y no tiene certificado. **El APK de
`release` queda sin ese permiso y Android le rechaza cualquier HTTP.**

Estuvo en el manifiesto principal hasta el 14/10/2026, y mientras estuvo valía
para cualquier APK: la cabecera `X-API-Key` viajaba legible por la red y se podía
capturar con un analizador sin descompilar nada. Rotar la clave no lo arreglaba,
porque el problema no era la clave sino el transporte.

## Decisiones que conviene conocer antes de tocar el código

**La app juega siempre contra Room.** La red solo actualiza Room; ninguna
pantalla espera a una respuesta HTTP. Es lo que sostiene el modo sin conexión.

**Nada se borra, se desactiva.** Una pregunta que el panel retira se marca
inactiva y un nivel que retira se marca no publicado. El progreso del estudiante
apunta a sus ids, y borrar la fila dejaría intentos huérfanos.

**La música suena en el menú y nunca dentro de un nivel.** Los pasajes de
Lectura Crítica llegan a 2 100 caracteres, y una melodía reconocible compite con
la comprensión lectora justo cuando es lo único que se está midiendo.

**El veredicto no depende solo del color.** Acertar y fallar se distinguían por
el verde y el rojo —el par que más confunde un daltónico—; ahora la opción
correcta da un pulso y la fallada una sacudida.

**El movimiento cuenta la jerarquía.** Bajar a un nivel entra deslizando desde
la derecha y el botón de atrás lo saca por donde entró; cambiar de pestaña se
funde, porque entre hermanas no hay jerarquía que contar. Todo en
`ui/navigation/Transiciones.kt`, y respeta el ajuste de «quitar animaciones» del
sistema, que Compose no respeta solo.

**Salir de un cuestionario pregunta antes.** Las respuestas se registran al
terminar la sesión, no una a una: salir a medias pierde la sesión entera.

## Tecnologías

Kotlin · Jetpack Compose · Room · Navigation Compose · Hilt · Retrofit · Coil ·
Coroutines y Flow

## Cómo ejecutar

1. Abrir el proyecto en Android Studio.
2. Crear `local.properties` con `RETA2_API_KEY`.
3. Para hablar con el panel desplegado, nada más: la compilación `debug` ya
   apunta ahí. Para hablar con el panel de tu máquina, levantarlo en la misma red
   y añadir `RETA2_BASE_URL` con su IP.
4. Ejecutar en un emulador o dispositivo.

Desde la línea de comandos:

```bash
./gradlew assembleDebug        # compilar
./gradlew testDebugUnitTest    # pruebas unitarias
./gradlew connectedAndroidTest # pruebas instrumentadas (requiere dispositivo)
./gradlew lint
```

### Publicar una versión

```bash
.\scripts\preparar-release.ps1
```

No publica: comprueba y construye, y se **niega a seguir** si algo no cuadra.
Publicar son cinco pasos manuales y cuatro pueden salir mal en silencio, así que
el script mira lo que nadie miraba:

| Comprueba | Porque si no |
| --- | --- |
| El árbol está limpio | el APK no correspondería a ningún commit y nadie podría reproducirlo |
| El commit está empujado | la etiqueta apuntaría a algo que nadie más tiene |
| El `versionCode` supera al de la última etiqueta | Android se niega a instalar encima y el estudiante solo ve «no se pudo instalar» |
| Salió `app-release.apk`, no el sin firmar | un APK sin firmar no se instala |
| La firma es la de publicación | con la de depuración —la misma en todas las máquinas— cualquiera podría publicar una actualización de la app |
| El APK declara la misma versión que el código | es como se caza una compilación vieja |
| El `mapping.txt` existe | se sobrescribe en cada compilación: el de *ese* APK solo existe en ese momento |

Al final imprime las órdenes con los valores ya puestos. **Adjunta los dos
ficheros al Release**, no solo el APK.

> Existe por lo que pasó en la v1.1.0: la etiqueta quedó apuntando a un commit
> donde `versionCode` todavía era 1 y el proyecto ni siquiera sabía firmar,
> mientras el APK publicado venía de tres commits más adelante. Nadie se enteró,
> porque nada lo miraba. La etiqueta se corrigió después.

### La firma

El APK de release se firma con un keystore propio que **no está en el
repositorio** y cuya ruta y contraseñas se leen de `local.properties`:

```properties
RETA2_KEYSTORE=C:/ruta/al/reta2-release.jks
RETA2_KEYSTORE_PASSWORD=...
RETA2_KEY_ALIAS=reta2
RETA2_KEY_PASSWORD=...
```

Las barras van **normales**, no invertidas: un `.properties` de Java trata la
barra invertida como escape y se la come, y la ruta que llega no existe. Si
falta la configuración, `assembleRelease` saca un APK sin firmar y lo avisa en
consola; sin esa advertencia, el problema aparece al ir a repartirlo.

**Huella del certificado de publicación** (SHA-256), para comprobar que un APK
descargado es el auténtico:

```
50:12:0C:E1:73:B7:92:96:12:03:E4:F1:84:0D:FD:F6:A4:C2:47:F9:C3:4B:1A:20:2E:02:87:FF:48:17:D2:92
```

```bash
apksigner verify --print-certs app-release.apk
```

> **El keystore no se puede perder ni rotar.** Android identifica una app por su
> firma: sin esa clave, la siguiente versión es para el sistema una aplicación
> distinta y no hay forma de actualizar la que ya tengan instalada los
> estudiantes. Guárdalo fuera del repositorio y con copia aparte.

> **Un APK firmado no se instala encima de uno de desarrollo.** Las firmas no
> coinciden y Android responde `INSTALL_FAILED_UPDATE_INCOMPATIBLE`; hay que
> desinstalar primero, lo que borra el progreso local de ese teléfono. A un
> estudiante no le afecta —instala de cero—, pero al desarrollador sí.

Para la versión de publicación:

```bash
./gradlew assembleRelease
```

El APK resultante **hay que firmarlo** antes de distribuirlo; sin firma Android
lo rechaza. Conviene archivar el `mapping.txt` de cada APK distribuido: sin él
no se puede leer una traza de error de una versión con R8.

## Arquitectura

```
 UI (Compose)                    →  ViewModel
 ui/screens/**/XScreen.kt           ui/screens/**/XViewModel.kt
                   ↓
 Casos de uso
 domain/usecases/GetQuestionsUseCase.kt, UpdateProgressUseCase.kt
                   ↓
 Repositorios (interfaz → implementación)
 domain/repositories/*.kt  →  data/repositories/*Impl.kt
                   ↓
 Datos
 · Room            data/local/{entities,dao,database}
 · API             data/remote/{Reta2ApiService,dto}
 · Arranque        data/source/CompetencyData.kt
```

La inyección la configura Hilt en `di/` (`RepositotyModule.kt`,
`UseCaseModule.kt`, `DatabaseModule.kt`).

## Licencia

Proyecto académico — Universidad de Santander (UDES).
