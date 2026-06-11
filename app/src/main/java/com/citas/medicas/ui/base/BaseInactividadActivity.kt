package com.citas.medicas.ui.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.citas.medicas.utils.SessionDialogHelper

abstract class BaseInactividadActivity : AppCompatActivity() {

    // 5 minutos en milisegundos (Puedes cambiarlo a 30000 para probar con 30 segundos si funciona)
    private val TIEMPO_INACTIVIDAD: Long = 5 * 60 * 1000

    private val handlerInactividad = Handler(Looper.getMainLooper())

    // Lo que ocurrirá cuando se acabe el tiempo
    private val runnableInactividad = Runnable {
        // Dispara el diálogo automático de: ¿Deseas mantenerte conectado?
        SessionDialogHelper.mostrarDialogoExpiracion(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetearTemporizador()
    }

    // CAPTURA CUALQUIER TOQUE EN LA PANTALLA DEL TELÉFONO
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Cada vez que el médico toca la pantalla, se reinicia el conteo de los 5 minutos
        resetearTemporizador()
        return super.dispatchTouchEvent(ev)
    }

    private fun resetearTemporizador() {
        handlerInactividad.removeCallbacks(runnableInactividad)
        handlerInactividad.postDelayed(runnableInactividad, TIEMPO_INACTIVIDAD)
    }

    override fun onResume() {
        super.onResume()
        resetearTemporizador()
    }

    override fun onPause() {
        super.onPause()
        // Evita fugas de memoria si el usuario minimiza la app
        handlerInactividad.removeCallbacks(runnableInactividad)
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerInactividad.removeCallbacks(runnableInactividad)
    }
}