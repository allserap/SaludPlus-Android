package com.citas.medicas.ui.paciente

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch


private lateinit var adapter: MapaAdapter
class MapaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa)

        val rvUnidades = findViewById<RecyclerView>(R.id.rvListaUnidadesMapa)
        rvUnidades.layoutManager = LinearLayoutManager(this)

        adapter = MapaAdapter(emptyList())
        rvUnidades.adapter = adapter

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
                R.id.nav_mapa -> {
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

        cargarUnidades()

    }

    private fun cargarUnidades() {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@MapaActivity)
                val response = apiService.getUnidadesMapa()

                if (response.isSuccessful && response.body()?.exito == true) {
                    val datos = response.body()?.datos

                    if (datos != null && datos.isNotEmpty()) {
                        adapter.actualizarDatos(datos)

                        val tituloUnidades = findViewById<TextView>(R.id.tvTituloTodasUnidades)
                        tituloUnidades?.text = "🏢 Todas las Unidades (${datos.size})"


                        val primeraUnidad = datos[0]
                        findViewById<TextView>(R.id.tvDestacadoNombre).text = primeraUnidad.nombre
                        findViewById<TextView>(R.id.tvDestacadoDireccion).text = primeraUnidad.direccion

                        findViewById<MaterialCardView>(R.id.btnAbrirMaps).setOnClickListener {
                            if (primeraUnidad.latitud != null && primeraUnidad.longitud != null) {
                                val uri = "geo:${primeraUnidad.latitud},${primeraUnidad.longitud}?q=${primeraUnidad.latitud},${primeraUnidad.longitud}(${primeraUnidad.nombre})"
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@MapaActivity, "Error al cargar el mapa", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
                Toast.makeText(this@MapaActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }
}