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

    @Query("SELECT * FROM levels WHERE competence_id = :competenceId ORDER BY id")
    suspend fun getLevelsByCompetence(competenceId: Int): List<LevelEntity>

    @Query("SELECT * FROM levels WHERE id = :levelId AND competence_id = :competenceId")
    suspend fun getLevel(competenceId: Int, levelId: Int): LevelEntity?

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