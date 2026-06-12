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
Entidades registradas en AppDatabase (versión actual: 8):
- UserEntity (table: users)
  - username: String (PK)
  - email: String
  - password: String
  - student_code: String
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
- User: username, email, password, studentCode
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

Tecnologías usadas
------------------
- Kotlin
- Jetpack Compose (UI declarativa)
- Android Architecture Components: ViewModel, Room, Navigation (compose)
- Hilt (inyección de dependencias)
- Coroutines y Flow para asincronía y streams reactivos

Notas operativas
----------------
- La base de datos Room está configurada con fallbackToDestructiveMigration() por simplicidad de desarrollo; actualizar la versión (ahora v8) provocará recreación del DB en dispositivos con versiones previas. Para producción se debe añadir migraciones no destructivas.
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

