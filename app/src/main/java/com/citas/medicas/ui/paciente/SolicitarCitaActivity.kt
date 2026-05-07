package com.citas.medicas.ui.paciente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.adapter.EspecialidadAdapter
import com.citas.medicas.adapter.HoraAdapter
import com.citas.medicas.adapter.UnidadMedicaAdapter
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.Especialidad
import com.citas.medicas.models.UnidadMedicaFiltro
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SolicitarCitaActivity : AppCompatActivity() {
    val apiService = RetrofitClient.getApiService(this@SolicitarCitaActivity)
    private var pasoActual = 1

    private var fechaSeleccionadaReal: String? = null
    private var horaSeleccionadaReal: String? = null
    private lateinit var adapterHoras: HoraAdapter

    // 1. VARIABLE REAL DE LA API
    private var especialidadSeleccionada: Especialidad? = null
    private var unidadSeleccionadaReal: UnidadMedicaFiltro? = null
    private lateinit var adapterUnidades: UnidadMedicaAdapter

    private lateinit var adapterEspecialidad: EspecialidadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_cita)

        val btnSiguiente = findViewById<MaterialButton>(R.id.btnSiguiente)
        val btnAnterior = findViewById<MaterialButton>(R.id.btnAnterior)
        val tvSelectDate = findViewById<TextView>(R.id.tvSelectDate)
        val layoutStep2 = findViewById<LinearLayout>(R.id.layoutStep2)


        val rvEspecialidades = findViewById<RecyclerView>(R.id.rvEspecialidades)

        rvEspecialidades.layoutManager = GridLayoutManager(this, 2)

        adapterEspecialidad = EspecialidadAdapter(emptyList()) { especialidad ->
            especialidadSeleccionada = especialidad
            btnSiguiente.isEnabled = true

        }
        rvEspecialidades.adapter = adapterEspecialidad

        //  PREPARAR (UNIDADES MÉDICAS)
        val rvUnidadesMedicas = findViewById<RecyclerView>(R.id.rvUnidadesMedicas)
        rvUnidadesMedicas.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this) // Esta lista va en 1 columna vertical

        adapterUnidades = UnidadMedicaAdapter(emptyList()) { unidad ->
            unidadSeleccionadaReal = unidad
            btnSiguiente.isEnabled = true
        }
        rvUnidadesMedicas.adapter = adapterUnidades


        //  PREPARAR (FECHA Y HORA)
        val rvHoras = findViewById<RecyclerView>(R.id.rvHoras)
        rvHoras.layoutManager = GridLayoutManager(this, 4)

        adapterHoras = HoraAdapter(emptyList()) { hora ->
            horaSeleccionadaReal = hora
            btnSiguiente.isEnabled = true
        }
        rvHoras.adapter = adapterHoras

        // --- CALENDARIO ---
        tvSelectDate.setOnClickListener {
            val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccione la fecha")
                .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val date = java.util.Date(selection)
                val formatVisual = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es", "ES"))
                tvSelectDate.text = formatVisual.format(date)

                val formatApi = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                fechaSeleccionadaReal = formatApi.format(date)

                Log.d("API_DEBUG", "Fecha seleccionada: $fechaSeleccionadaReal")

                if (unidadSeleccionadaReal != null && especialidadSeleccionada != null) {
                    cargarHorarios()
                }
            }
            picker.show(supportFragmentManager, "DATE_PICKER")
        }


        actualizarVista()

        cargarEspecialidades()


        btnSiguiente.setOnClickListener {
            when (pasoActual) {
                1 -> {

                    if (especialidadSeleccionada != null) {
                        pasoActual = 2
                        unidadSeleccionadaReal = null
                        actualizarVista()
                        cargarUnidades(especialidadSeleccionada!!.id)
                    }
                }
                2 -> {
                    if (unidadSeleccionadaReal != null) {
                        pasoActual = 3
                        horaSeleccionadaReal = null
                        actualizarVista()
                        findViewById<TextView>(R.id.tvSelectDate).text = "dd/mm/aaaa"
                        adapterHoras.actualizarDatos(emptyList())
                    }
                }
                3 -> {
                    if (horaSeleccionadaReal != null && fechaSeleccionadaReal != null) {
                        pasoActual = 4
                        actualizarVista()

                    }
                }
                4 -> {
                    val intent = Intent(this, HomePacienteActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
        }

        btnAnterior.setOnClickListener {
            if (pasoActual > 1) {
                pasoActual--
                actualizarVista()
            } else {
                finish()
            }
        }
    }

    private fun actualizarVista() {
        val layoutStep1 = findViewById<LinearLayout>(R.id.layoutStep1)
        val layoutStep2 = findViewById<LinearLayout>(R.id.layoutStep2)
        val layoutStep3 = findViewById<LinearLayout>(R.id.layoutStep3)
        val layoutSuccess = findViewById<LinearLayout>(R.id.layoutSuccess)
        val btnSiguiente = findViewById<MaterialButton>(R.id.btnSiguiente)
        val btnAnterior = findViewById<MaterialButton>(R.id.btnAnterior)
        val tvStepCounter = findViewById<TextView>(R.id.tvStepCounter)

        layoutStep1.visibility = View.GONE
        layoutStep2.visibility = View.GONE
        layoutStep3.visibility = View.GONE
        layoutSuccess.visibility = View.GONE

        when (pasoActual) {
            1 -> {
                layoutStep1.visibility = View.VISIBLE
                btnAnterior.visibility = View.VISIBLE
                tvStepCounter.text = "Paso 1 de 3"
                btnSiguiente.text = "Siguiente"

                btnSiguiente.isEnabled = (especialidadSeleccionada != null)
            }
            2 -> {
                layoutStep2.visibility = View.VISIBLE
                btnAnterior.visibility = View.VISIBLE
                tvStepCounter.text = "Paso 2 de 3"
                btnSiguiente.text = "Siguiente"
                btnSiguiente.isEnabled = (unidadSeleccionadaReal != null)
            }
            3 -> {
                layoutStep3.visibility = View.VISIBLE
                btnAnterior.visibility = View.VISIBLE
                tvStepCounter.text = "Paso 3 de 3"
                btnSiguiente.text = "Confirmar Cita"
                btnSiguiente.isEnabled = (horaSeleccionadaReal != null && fechaSeleccionadaReal != null)
            }
            4 -> {
                layoutSuccess.visibility = View.VISIBLE
                btnAnterior.visibility = View.GONE
                btnSiguiente.text = "Volver al Inicio"
                btnSiguiente.isEnabled = true
                tvStepCounter.text = "¡Cita Confirmada!"

                val nombreEspecialidad = especialidadSeleccionada?.nombre ?: "No especificada"
                val nombreUnidad = unidadSeleccionadaReal?.nombre ?: "No especificada"

                val nombreMedico = "Por asignar"

                val fechaBonita = findViewById<TextView>(R.id.tvSelectDate).text.toString()

                findViewById<TextView>(R.id.tvResumenEspecialidad).text = "Especialidad: $nombreEspecialidad \nUnidad: $nombreUnidad"
                findViewById<TextView>(R.id.tvResumenMedico).text = "Médico: $nombreMedico"
                findViewById<TextView>(R.id.tvResumenFecha).text = "Fecha: $fechaBonita"
                findViewById<TextView>(R.id.tvResumenHora).text = "Hora: ${horaSeleccionadaReal ?: "--:--"}"
            }
        }
    }


    private fun cargarEspecialidades() {
        lifecycleScope.launch {
            try {

                val response = apiService.getEspecialidades()
                if (response.isSuccessful) {
                    val datos = response.body()?.datos
                    if (datos != null && datos.isNotEmpty()) {
                        Log.d("API_DEBUG", "Especialidades cargadas: ${datos.size}")
                        adapterEspecialidad.actualizarDatos(datos)
                    } else {
                        Toast.makeText(this@SolicitarCitaActivity, "No hay especialidades", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SolicitarCitaActivity, "Error del servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción de red", e)
                Toast.makeText(this@SolicitarCitaActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarUnidades(idEspecialidad: Int) {
        lifecycleScope.launch {
            try {
                val response = apiService.getUnidadesFiltradas(idEspecialidad)
                if (response.isSuccessful) {
                    val datos = response.body()?.datos
                    if (datos != null && datos.isNotEmpty()) {
                        Log.d("API_DEBUG", "Unidades cargadas: ${datos.size}")
                        adapterUnidades.actualizarDatos(datos)
                    } else {
                        adapterUnidades.actualizarDatos(emptyList())
                        Toast.makeText(this@SolicitarCitaActivity, "No hay unidades para esta especialidad", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SolicitarCitaActivity, "Error al cargar unidades", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción de red en unidades", e)
                Toast.makeText(this@SolicitarCitaActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarHorarios() {
        val idUnidad = unidadSeleccionadaReal?.id ?: return
        val idEspecialidad = especialidadSeleccionada?.id ?: return
        val fecha = fechaSeleccionadaReal ?: return

        Log.d("API_DEBUG", "Pidiendo horas -> Unidad: $idUnidad, Especialidad: $idEspecialidad, Fecha: $fecha")

        lifecycleScope.launch {
            try {
                val response = apiService.getHorariosDisponibles(idUnidad, idEspecialidad, fecha)

                if (response.isSuccessful) {
                    val datos = response.body()?.datos
                    if (datos != null && datos.isNotEmpty()) {
                        Log.d("API_DEBUG", "¡Éxito! Node devolvió ${datos.size} horas")
                        adapterHoras.actualizarDatos(datos)
                    } else {
                        Log.d("API_DEBUG", "Node devolvió un arreglo vacío []")
                        adapterHoras.actualizarDatos(emptyList())
                        Toast.makeText(this@SolicitarCitaActivity, "La clínica está cerrada o sin cupo ese día", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SolicitarCitaActivity, "Error al cargar horarios", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción de red en horarios", e)
                Toast.makeText(this@SolicitarCitaActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }
}