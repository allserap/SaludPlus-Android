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
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        val nombre = prefs.getString("user_nombre", "")
        val apellido = prefs.getString("user_apellido", "")
        val afiliado = prefs.getString("user_afiliado", "No disponible")
        val dui = prefs.getString("user_dui", "No disponible")
        val email = prefs.getString("user_email", "No disponible")
        val cronicas = prefs.getString("user_cronicas", "Ninguna registrada")
        val alergias = prefs.getString("user_alergias", "Ninguna registrada")
        val telefono = prefs.getString("user_telefono", "")
        val sangre = prefs.getString("user_sangre", "No especificado")

        // 1. Pintamos los datos en la pantalla
        val iniciales = "${nombre?.firstOrNull() ?: ""}${apellido?.firstOrNull() ?: ""}"
        findViewById<TextView>(R.id.tvAvatarInitials).text = iniciales.uppercase()
        findViewById<TextView>(R.id.tvPerfilNombre).text = "$nombre $apellido"

        findViewById<TextView>(R.id.tvPerfilAfiliado).text = afiliado
        findViewById<TextView>(R.id.tvPerfilDUI).text = dui
        findViewById<TextView>(R.id.tvPerfilCorreo).text = email

        findViewById<TextView>(R.id.tvSaludCronicas).text = cronicas
        findViewById<TextView>(R.id.tvSaludAlergias).text = alergias

        perfilActual = DatosPerfil(
            nombre = nombre,
            apellido = apellido,
            num_afiliado = afiliado,
            dui = dui,
            email = email,
            condiciones_cronicas = cronicas,
            alergias = alergias,
            telefono = telefono,
            tipo_sangre = sangre
        )
    }

    override fun onResume() {
        super.onResume()
        cargarPerfil()
    }

}