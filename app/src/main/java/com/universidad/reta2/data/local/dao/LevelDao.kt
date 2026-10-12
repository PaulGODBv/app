package com.universidad.reta2.data.local.dao

import com.universidad.reta2.data.local.entities.LevelEntity
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface LevelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: LevelEntity)

    /**
     * Los niveles que se le ensenan al estudiante.
     *
     * Filtra por `publicado`: un nivel que el panel retiro sigue en la tabla
     * —con su progreso— pero deja de aparecer en la lista.
     */
    @Query("SELECT * FROM levels WHERE competence_id = :competenceId AND publicado = 1 ORDER BY id")
    suspend fun getLevelsByCompetence(competenceId: Int): List<LevelEntity>

    @Query("SELECT * FROM levels WHERE id = :levelId AND competence_id = :competenceId")
    suspend fun getLevel(competenceId: Int, levelId: Int): LevelEntity?

    /** Por el id del panel, que es la identidad estable entre los dos lados. */
    @Query("SELECT * FROM levels WHERE django_id = :djangoId LIMIT 1")
    suspend fun getLevelPorDjangoId(djangoId: Int): LevelEntity?

    /** Por el id local, para adoptar un nivel sembrado que aun no tiene django_id. */
    @Query("SELECT * FROM levels WHERE id = :levelId LIMIT 1")
    suspend fun getLevelPorId(levelId: Int): LevelEntity?

    @Query("UPDATE levels SET django_id = :djangoId WHERE id = :levelId")
    suspend fun adoptarDjangoId(levelId: Int, djangoId: Int)

    /**
     * Refresca lo que gobierna el panel y **solo eso**.
     *
     * El nombre, la descripcion y el formato de practica vienen del panel. El
     * bloqueo, el progreso y si esta completado NO: eso se lo gano el
     * estudiante jugando, y pisarlo desde el catalogo le borraria lo que ya
     * tenia desbloqueado.
     */
    @Query("UPDATE levels SET name = :nombre, description = :descripcion, formato_practica = :formato WHERE id = :levelId")
    suspend fun refrescarDesdeElPanel(levelId: Int, nombre: String, descripcion: String, formato: String)

    /**
     * Retira de la lista los niveles que el catalogo ya no trae.
     *
     * Solo toca a los que tienen `django_id`: de un nivel sin adoptar no se
     * sabe si el panel lo conoce, y esconderlo por no saberlo seria peor.
     */
    @Query("UPDATE levels SET publicado = 0 WHERE django_id IS NOT NULL AND django_id NOT IN (:idsDelPanel)")
    suspend fun despublicarLosQueFaltan(idsDelPanel: List<Int>)

    /** Y devuelve a la lista los que el panel vuelve a traer. */
    @Query("UPDATE levels SET publicado = 1 WHERE django_id IN (:idsDelPanel)")
    suspend fun publicarLosQueEstan(idsDelPanel: List<Int>)

    @Update
    suspend fun updateLevel(level: LevelEntity)

    /**
     * Guarda como se juega el nivel en practica.
     *
     * Es lo unico que la sincronizacion escribe de un nivel: el resto —nombre,
     * descripcion, bloqueo— lo gobierna la app, y pisarlo desde el panel
     * borraria el progreso de desbloqueo del estudiante.
     */
    /**
     * Anota que se acaba de entrar a practicar o a evaluarse este nivel.
     *
     * Se llama al abrir la sesion y no al terminarla: abandonarla a mitad
     * tambien cuenta como "estuve aqui", que es lo que "Continuar practicando"
     * quiere recordar.
     */
    @Query("UPDATE levels SET last_practiced_at = :cuando WHERE id = :levelId")
    suspend fun marcarPracticado(levelId: Int, cuando: Long)

    @Query("UPDATE levels SET formato_practica = :formato WHERE id = :levelId")
    suspend fun actualizarFormatoDePractica(levelId: Int, formato: String)

    @Query("UPDATE levels SET is_locked = :isLocked WHERE id = :levelId AND competence_id = :competenceId")
    suspend fun updateLevelLockStatus(competenceId: Int, levelId: Int, isLocked: Boolean)

    @Query("UPDATE levels SET is_completed = :isCompleted, progress = :progress WHERE id = :levelId AND competence_id = :competenceId")
    suspend fun updateLevelProgress(competenceId: Int, levelId: Int, isCompleted: Boolean, progress: Float)
}