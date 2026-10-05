package com.universidad.reta2.data.local.database

import androidx.room.Database
import com.universidad.reta2.data.local.entities.UserEntity
import com.universidad.reta2.data.local.entities.UserStatsEntity
import com.universidad.reta2.data.local.entities.CompetenceEntity
import com.universidad.reta2.data.local.entities.LevelEntity
import com.universidad.reta2.data.local.entities.QuestionEntity
import com.universidad.reta2.data.local.entities.QuestionOptionEntity
import com.universidad.reta2.data.local.entities.QuestionAttemptEntity
import com.universidad.reta2.data.local.entities.LevelProgressEntity
import androidx.room.RoomDatabase
import com.universidad.reta2.data.local.dao.UserDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.QuestionDao
import com.universidad.reta2.data.local.dao.CompetenceDao
import com.universidad.reta2.data.local.dao.LevelDao
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase



@Database(
    entities = [
        UserEntity::class,
        UserStatsEntity::class,
        CompetenceEntity::class,
        LevelEntity::class,
        QuestionEntity::class,
        QuestionOptionEntity::class,
        QuestionAttemptEntity::class,
        LevelProgressEntity::class
    ],
    // v9: la columna "password" pasó a "password_hash" y guarda el hash SHA-256
    // con salt. Al subir la versión, fallbackToDestructiveMigration borró la base
    // local y eliminó de paso las contraseñas que estaban en texto plano.
    //
    // v10: marcas históricas para los logros (ver MIGRACION_9_10). Primera
    // migración escrita a mano del proyecto: aquí sí había que conservar el
    // progreso, así que no se podía tirar de la vía destructiva.
    //
    // v11: las preguntas dejan de ser solo de lectura. Se añaden los campos que
    // llegan del panel (texto de lectura, URL de imagen, competencia, marca de
    // cambio y activa/retirada) para que el banco pueda vivir en Room.
    //
    // v12: el nivel de práctica de cada competencia (ver MIGRACION_11_12).
    //
    // v13: Comunicación Escrita como quinta competencia (ver MIGRACION_12_13).
    //
    // v14: cómo se juega cada nivel en práctica (ver MIGRACION_13_14).
    //
    // v15: cuándo se practicó cada nivel por última vez (ver MIGRACION_14_15).
    //
    // v16: los intentos viejos apuntan al banco actual (ver MIGRACION_15_16).
    version = 16
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun progressDao(): ProgressDao
    abstract fun questionDao(): QuestionDao
    abstract fun competenceDao(): CompetenceDao
    abstract fun levelDao(): LevelDao



    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v9 → v10: dos columnas de máximo histórico en `user_stats`.
         *
         * Los logros se medían contra los contadores vivos, y la racha y el
         * tiempo del día se reinician por diseño: al perder la racha, la
         * insignia «Alcanza una racha de 3 días» volvía a salir bloqueada
         * aunque ya se hubiera conseguido. Ahora se miden contra estas marcas,
         * que solo suben.
         *
         * El `UPDATE` final siembra las marcas con lo que ya hay guardado. Sin
         * él, la migración le quitaría a quien ya tenga la app una insignia que
         * se ganó: es lo más que se puede reconstruir, porque el histórico de
         * rachas nunca se guardó.
         */
        /**
         * v10 → v11: la tabla `questions` pasa a guardar el banco descargado.
         *
         * Hasta ahora era una tabla muerta —existía pero nadie escribía en
         * ella— y las preguntas salían de `CompetencyData`. Estas columnas son
         * lo que hace falta para que el panel sea la fuente de verdad sin que
         * la app pierda el modo offline.
         *
         * `context_image_url` guarda la dirección, no la imagen: el binario lo
         * cachea Coil en disco. Meter los bytes aquí hincharía la base y
         * duplicaría un trabajo que la librería ya hace mejor.
         */
        /**
         * v11 → v12: el nivel de práctica de cada competencia.
         *
         * **Por qué hace falta una migración para meter cuatro filas.** El
         * catálogo de niveles solo se siembra la primera vez que se abre la
         * app: `getAllCompetences()` escribe los niveles del código únicamente
         * si la tabla `competences` está vacía. En cualquier teléfono que ya
         * tenga la app instalada, esa tabla tiene datos, así que añadir los
         * niveles a la lista del código no los haría aparecer nunca. Esto es lo
         * que los pone también ahí.
         *
         * El id se calcula igual que en el código —`competencia * 100`— para
         * que las dos vías den lo mismo, y se insertan a partir de las
         * competencias que ya existan, no de una lista fija: si alguna no está,
         * no se inventa una fila huérfana que rompería la clave foránea.
         *
         * `INSERT OR IGNORE` para que sea inocuo si la fila ya está, que es el
         * caso de quien instale limpio sobre la versión nueva.
         */
        /**
         * v12 → v13: Comunicación Escrita, la quinta competencia.
         *
         * Requerimiento de Desarrollo Estudiantil. Como el catálogo solo se
         * siembra en la primera instalación, sin esta migración la competencia
         * no aparecería en ningún teléfono que ya tenga la app — el mismo
         * motivo que obligó a MIGRACION_11_12.
         *
         * **El icono se copia del de Lectura Crítica en vez de escribir un
         * número.** `icon` guarda un id de recurso de Android, y esos ids los
         * reasigna el compilador en cada build: dejar una constante aquí
         * funcionaría hoy y apuntaría a cualquier cosa mañana. Copiando el de
         * otra fila se obtiene el valor válido de este build, sea cual sea.
         * Comunicación Escrita no tiene icono propio todavía.
         */
        /**
         * v13 → v14: el formato de práctica de cada nivel.
         *
         * El panel ya decidía esto —`Level.formato_practica`— y lo mandaba en
         * la API, pero la app no lo guardaba en ninguna parte: la
         * sincronización solo escribía preguntas, nunca niveles. Sin esta
         * columna no hay forma de saber que «Feelings» se juega uniendo
         * parejas y «Complete the text» arrastrando.
         *
         * Por defecto "opcion", que es como se jugaba todo hasta ahora: la
         * migración no cambia el comportamiento de ningún nivel existente
         * hasta que el panel diga otra cosa.
         */
        /**
         * v14 → v15: la marca de última práctica de cada nivel.
         *
         * «Continuar practicando» filtraba por `totalProgress > 0`, y ese
         * progreso sale de los intentos acertados. Como la práctica **no
         * registra intentos** —a propósito, para no mover el porcentaje ni el
         * desbloqueo—, practicar no aparecía nunca ahí: la sección enseñaba
         * una competencia vieja de evaluación e ignoraba todo lo recién hecho.
         *
         * Con esta marca la sección mira actividad, que es lo que su nombre
         * promete, sin tocar el progreso ni el desbloqueo.
         */
        /**
         * v15 → v16: los intentos viejos apuntan al banco de hoy (TODO-18).
         *
         * Cuando el banco paso a venir del panel, las preguntas cambiaron de id:
         * `CompetencyData` las numeraba 401..409 y 1001..1012, y el panel las
         * numera 1..111. Los intentos ya registrados se quedaron apuntando a los
         * viejos, asi que contaban como trabajo hecho pero no correspondian a
         * ninguna pregunta existente: un nivel superado se enseñaba al 0 %, y el
         * siguiente aparecia desbloqueado sin motivo aparente.
         *
         * **El puente es el enunciado.** Es el mismo texto en los dos bancos,
         * porque el panel se sembro desde `CompetencyData`. El emparejamiento se
         * hizo fuera, comparando enunciados normalizados —sin acentos, sin dobles
         * espacios, en minusculas—, y las veinte parejas salieron sin ambiguedad.
         * Aqui va el resultado, no el algoritmo: una migracion no puede adivinar,
         * tiene que aplicar algo ya decidido.
         *
         * Cada UPDATE lleva tambien el nivel. No hace falta hoy —los niveles
         * coinciden— pero acota el cambio a la fila que se quiere tocar en vez de
         * a cualquiera que comparta numero.
         */
        val MIGRACION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 15 " +
                        "WHERE question_id = 401 AND level_id = 201"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 73 " +
                        "WHERE question_id = 402 AND level_id = 201"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 16 " +
                        "WHERE question_id = 403 AND level_id = 201"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 17 " +
                        "WHERE question_id = 405 AND level_id = 201"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 75 " +
                        "WHERE question_id = 406 AND level_id = 201"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 76 " +
                        "WHERE question_id = 407 AND level_id = 201"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 77 " +
                        "WHERE question_id = 408 AND level_id = 201"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 19 " +
                        "WHERE question_id = 409 AND level_id = 201"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 47 " +
                        "WHERE question_id = 1001 AND level_id = 401"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 48 " +
                        "WHERE question_id = 1002 AND level_id = 401"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 95 " +
                        "WHERE question_id = 1003 AND level_id = 401"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 52 " +
                        "WHERE question_id = 1004 AND level_id = 401"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 49 " +
                        "WHERE question_id = 1005 AND level_id = 401"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 96 " +
                        "WHERE question_id = 1006 AND level_id = 401"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 53 " +
                        "WHERE question_id = 1007 AND level_id = 402"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 54 " +
                        "WHERE question_id = 1008 AND level_id = 402"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 55 " +
                        "WHERE question_id = 1009 AND level_id = 402"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 97 " +
                        "WHERE question_id = 1010 AND level_id = 402"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 98 " +
                        "WHERE question_id = 1011 AND level_id = 402"
                )
                db.execSQL(
                    "UPDATE question_attempts SET question_id = 56 " +
                        "WHERE question_id = 1012 AND level_id = 402"
                )
            }
        }

        val MIGRACION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE levels ADD COLUMN last_practiced_at INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRACION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE levels ADD COLUMN formato_practica TEXT NOT NULL DEFAULT 'opcion'"
                )
            }
        }

        val MIGRACION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO competences (id, name, description, icon, total_progress)
                    SELECT 5, 'Comunicación Escrita',
                           'Cohesión, precisión léxica y corrección gramatical: los ejes con los que el Icfes califica el texto escrito.',
                           icon, 0.0
                    FROM competences WHERE id = 1
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO levels
                        (id, competence_id, name, description, is_locked, is_completed, progress)
                    SELECT 501, 5, 'Nivel 1 – Cohesión y corrección',
                           'Conectores, adverbios y artículos dentro de una frase con sentido.',
                           0, 0, 0.0
                    FROM competences WHERE id = 5
                    """.trimIndent()
                )
            }
        }

        val MIGRACION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO levels
                        (id, competence_id, name, description, is_locked, is_completed, progress)
                    SELECT c.id * 100, c.id, 'Calentamiento',
                           'Ítems cortos para coger ritmo. No cuenta para desbloquear: al responder te dice si acertaste y por qué.',
                           0, 0, 0.0
                    FROM competences c
                    """.trimIndent()
                )
            }
        }

        val MIGRACION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN competence_id INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN reading_text TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE questions ADD COLUMN context_image_url TEXT")
                db.execSQL("ALTER TABLE questions ADD COLUMN context_image_alt TEXT")
                db.execSQL("ALTER TABLE questions ADD COLUMN context_image TEXT")
                db.execSQL("ALTER TABLE questions ADD COLUMN remote_updated_at TEXT")
                db.execSQL("ALTER TABLE questions ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRACION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_stats ADD COLUMN max_streak_days INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE user_stats ADD COLUMN max_daily_practice_time INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "UPDATE user_stats SET max_streak_days = current_streak_days, " +
                        "max_daily_practice_time = daily_practice_time"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reta2_database"
                )
                    .addMigrations(
                        MIGRACION_9_10,
                        MIGRACION_10_11,
                        MIGRACION_11_12,
                        MIGRACION_12_13,
                        MIGRACION_13_14,
                        MIGRACION_14_15,
                        MIGRACION_15_16
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
