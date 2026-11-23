package com.zenithsky.app.ui.airport

import android.content.Context
import com.google.gson.JsonParser
import com.zenithsky.app.data.local.ZenithSkyDatabase
import com.zenithsky.app.data.local.entity.OperacionLog
import retrofit2.Response


fun Response<*>.mensajeError(): String {
    return try {
        val raw = errorBody()?.string()
        if (raw.isNullOrBlank()) "Error ${code()}"
        else JsonParser.parseString(raw).asJsonObject.get("message").asString
    } catch (e: Exception) {
        "Error ${code()}"
    }
}


object AirportLog {
    suspend fun registrar(context: Context, operacion: String, detalle: String) {
        ZenithSkyDatabase.getInstance(context).operacionLogDao().insertar(
            OperacionLog(
                operacion = operacion,
                detalle = detalle,
                fecha = System.currentTimeMillis()
            )
        )
    }
}
