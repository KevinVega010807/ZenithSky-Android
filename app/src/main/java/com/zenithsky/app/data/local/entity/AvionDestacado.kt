package com.zenithsky.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avion_destacado")
data class AvionDestacado(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val airplaneId: Int = 0,
    val descripcion: String = ""
)
