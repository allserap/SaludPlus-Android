package com.citas.medicas.ui.paciente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.DatosPerfil
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

private var perfilActual: DatosPerfil? = null
class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationPerfil)

        val btnEditar = findViewById<Button>(R.id.btnEditarPerfil)

        bottomNav.selectedItemId = R.id.nav_perfil

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
                    val intent = Intent(this, MapaActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> {
                    true
                }
                else -> false
            }
        }

        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            // Solo pasamos los datos si ya cargaron de la API
            perfilActual?.let { perfil ->
                intent.putExtra("EXTRA_TELEFONO", perfil.telefono)
                intent.putExtra("EXTRA_ALERGIAS", perfil.alergias)
                intent.putExtra("EXTRA_CRONICAS", perfil.condiciones_cronicas)
                // Nota: Tu diseño de editar tiene "Dirección", pero la API no la devuelve.
                // La dejaremos vacía o con un placeholder por ahora.
                intent.putExtra("EXTRA_DIRECCION", "")
            }
            startActivity(intent)
        }
        cargarPerfil()
    }

    private fun cargarPerfil() {
        // En el futuro, sacar esto de SharedPreferences
        val pacienteId = 1

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@PerfilActivity)
                val response = apiService.getPerfilPaciente(pacienteId)

                if (response.isSuccessful && response.body()?.exito == true) {
                    val datos = response.body()?.datos
                    if (datos != null) {
                        perfilActual = datos

                        val iniciales = "${datos.nombre?.firstOrNull() ?: ""}${datos.apellido?.firstOrNull() ?: ""}"
                        findViewById<TextView>(R.id.tvAvatarInitials).text = iniciales.uppercase()
                        findViewById<TextView>(R.id.tvPerfilNombre).text = "${datos.nombre} ${datos.apellido}"

                        findViewById<TextView>(R.id.tvPerfilAfiliado).text = datos.num_afiliado ?: "No disponible"
                        findViewById<TextView>(R.id.tvPerfilDUI).text = datos.dui ?: "No disponible"
                        findViewById<TextView>(R.id.tvPerfilCorreo).text = datos.email ?: "No disponible"

                        // La API actual no trae dirección
                        // findViewById<TextView>(R.id.tvPerfilDireccion).text = datos.direccion

                        // Llenar Información de Salud
                        findViewById<TextView>(R.id.tvSaludCronicas).text = datos.condiciones_cronicas ?: "Ninguna registrada"
                        findViewById<TextView>(R.id.tvSaludAlergias).text = datos.alergias ?: "Ninguna registrada"

                    // La API actual no trae medicinas actuales
                        // findViewById<TextView>(R.id.tvSaludMedicinas).text = "..."

                    }
                } else {
                    Toast.makeText(this@PerfilActivity, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error de conexión", e)
                Toast.makeText(this@PerfilActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }
}