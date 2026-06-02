package com.citas.medicas.ui.paciente

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.ui.paciente.adapter.MapaAdapter
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.ui.AppDatabase
import com.citas.medicas.ui.paciente.adapter.UnidadMedicaAdapter
import com.citas.medicas.ui.paciente.local.entities.toEntity
import com.citas.medicas.ui.paciente.local.entities.toModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class MapaActivity : AppCompatActivity() {

    // 🌟 1. Declaramos el adapter a nivel de clase para que todas las funciones lo conozcan
    private lateinit var adapter: UnidadMedicaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa)

        val rvUnidades = findViewById<RecyclerView>(R.id.rvListaUnidadesMapa)
        rvUnidades.layoutManager = LinearLayoutManager(this)

        // 🛠️ Inicializamos el adapter correctamente
        adapter = UnidadMedicaAdapter()
        rvUnidades.adapter = adapter // Asignamos al RecyclerView real

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationMapa)
        bottomNav.selectedItemId = R.id.nav_mapa

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
                    val intent = Intent(this, HistorialCitasActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_mapa -> true
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        cargarUnidades()
    }

    private fun cargarUnidades() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MapaActivity)
            val unidadDao = db.unidadMedicaDao()

            val unidadesGuardadas = unidadDao.obtenerTodasLasUnidades()
            if (unidadesGuardadas.isNotEmpty()) {
                val unidadesParaPantalla = unidadesGuardadas.map { it.toModel() }
                pintarUnidadesEnPantalla(unidadesParaPantalla, isOffline = true)
            }

            try {
                val apiService = RetrofitClient.getApiService(this@MapaActivity)
                val response = apiService.getUnidadesMapa()

                if (response.isSuccessful && response.body()?.exito == true) {
                    val datosRed = response.body()?.datos

                    if (datosRed != null && datosRed.isNotEmpty()) {
                        val unidadesEntities = datosRed.map { it.toEntity() }

                        unidadDao.limpiarTablaUnidades()
                        unidadDao.insertarUnidades(unidadesEntities)

                        pintarUnidadesEnPantalla(datosRed, isOffline = false)
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fallo de red: ${e.message}")
                if (unidadesGuardadas.isNotEmpty()) {
                    Toast.makeText(this@MapaActivity, "Sin conexión. Mostrando mapa guardado.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MapaActivity, "No hay datos guardados y no hay conexión.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun pintarUnidadesEnPantalla(datos: List<com.citas.medicas.models.UnidadMedicaMapa>, isOffline: Boolean) {
        // 1. Actualizamos el título
        val tituloUnidades = findViewById<TextView>(R.id.tvTituloTodasUnidades)
        tituloUnidades?.text = "🏢 Todas las Unidades (${datos.size})"

        // 2. Colocamos la foto genérica del mapa
        val ivMapPreview = findViewById<ImageView>(R.id.ivMapPreview)
        ivMapPreview.setImageResource(R.drawable.mapa_placeholder)

        // 3. Cargamos la primera unidad por defecto al abrir la pantalla
        if (datos.isNotEmpty()) {
            actualizarUnidadDestacada(datos[0], isOffline)
        }

        // 4. Actualizamos el Adapter Y le decimos qué hacer al tocar una tarjeta
        adapter.actualizarDatos(datos)

        // ¡LA MAGIA DE LA INTERACTIVIDAD FUNCIONANDO!
        adapter.setOnItemClickListener { unidadSeleccionada ->
            actualizarUnidadDestacada(unidadSeleccionada, isOffline)
        }
    }

    private fun actualizarUnidadDestacada(unidad: com.citas.medicas.models.UnidadMedicaMapa, isOffline: Boolean) {
        findViewById<TextView>(R.id.tvDestacadoNombre).text = unidad.nombre
        findViewById<TextView>(R.id.tvDestacadoDireccion).text = unidad.direccion

        val lat = unidad.latitud
        val lon = unidad.longitud

        val btnAbrirMaps = findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnAbrirMaps)
        val btnIrDestacado = findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnIrDestacado)

        val clickListener = android.view.View.OnClickListener {
            if (isOffline) {
                Toast.makeText(this@MapaActivity, "Necesitas internet para abrir navegación GPS.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (lat != null && lon != null) {
                val uri = "geo:$lat,$lon?q=$lat,$lon(${unidad.nombre})"
                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri)))
            } else {
                Toast.makeText(this@MapaActivity, "Coordenadas no disponibles para esta clínica.", Toast.LENGTH_SHORT).show()
            }
        }

        btnAbrirMaps.setOnClickListener(clickListener)
        btnIrDestacado.setOnClickListener(clickListener)
    }
}