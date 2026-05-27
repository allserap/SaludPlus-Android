package com.citas.medicas.ui.paciente

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.citas.medicas.R
import com.google.android.material.bottomnavigation.BottomNavigationView

import android.util.Log
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.ActualizarCitaRequest
import com.citas.medicas.models.CitaHistorial
import com.citas.medicas.ui.paciente.local.entities.toEntity
import com.citas.medicas.ui.paciente.local.entities.toModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class HomePacienteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_paciente_activityn)




        val tvVerTodasCitas = findViewById<TextView>(R.id.tvVerTodasCitas)


        tvVerTodasCitas.setOnClickListener {
            startActivity(Intent(this, HistorialCitasActivity::class.java))
        }



        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.selectedItemId = R.id.nav_inicio

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    true
                }
                R.id.nav_solicitar -> {
                    val intent = Intent(this, SolicitarCitaActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }

                R.id.nav_historial -> {
                    val intent = Intent(this, HistorialCitasActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_mapa -> {
                    val intent = Intent(this, MapaActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        cargarCitasDesdeApi()

    }


    private fun cargarCitasDesdeApi() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
        val nombreUsuario = prefs.getString("user_nombre", "Paciente")
        val apellidoUsuario = prefs.getString("user_apellido", "")
        val numAfiliado = prefs.getString("user_afiliado", "No disponible")
        val usuarioId = prefs.getString("user_usuarioid", "") ?: ""

        findViewById<TextView>(R.id.tvUserName).text = "$nombreUsuario $apellidoUsuario"
        findViewById<TextView>(R.id.tvUserAffiliate).text = "Afiliado: $numAfiliado"

        if (usuarioId.isEmpty()) {
            Log.e("API_DEBUG", "¡ALERTA! El usuarioId está vacío.")
            Toast.makeText(this, "Por favor, cierra sesión y vuelve a entrar.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            val db = com.citas.medicas.ui.paciente.local.AppDatabase.getDatabase(this@HomePacienteActivity)
            val citasDao = db.citasDao()

            val citasGuardadas = citasDao.obtenerTodasLasCitas()
            if (citasGuardadas.isNotEmpty()) {
                val citasParaPantalla = citasGuardadas.map { it.toModel() }
                pintarCitasEnPantalla(citasParaPantalla, isOffline = true)
            }

            try {
                val apiService = RetrofitClient.getApiService(this@HomePacienteActivity)
                val response = apiService.getHistorialCitas(usuarioId)

                if (response.isSuccessful && response.body()?.exito == true) {
                    val listaProximas = response.body()?.datos?.proximas ?: emptyList()

                    val citasEntities = listaProximas.map { it.toEntity() }
                    citasDao.limpiarTablaCitas()
                    citasDao.insertarCitas(citasEntities)

                    pintarCitasEnPantalla(listaProximas, isOffline = false)
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Fallo silencioso de red: ${e.message}")

                if (citasGuardadas.isNotEmpty()) {
                    Toast.makeText(this@HomePacienteActivity, "Sin conexión a internet. Viendo datos offline.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun pintarCitasEnPantalla(listaProximas: List<CitaHistorial>, isOffline: Boolean) {
        val container = findViewById<LinearLayout>(R.id.llUpcomingAppointmentsContainer)
        container.removeAllViews()

        if (listaProximas.isNotEmpty()) {
            val citasLimitadas = listaProximas.take(3)

            for (cita in citasLimitadas) {
                val view = layoutInflater.inflate(R.layout.item_cita_home, container, false)

                view.findViewById<TextView>(R.id.tvApptType).text = cita.especialidad
                view.findViewById<TextView>(R.id.tvApptTime).text = cita.hora_asignada
                view.findViewById<TextView>(R.id.tvApptLocation).text = "🏥 ${cita.unidad_medica}"
                view.findViewById<TextView>(R.id.tvApptDoctor).text = "👨‍⚕️ ${cita.doctor ?: "Por asignar"}"

                try {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                    val date = parser.parse(cita.fecha_solicitada ?: "")
                    view.findViewById<TextView>(R.id.tvApptDate).text = "📅 ${formatter.format(date!!)}"
                } catch (e: Exception) {
                    view.findViewById<TextView>(R.id.tvApptDate).text = "📅 ${cita.fecha_solicitada}"
                }

                // LÓGICA DE CANCELAR
                val btnCancel = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelAppt)

                btnCancel.setOnClickListener {
                    if (isOffline) {
                        Toast.makeText(this@HomePacienteActivity, "Necesitas internet para cancelar citas.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    com.google.android.material.dialog.MaterialAlertDialogBuilder(this@HomePacienteActivity)
                        .setTitle("⚠️ ¿Cancelar Cita?")
                        .setMessage("¿Estás seguro de cancelar tu cita de ${cita.especialidad}?\n\nEsta acción es irreversible y perderás tu espacio reservado.")
                        .setPositiveButton("Sí, cancelar") { dialog, which ->

                            btnCancel.isEnabled = false
                            btnCancel.text = "Cancelando..."

                            lifecycleScope.launch {
                                try {
                                    val apiService = RetrofitClient.getApiService(this@HomePacienteActivity)
                                    val request = ActualizarCitaRequest(estado_id = 3)
                                    val response = apiService.actualizarCita(cita.id, request)

                                    if (response.isSuccessful && response.body()?.exito == true) {
                                        Toast.makeText(this@HomePacienteActivity, "¡Cita cancelada con éxito!", Toast.LENGTH_SHORT).show()
                                        container.removeView(view)
                                    } else {
                                        Toast.makeText(this@HomePacienteActivity, "No se pudo cancelar: ${response.body()?.mensaje}", Toast.LENGTH_LONG).show()
                                        btnCancel.isEnabled = true
                                        btnCancel.text = "Cancelar"
                                    }
                                } catch (e: Exception) {
                                    Log.e("API_ERROR", "Error al cancelar cita", e)
                                    Toast.makeText(this@HomePacienteActivity, "Error de red al cancelar", Toast.LENGTH_SHORT).show()
                                    btnCancel.isEnabled = true
                                    btnCancel.text = "Cancelar"
                                }
                            }
                        }
                        .setNegativeButton("Mantener", null)
                        .show()
                }

                // LÓGICA DE REPROGRAMAR
                val btnChange = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangeAppt)

                btnChange.setOnClickListener {
                    if (isOffline) {
                        Toast.makeText(this@HomePacienteActivity, "Necesitas internet para reprogramar citas.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (cita.especialidad_id != null && cita.unidad_medica_id != null) {
                        abrirMenuReprogramar(cita)
                    } else {
                        Toast.makeText(this@HomePacienteActivity, "Ids para citas no encontrados.", Toast.LENGTH_SHORT).show()
                    }
                }

                container.addView(view)
            }
        } else {
            Log.d("API_DEBUG", "El paciente no tiene citas próximas")
        }
    }



    private fun abrirMenuReprogramar(cita: CitaHistorial) {
        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona una nueva fecha")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val nuevaFecha = sdf.format(calendar.time)

            mostrarBottomSheetHoras(cita, nuevaFecha)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER_REPROGRAMAR")
    }

    private fun mostrarBottomSheetHoras(cita: CitaHistorial, nuevaFecha: String) {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_reprogramar, null)
        bottomSheetDialog.setContentView(view)

        val tvDate = view.findViewById<TextView>(R.id.tvSheetDate)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbSheetLoading)
        val tvEmpty = view.findViewById<TextView>(R.id.tvSheetEmpty)
        val chipGroup = view.findViewById<com.google.android.material.chip.ChipGroup>(R.id.cgHorasDisponibles)
        val btnConfirmar = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirmarReprogramacion)

        tvDate.text = "📅 Nueva Fecha: $nuevaFecha"
        var horaSeleccionada: String? = null

        bottomSheetDialog.show()

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@HomePacienteActivity)

                val response = apiService.getHorariosDisponibles(cita.unidad_medica_id!!, cita.especialidad_id!!, nuevaFecha)

                pbLoading.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    val horasDisponibles = response.body()?.datos
                    if (horasDisponibles != null && horasDisponibles.isNotEmpty()) {


                        for (horaStr in horasDisponibles) {

                            val nuevoChip = com.google.android.material.chip.Chip(this@HomePacienteActivity).apply {
                                text = horaStr
                                isCheckable = true
                                isClickable = true

                                setOnCheckedChangeListener { _, isChecked ->
                                    if (isChecked) {
                                        horaSeleccionada = this.text.toString()
                                        btnConfirmar.isEnabled = true
                                    }
                                }
                            }

                            chipGroup.addView(nuevoChip)
                        }
                    } else {
                        tvEmpty.visibility = android.view.View.VISIBLE
                    }
                } else {
                    tvEmpty.text = "Error al obtener horarios."
                    tvEmpty.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
                pbLoading.visibility = android.view.View.GONE
                tvEmpty.text = "Error de red."
                tvEmpty.visibility = android.view.View.VISIBLE
            }
        }

        btnConfirmar.setOnClickListener {
            if (horaSeleccionada == null) return@setOnClickListener

            btnConfirmar.isEnabled = false
            btnConfirmar.text = "Reprogramando..."

            lifecycleScope.launch {
                try {
                    val apiService = RetrofitClient.getApiService(this@HomePacienteActivity)
                    val request = ActualizarCitaRequest(
                        fecha_solicitada = nuevaFecha,
                        hora_asignada = horaSeleccionada
                    )

                    val response = apiService.actualizarCita(cita.id, request)

                    if (response.isSuccessful && response.body()?.exito == true) {
                        Toast.makeText(this@HomePacienteActivity, "¡Cita reprogramada con éxito!", Toast.LENGTH_LONG).show()
                        bottomSheetDialog.dismiss()
                        cargarCitasDesdeApi()
                    } else {
                        Toast.makeText(this@HomePacienteActivity, "Error: ${response.body()?.mensaje}", Toast.LENGTH_SHORT).show()
                        btnConfirmar.isEnabled = true
                        btnConfirmar.text = "Confirmar Nuevo Horario"
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@HomePacienteActivity, "Error de red al reprogramar", Toast.LENGTH_SHORT).show()
                    btnConfirmar.isEnabled = true
                    btnConfirmar.text = "Confirmar Nuevo Horario"
                }
            }
        }
    }
}