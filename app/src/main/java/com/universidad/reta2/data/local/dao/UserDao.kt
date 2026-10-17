package com.universidad.reta2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.universidad.reta2.data.local.entities.UserEntity


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Busca ignorando los espacios de los extremos, y por eso NO basta con
     * recortar al registrar.
     *
     * Hay cuentas creadas antes de que el registro recortara, guardadas con un
     * espacio final que nadie ve: «Paul Mateo ». Quien la tenga escribe su
     * nombre correctamente y la app le dice que no existe, lo cual desde su
     * silla es incomprensible. Comparando con TRIM a los dos lados, esas
     * cuentas vuelven a entrar sin tocar ni una fila.
     *
     * La sesion guarda despues el nombre TAL COMO ESTA en la base, no el que
     * se tecleo, para que el progreso —que cuelga del nombre— siga casando.
     */
    @Query("SELECT * FROM users WHERE TRIM(username) = TRIM(:username)")
    suspend fun getUser(username: String): UserEntity?

    /** Lo mismo con el correo, y ademas sin distinguir mayusculas: nadie
     *  entiende que su correo "no exista" por haberlo escrito con mayúscula. */
    @Query("SELECT * FROM users WHERE TRIM(LOWER(email)) = TRIM(LOWER(:email))")


































    suspend fun getUserByEmail(email: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username AND email = :email LIMIT 1")
    suspend fun getUserByUsernameAndEmail(username: String, email: String): UserEntity?

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String)
}

