# Reta2

Paul Mateo Contreras Arias - 01220371027

Resumen
-------
Aplicación educativa Android escrita en Kotlin con Jetpack Compose que ofrece prácticas por competencias, preguntas y modos de juego (incluido un modo contrarreloj). Las preguntas están actualmente embebidas (hardcodeadas) en la aplicación mediante CompetencyData.

Estructura de carpetas (resumen)
--------------------------------
- app/src/main/java/com/universidad/reta2/
  - data/
    - local/ — entidades Room, DAOs y mappers
    - repositories/ — implementaciones de repositorios que consumen los datos locales o estáticos
    - source/ — CompetencyData: datos de competencias, niveles y preguntas hardcodeados
    - preferences/ — SessionManager (SharedPreferences)
  - domain/
    - models/ — modelos de dominio (User, Question, Competence, Level, QuestionOption, UserStats...)
    - repositories/ — interfaces de repositorios (QuestionRepository, UserRepository, ProgressRepository, etc.)
    - usecases/ — casos de uso que encapsulan lógica de negocio (GetQuestionsUseCase, UpdateProgressUseCase...)
  - ui/
    - screens/ — pantallas composables por funcionalidad (home, questions, timedmode, registration, splash, profile, progress...)
    - navigation/ — NavGraph y definición de rutas (Screen.kt)
  - di/ — módulos Hilt para inyección de dependencias
  - utils/ — utilidades transversales (PasswordHasher: hash de contraseñas)

Uso de repositorios
-------------------
- QuestionRepository: interfaz para obtener preguntas. Implementado por QuestionRepositoryImpl y utiliza CompetencyData como fuente principal de preguntas.
- UserRepository / UserStatsRepository / ProgressRepository: encapsulan operaciones sobre usuarios, estadísticas y progreso. Implementaciones usan DAOs y mappers para persistencia local.
- CompetenceRepository: expone competencias y niveles y compone información para la UI.

Base de datos de preguntas
-------------------------
Las preguntas se mantienen en memoria en el archivo CompetencyData (hardcode). Esto simplifica pruebas y desarrollo. El repositorio de preguntas está preparado para consumir datos desde CompetencyData, y el flujo de selección (priorizar preguntas no correctamente respondidas) está implementado en QuestionRepositoryImpl + GetQuestionsUseCase.

Estructura de la base de datos (Room)
-----------------------------------
Entidades registradas en AppDatabase (versión actual: 9):
- UserEntity (table: users)
  - username: String (PK)
  - email: String
  - password_hash: String — hash SHA-256 con salt, nunca la contraseña en claro
  - student_code: String
  - student_program: String
  - created_at: Long
- UserStatsEntity
- CompetenceEntity
- LevelEntity
- QuestionEntity
- QuestionOptionEntity
- QuestionAttemptEntity
- LevelProgressEntity

Modelos principales (domain/models)
----------------------------------
- User: username, email, passwordHash, studentCode, studentProgram
- Question: id, text, options (QuestionOption), correctOptionId, readingText, contextImage, etc.
- QuestionOption: id, text
- Competence: id, name, list de Level
- Level: id, name, description, isLocked, etc.
- UserStats: estadística del usuario (preguntas respondidas, tiempo, racha...)

Implementaciones y mapeadores
-----------------------------
- Mappers en data/local/mappers convierten entre entidades Room (UserEntity, UserStatsEntity, etc.) y modelos de dominio.
- Repositorios en data/repositories implementan las interfaces de domain/repositories y ofrecen la lógica de selección, filtrado y transformaciones necesarias por la UI y los usecases.

Inyección de dependencias
-------------------------
- Hilt se usa para DI. Los módulos están en di/ (RepositoryModule, UseCaseModule, etc.).
- Proveedores típicos: AppDatabase/DAOs, mappers, repositorios, casos de uso.

Seguridad de credenciales
-------------------------
Las contraseñas no se guardan ni se comparan en texto plano. `utils/PasswordHasher.kt` genera un salt aleatorio de 16 bytes por contraseña y persiste el resultado con el formato:

```
sha256$<salt en hexadecimal>$<hash en hexadecimal>
```

- `hashPassword(password)` — crea un salt nuevo y devuelve la cadena completa.
- `verifyPassword(password, storedHash)` — recalcula con el salt guardado y compara en tiempo constante (`MessageDigest.isEqual`).
- `isHashed(value)` — valida el formato; `UserRepositoryImpl` lo usa como última barrera para no escribir texto plano en Room.

Dónde interviene:

| Flujo | Comportamiento |
| --- | --- |
| Registro | `RegistrationViewModel` hashea antes de construir el `User` |
| Inicio de sesión | `LoginViewModel` compara con `verifyPassword`, no con `==` |
| Cambio de contraseña | Perfil exige la contraseña actual, la verifica contra el hash y solo entonces guarda el hash de la nueva; si no coincide muestra «Contraseña actual incorrecta» |
| Sesión y sincronización | `SessionManager` no guarda credenciales y los DTO no envían la contraseña al servidor |

El salt por usuario evita tablas precalculadas y que dos cuentas con la misma clave compartan hash. SHA-256 sigue siendo una función rápida: para un despliegue real se recomienda migrar a PBKDF2 o Argon2, cambio acotado a la función privada `computeHash()`.

Tecnologías usadas
------------------
- Kotlin
- Jetpack Compose (UI declarativa)
- Android Architecture Components: ViewModel, Room, Navigation (compose)
- Hilt (inyección de dependencias)
- Coroutines y Flow para asincronía y streams reactivos

Notas operativas
----------------
- La base de datos Room está configurada con fallbackToDestructiveMigration() por simplicidad de desarrollo; actualizar la versión (ahora v9) provocará recreación del DB en dispositivos con versiones previas. Para producción se debe añadir migraciones no destructivas.
- La versión 9 renombró la columna `password` como `password_hash`. Al recrearse la base se eliminaron las contraseñas en texto plano de instalaciones anteriores: los usuarios previos deben registrarse de nuevo.
- CompetencyData contiene el contenido de preguntas; migrar a una fuente externa (archivo JSON o servidor) es posible implementando un repositorio distinto.
- El modo contrarreloj (TimedMode) y la selección aleatoria priorizan preguntas no resueltas correctamente; la selección nunca pedirá más preguntas que las disponibles.

Cómo ejecutar
--------------
1. Abrir el proyecto en Android Studio
2. Construir el proyecto (Gradle) y ejecutar en un emulador o dispositivo

Ejecutar tests y comandos Gradle
--------------------------------
Desde la línea de comandos (en la raíz del proyecto):

- Compilar la app (debug):
  - Linux / macOS: `./gradlew assembleDebug`
  - Windows: `gradlew.bat assembleDebug` o `gradlew assembleDebug`

- Ejecutar tests unitarios locales:
  - `./gradlew testDebugUnitTest` (o `gradlew.bat testDebugUnitTest` en Windows)

- Ejecutar pruebas instrumentadas (requieren emulador/dispositivo conectado):
  - `./gradlew connectedAndroidTest`

- Ejecutar lint:
  - `./gradlew lint`

En Android Studio:
- Importa el proyecto y usa los botones Run / Debug para ejecutar la app en un emulador o dispositivo.
- Ejecuta tests con el panel de Test (Run > Run 'All Tests' o clic derecho sobre un paquete/test).

Diagrama de arquitectura (ASCII)
--------------------------------
La siguiente representación muestra las capas principales y ejemplos de archivos para cada una:

 UI (Compose)                           ViewModel
 -------------------------------------------------------------
 - ui/screens/home/HomeScreen.kt        - ui/screens/home/HomeViewModel.kt
 - ui/screens/questions/QuestionScreen  - ui/screens/questions/QuestionViewModel.kt
 - ui/screens/timedmode/TimedModeScreen - ui/screens/timedmode/TimedModeViewModel.kt

               ↓
 Use Cases / Business Logic
 -------------------------------------------------------------
 - domain/usecases/GetQuestionsUseCase.kt
 - domain/usecases/UpdateProgressUseCase.kt

               ↓
 Repositories (interfaces)  →  Implementaciones
 -------------------------------------------------------------
 - domain/repositories/QuestionRepository.kt
 - data/repositories/QuestionRepositoryImpl.kt
 - domain/repositories/UserRepository.kt
 - data/repositories/UserRepositoryImpl.kt

               ↓
 Data sources
 -------------------------------------------------------------
 - Local DB (Room)
   - app/src/main/java/.../data/local/entities/*.kt
   - app/src/main/java/.../data/local/dao/*.kt (ProgressDao, UserDao...)
   - app/src/main/java/.../data/local/database/database.kt

 - Static source
   - app/src/main/java/.../data/source/CompetencyData.kt (Preguntas hardcodeadas)

Inyección de dependencias
-------------------------
- Hilt configura la creación de repositorios, DAOs y usecases. Módulos relevantes:
  - di/RepositotyModule.kt
  - di/UseCaseModule.kt

