package com.citas.medicas.ui.paciente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.CitaHistorial
import com.citas.medicas.ui.paciente.local.entities.toEntity
import com.citas.medicas.ui.paciente.local.entities.toModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private var listaProximas = listOf<CitaHistorial>()
private var listaPasadas = listOf<CitaHistorial>()

private lateinit var adapter: HistorialAdapter
class HistorialCitasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial_citas)



        val rvHistorial = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvHistorialCitas)
        rvHistorial.layoutManager = LinearLayoutManager(this)
        adapter = HistorialAdapter(emptyList())
        rvHistorial.adapter = adapter

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutHistorial)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> adapter.actualizarDatos(listaProximas) // Clic en Próximas
                    1 -> adapter.actualizarDatos(listaPasadas)  // Clic en Pasadas
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })



        val btnNuevaCita = findViewById<Button>(R.id.btnNuevaCitaFlotante)


        btnNuevaCita.setOnClickListener {
            startActivity(Intent(this, SolicitarCitaActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationHistorial)
        bottomNav.selectedItemId = R.id.nav_historial

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, HomePacienteActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_solicitar -> {
                    val intent = Intent(this, SolicitarCitaActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_historial -> {
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

        cargarHistorial();

    }

    private fun cargarHistorial() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
        val usuarioId = prefs.getString("user_usuarioid", "") ?: ""

        lifecycleScope.launch {
            val db = com.citas.medicas.ui.paciente.local.AppDatabase.getDatabase(this@HistorialCitasActivity)
            val citasDao = db.citasDao()

            val citasGuardadas = citasDao.obtenerTodasLasCitas()
            if (citasGuardadas.isNotEmpty()) {
                val todasLasCitas = citasGuardadas.map { it.toModel() }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hoy = sdf.parse(sdf.format(java.util.Date()))

                val estadosProximas = listOf("pendiente", "confirmada", "reprogramada")

                val proximasLocal = todasLasCitas.filter { cita ->
                    var esFutura = false
                    try {
                        val fechaStr = cita.fecha_solicitada?.take(10)
                        if (fechaStr != null) {
                            val fechaCita = sdf.parse(fechaStr)
                            esFutura = fechaCita?.before(hoy) == false
                        }
                    } catch (e: Exception) {
                        esFutura = true
                    }

                    val esEstadoValido = cita.estado?.lowercase(Locale.getDefault()) in estadosProximas

                    esEstadoValido && esFutura
                }

                val pasadasLocal = todasLasCitas.filter { !proximasLocal.contains(it) }

                pintarHistorialEnPantalla(proximasLocal, pasadasLocal, isOffline = true)
            }


            try {
                val apiService = RetrofitClient.getApiService(this@HistorialCitasActivity)
                val response = apiService.getHistorialCitas(usuarioId)

                if (response.isSuccessful && response.body()?.exito == true) {
                    val datos = response.body()?.datos

                    if (datos != null) {
                        val proximasRed = datos.proximas ?: emptyList()
                        val pasadasRed = datos.pasadas ?: emptyList()

                        val todasLasCitasRed = proximasRed + pasadasRed
                        val citasEntities = todasLasCitasRed.map { it.toEntity() }

                        citasDao.limpiarTablaCitas()
                        citasDao.insertarCitas(citasEntities)

                        pintarHistorialEnPantalla(proximasRed, pasadasRed, isOffline = false)
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fallo de red: ${e.message}")
                if (citasGuardadas.isNotEmpty()) {
                    Toast.makeText(this@HistorialCitasActivity, "Sin conexión. Viendo historial guardado.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@HistorialCitasActivity, "No hay historial guardado y no hay conexión.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun pintarHistorialEnPantalla(proximas: List<CitaHistorial>, pasadas: List<com.citas.medicas.models.CitaHistorial>, isOffline: Boolean) {
        listaProximas = proximas
        listaPasadas = pasadas

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutHistorial)
        tabLayout.getTabAt(0)?.text = "📅 Próximas (${listaProximas.size})"
        tabLayout.getTabAt(1)?.text = "✓ Pasadas (${listaPasadas.size})"

        val tabSeleccionada = tabLayout.selectedTabPosition
        if (tabSeleccionada == 0) {
            adapter.actualizarDatos(listaProximas)
        } else {
            adapter.actualizarDatos(listaPasadas)
        }
    }

}