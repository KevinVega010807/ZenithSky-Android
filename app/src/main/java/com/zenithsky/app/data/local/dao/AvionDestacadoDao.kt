package com.zenithsky.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenithsky.app.data.local.entity.AvionDestacado
import kotlinx.coroutines.flow.Flow

@Dao
interface AvionDestacadoDao {

    @Query("SELECT * FROM avion_destacado")
    fun obtenerTodos(): Flow<List<AvionDestacado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(avion: AvionDestacado)

    @Delete
    suspend fun eliminar(avion: AvionDestacado)

    @Query("DELETE FROM avion_destacado WHERE airplaneId = :airplaneId")
    suspend fun eliminarPorAirplaneId(airplaneId: Int)
}
