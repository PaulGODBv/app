package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "levels",
    foreignKeys = [
        ForeignKey(
            entity = CompetenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["competence_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LevelEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "competence_id")
    val competenceId: Int,

    /**
     * El id que este nivel tiene en el panel, o null si todavia no se sabe.
     *
     * Es la identidad estable entre los dos lados. El id local se calcula al
     * crearlo como `competencia * 100 + order`, pero **el order puede cambiar**
     * si alguien reordena los niveles en el panel, y entonces un id derivado
     * apuntaria a otro sitio y dejaria los intentos huerfanos. Eso ya paso una
     * vez, cuando el banco cambio de numeracion (ver MIGRACION_15_16), y costo
     * media docena de sintomas raros entenderlo.
     *
     * Guardando el id del panel, reordenar deja de importar.
     *
     * Null en los niveles sembrados desde `CompetencyData` que todavia no han
     * visto una sincronizacion; la primera los adopta emparejandolos por el id
     * local que les corresponde.
     */
    @ColumnInfo(name = "django_id")
    val djangoId: Int? = null,

    /**
     * Si el panel sigue publicando este nivel.
     *
     * Un nivel que desaparece del catalogo **no se borra**: el progreso y los
     * intentos del estudiante apuntan a su id, y borrar la fila los dejaria
     * huerfanos. Se deja de ofrecer, que es lo que de verdad se queria. Si el
     * nivel vuelve al panel, vuelve con su progreso intacto.
     *
     * Los niveles que todavia no tienen `django_id` siguen publicados: de esos
     * no se sabe si el panel los conoce, y esconderlos por no saberlo dejaria
     * al estudiante sin niveles.
     */
    @ColumnInfo(name = "publicado")
    val publicado: Boolean = true,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = true,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "progress")
    val progress: Float = 0f,

    /** Como se juega en practica: "opcion", "unir" o "arrastrar". */
    @ColumnInfo(name = "formato_practica")
    val formatoPractica: String = "opcion",

    /**
     * Cuando se practico este nivel por ultima vez, en milisegundos. 0 = nunca.
     *
     * Se escribe en **los dos modos**. Es lo que alimenta "Continuar
     * practicando", que antes miraba el progreso y por eso ignoraba todo lo
     * hecho en practica.
     */
    @ColumnInfo(name = "last_practiced_at")
    val ultimaPractica: Long = 0L
)