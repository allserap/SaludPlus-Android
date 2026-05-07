package com.citas.medicas.ui.paciente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.CitaHistorial
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

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
        val pacienteIdId = 1 // Reemplazar por: prefs.getInt("user_id_numerico", 1)

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@HistorialCitasActivity)
                val response = apiService.getHistorialCitas(pacienteIdId)

                if (response.isSuccessful && response.body()?.exito == true) {
                    val datos = response.body()?.datos

                    if (datos != null) {
                        listaProximas = datos.proximas
                        listaPasadas = datos.pasadas

                        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutHistorial)
                        tabLayout.getTabAt(0)?.text = "📅 Próximas (${listaProximas.size})"
                        tabLayout.getTabAt(1)?.text = "✓ Pasadas (${listaPasadas.size})"

                        // Por defecto, mostramos las próximas al entrar
                        adapter.actualizarDatos(listaProximas)
                    }
                } else {
                    Toast.makeText(this@HistorialCitasActivity, "Error al cargar historial", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
                Toast.makeText(this@HistorialCitasActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

}