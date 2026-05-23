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
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.data.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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
            try {
                val apiService = RetrofitClient.getApiService(this@HomePacienteActivity)

                val response = apiService.getHistorialCitas(usuarioId)

                if (response.isSuccessful && response.body()?.exito == true) {
                    val listaProximas = response.body()?.datos?.proximas ?: emptyList()

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


                            // LÓGICA DEL BOTÓN CANCELAR
                            val btnCancel = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelAppt)

                            btnCancel.setOnClickListener {
                                com.google.android.material.dialog.MaterialAlertDialogBuilder(this@HomePacienteActivity)
                                    .setTitle("⚠️ ¿Cancelar Cita?")
                                    .setMessage("¿Estás seguro de cancelar tu cita de ${cita.especialidad}?\n\nEsta acción es irreversible y perderás tu espacio reservado.")
                                    .setPositiveButton("Sí, cancelar") { dialog, which ->

                                        // apiService.cancelarCita(cita.id)

                                        Toast.makeText(this@HomePacienteActivity, "Simulando cancelación...", Toast.LENGTH_SHORT).show()

                                        // quitar la tarjeta de la vista de una vez
                                        // container.removeView(view)
                                    }
                                    .setNegativeButton("Mantener", null)
                                    .show()
                            }

                            //  LÓGICA DEL BOTÓN REPROGRAMAR
                            val btnChange = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangeAppt)

                            btnChange.setOnClickListener {
                                Toast.makeText(this@HomePacienteActivity, "Pronto abriremos el menú para reprogramar", Toast.LENGTH_SHORT).show()
                            }

//                            val btnChange = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangeAppt)
//
//                            btnChange.setOnClickListener {
//                                // cita.especialidad_id y cita.unidad_medica_id
//
//                                if (cita.especialidad_id != null && cita.unidad_medica_id != null) {
//                                    abrirMenuReprogramar(cita)
//                                } else {
//                                    // Un pequeño seguro por si acaso el backend falla
//                                    Toast.makeText(this@HomePacienteActivity, "Faltan datos de la clínica para reprogramar.", Toast.LENGTH_SHORT).show()
//                                }
//                            }

                            container.addView(view)
                        }
                    } else {
                        Log.d("API_DEBUG", "El paciente no tiene citas próximas")
                    }
                } else {
                    Log.e("API_DEBUG", "Error servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Error de red: ${e.message}")
            }
        }
    }


//    private fun abrirMenuReprogramar(cita: CitaHistorial) {
//        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
//
//        // val view = layoutInflater.inflate(R.layout.bottom_sheet_reprogramar, null)
//        // bottomSheetDialog.setContentView(view)
//
//        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
//            .setTitleText("Selecciona una nueva fecha")
//            .build()
//
//        datePicker.addOnPositiveButtonClickListener { selection ->
//            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val nuevaFecha = sdf.format(Date(selection))
//
//            Toast.makeText(this, "Buscando horas para el $nuevaFecha...", Toast.LENGTH_SHORT).show()
//
//            cargarHorariosReprogramacion(cita.unidad_medica_id!!, cita.especialidad_id!!, nuevaFecha, cita.id)
//        }
//
//        datePicker.show(supportFragmentManager, "DATE_PICKER_REPROGRAMAR")
//    }

    private fun cargarHorariosReprogramacion(idUnidad: Int, idEspecialidad: Int, nuevaFecha: String, idCitaOriginal: String) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@HomePacienteActivity)
                val response = apiService.getHorariosDisponibles(idUnidad, idEspecialidad, nuevaFecha)

                if (response.isSuccessful) {
                    val horasDisponibles = response.body()?.datos
                    if (horasDisponibles != null && horasDisponibles.isNotEmpty()) {



                        Toast.makeText(this@HomePacienteActivity, "¡Horas encontradas! Falta mostrarlas.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@HomePacienteActivity, "No hay cupo para ese día.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@HomePacienteActivity, "Error de red al buscar horas.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}