package com.citas.medicas.ui.paciente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.ui.AppDatabase
import com.citas.medicas.models.CitaHistorial
import com.citas.medicas.ui.paciente.local.entities.toEntity
import com.citas.medicas.ui.paciente.local.entities.toModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialCitasActivity : AppCompatActivity() {

    private var listaProximas = listOf<CitaHistorial>()
    private var listaPasadas = listOf<CitaHistorial>()
    private lateinit var adapter: HistorialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial_citas)

        // Inicializar RecyclerView
        val rvHistorial = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvHistorialCitas)
        rvHistorial.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = HistorialAdapter(emptyList())
        rvHistorial.adapter = adapter

        // Referencias de los nuevos filtros en el XML
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutHistorial)
        val scrollFiltros = findViewById<HorizontalScrollView>(R.id.scrollFiltros)
        val chipGroupFiltros = findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupFiltros)

        // Control de pestañas (Próximas vs Pasadas)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Pestaña Próximas
                        scrollFiltros.visibility = View.GONE
                        adapter.actualizarDatos(listaProximas)
                    }
                    1 -> { // Pestaña Pasadas (Aquí prendemos los Chips)
                        scrollFiltros.visibility = View.VISIBLE
                        aplicarFiltroPasadas()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        chipGroupFiltros.setOnCheckedStateChangeListener { _, _ ->
            if (tabLayout.selectedTabPosition == 1) {
                aplicarFiltroPasadas()
            }
        }

        // Configuración de botón Solicitar Nueva Cita
        val btnNuevaCita = findViewById<Button>(R.id.btnNuevaCitaFlotante)
        btnNuevaCita.setOnClickListener {
            startActivity(Intent(this, SolicitarCitaActivity::class.java))
        }

        // Configuración de la barra de navegación inferior
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationHistorial)
        bottomNav.selectedItemId = R.id.nav_historial
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, HomePacienteActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                    true
                }
                R.id.nav_solicitar -> {
                    startActivity(Intent(this, SolicitarCitaActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                    true
                }
                R.id.nav_historial -> true
                R.id.nav_mapa -> {
                    startActivity(Intent(this, MapaActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                    true
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                    true
                }
                else -> false
            }
        }

        // Carga inicial de datos
        cargarHistorial()
    }

    private fun cargarHistorial() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
        val usuarioId = prefs.getString("user_usuarioid", "") ?: ""

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@HistorialCitasActivity)
            val citasDao = db.citasDao()

            val citasGuardadas = citasDao.obtenerTodasLasCitas()
            if (citasGuardadas.isNotEmpty()) {
                val todasLasCitas = citasGuardadas.map { it.toModel() }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hoy = sdf.parse(sdf.format(java.util.Date()))

                val estadosProximas = listOf("pendiente", "confirmada")

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

    private fun pintarHistorialEnPantalla(proximas: List<CitaHistorial>, pasadas: List<CitaHistorial>, isOffline: Boolean) {
        listaProximas = proximas
        listaPasadas = pasadas

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutHistorial)
        tabLayout.getTabAt(0)?.text = "📅 Próximas (${listaProximas.size})"
        tabLayout.getTabAt(1)?.text = "✓ Pasadas (${listaPasadas.size})"

        if (tabLayout.selectedTabPosition == 0) {
            adapter.actualizarDatos(listaProximas)
        } else {
            aplicarFiltroPasadas() // Mantiene el filtro si la API responde estando parados en Pasadas
        }
    }

    private fun aplicarFiltroPasadas() {
        val chipGroup = findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupFiltros)
        val seleccion = chipGroup.checkedChipId

        val listaFiltrada = when (seleccion) {
            R.id.chipCanceladas -> listaPasadas.filter {
                it.estado?.lowercase()?.contains("cancelada") == true
            }
            R.id.chipReprogramadas -> listaPasadas.filter {
                it.estado?.lowercase()?.contains("reprogramada") == true
            }
            R.id.chipCompletadas -> listaPasadas.filter {
                it.estado?.lowercase()?.contains("atendida") == true ||
                        it.estado?.lowercase()?.contains("finalizada") == true
            }
            else -> listaPasadas // "Todos"
        }

        adapter.actualizarDatos(listaFiltrada)
    }
}