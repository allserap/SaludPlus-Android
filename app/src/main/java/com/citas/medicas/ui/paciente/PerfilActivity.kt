package com.citas.medicas.ui.paciente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.citas.medicas.R
import com.citas.medicas.models.DatosPerfil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class PerfilActivity : AppCompatActivity() {

    private var perfilActual: DatosPerfil? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationPerfil)
        val btnEditar = findViewById<MaterialButton>(R.id.btnEditarPerfil)

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
            perfilActual?.let { perfil ->
                intent.putExtra("EXTRA_TELEFONO", perfil.telefono)
                intent.putExtra("EXTRA_ALERGIAS", perfil.alergias)
                intent.putExtra("EXTRA_CRONICAS", perfil.condiciones_cronicas)
                intent.putExtra("EXTRA_MEDICINAS", perfil.medicinas)
            }
            startActivity(intent)
        }

        cargarPerfil()
    }

    private fun cargarPerfil() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        val nombre = prefs.getString("user_nombre", "Usuario")
        val apellido = prefs.getString("user_apellido", "")
        val afiliado = prefs.getString("user_afiliado", "No disponible")
        val dui = prefs.getString("user_dui", "No disponible")
        val email = prefs.getString("user_email", "No disponible")
        val telefono = prefs.getString("user_telefono", "No disponible")
        val cronicas = prefs.getString("user_cronicas", "Ninguna registrada")
        val alergias = prefs.getString("user_alergias", "Ninguna registrada")
        val medicinas = prefs.getString("user_medicinas", "Ninguna registrada")
        val sangre = prefs.getString("user_sangre", "No especificado")

        val iniciales = "${nombre?.firstOrNull() ?: ""}${apellido?.firstOrNull() ?: ""}"
        findViewById<TextView>(R.id.tvAvatarInitials).text = iniciales.uppercase()
        findViewById<TextView>(R.id.tvPerfilNombre).text = "$nombre $apellido"

        // Datos Personales
        findViewById<TextView>(R.id.tvPerfilAfiliado).text = afiliado
        findViewById<TextView>(R.id.tvPerfilDUI).text = dui
        findViewById<TextView>(R.id.tvPerfilCorreo).text = email
        findViewById<TextView>(R.id.tvPerfilTelefono).text = telefono

        // Datos Médicos
        findViewById<TextView>(R.id.tvSaludAlergias).text = alergias
        findViewById<TextView>(R.id.tvSaludCronicas).text = cronicas
        findViewById<TextView>(R.id.tvSaludMedicinas).text = medicinas

        perfilActual = DatosPerfil(
            nombre = nombre,
            apellido = apellido,
            num_afiliado = afiliado,
            dui = dui,
            email = email,
            condiciones_cronicas = cronicas,
            alergias = alergias,
            telefono = telefono,
            tipo_sangre = sangre,
            medicinas = medicinas
        )
    }

    override fun onResume() {
        super.onResume()
        cargarPerfil()
    }
}