package com.citas.medicas.ui.paciente

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.citas.medicas.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

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

        // 1. Extraemos el ID real como número entero (Int)
        val usuarioId = prefs.getString("user_usuarioid", "") ?: ""

        findViewById<TextView>(R.id.tvUserName).text = "$nombreUsuario $apellidoUsuario"
        findViewById<TextView>(R.id.tvUserAffiliate).text = "Afiliado: $numAfiliado"

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@HomePacienteActivity)

                val response = apiService.getProximasCitas(usuarioId)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.exito && body.datos.isNotEmpty()) {

                        val container = findViewById<LinearLayout>(R.id.llUpcomingAppointmentsContainer)
                        container.removeAllViews()

                        val citasLimitadas = body.datos.take(3)

                        for (cita in citasLimitadas) {
                            val view = layoutInflater.inflate(R.layout.item_cita_home, container, false)

                            view.findViewById<TextView>(R.id.tvApptType).text = cita.especialidades
                            view.findViewById<TextView>(R.id.tvApptTime).text = cita.hora_asignada
                            view.findViewById<TextView>(R.id.tvApptLocation).text = cita.unidades_medicas
                            view.findViewById<TextView>(R.id.tvApptDoctor).text = cita.doctor

                            try {
                                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                                val formatter = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                                val date = parser.parse(cita.fecha_solicitada ?: "")
                                view.findViewById<TextView>(R.id.tvApptDate).text = formatter.format(date!!)
                            } catch (e: Exception) {
                                view.findViewById<TextView>(R.id.tvApptDate).text = cita.fecha_solicitada
                            }

                            container.addView(view)
                        }

                    } else {
                        Log.d("API_DEBUG", "Citas vacías o exito = false")
                    }
                } else {
                    Log.e("API_DEBUG", "Error servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Error de red: ${e.message}")
            }
        }
    }
}