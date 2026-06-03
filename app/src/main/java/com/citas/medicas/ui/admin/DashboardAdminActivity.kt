package com.citas.medicas.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.citas.medicas.R
import com.citas.medicas.databinding.ActivityDashboardAdminBinding
import com.citas.medicas.databinding.ActivityDashboardMedicoBinding
import com.citas.medicas.ui.auth.LoginActivity
import com.citas.medicas.ui.auth.RegistroActivity
import com.citas.medicas.ui.medico.AgendaFragment
import com.citas.medicas.ui.medico.HistorialFragment
import com.citas.medicas.ui.medico.RecetasFragment
import com.citas.medicas.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Inicializar el binding
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar fragment
        if (savedInstanceState == null) {
            replaceFragment(UsuariosFragment())
            actualizarEstiloTabs(pestanaActiva = "usuarios")
        }

        cargarDatosHeader()
        setupNavigation()
    }
    private fun cargarDatosHeader() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        // Recuperar valores reales del Administrador desde las SharedPreferences
        val nombre = prefs.getString("user_nombre", "") ?: ""
        val apellido = prefs.getString("user_apellido", "") ?: ""
        val email = prefs.getString("user_email", "admin@saludplus.com") ?: "admin@saludplus.com"

        // Editar la UI de la cabecera usando View Binding
        with(binding) {
            // Asigna directamente los textos correspondientes al rol administrativo
            tvHeaderRol.text = "Administrador del Sistema"
            tvHeaderNombre.text = "$nombre $apellido".trim().ifEmpty { "Administrador" }
        }
    }
    private fun setupNavigation() {
        with(binding) {
            tabUsuarios.setOnClickListener {
                replaceFragment(UsuariosFragment())
                actualizarEstiloTabs(pestanaActiva = "usuarios")
            }
            tabEstadisticas.setOnClickListener {
                replaceFragment(EstadisticasFragment())
                actualizarEstiloTabs(pestanaActiva = "estadisticas")
            }
            tabGestiones.setOnClickListener {
                replaceFragment(GestionesFragment())
                actualizarEstiloTabs(pestanaActiva = "gestiones")
            }

            btnSalir.setOnClickListener {
                // Borrar token al salir
                val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                prefs.edit().clear().apply()
                MaterialAlertDialogBuilder(this@DashboardAdminActivity)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Está seguro de que desea salir del sistema?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ ->
                        SessionManager.logout(this@DashboardAdminActivity)

                        // Redirige al Login limpiando el historial de navegación
                        val intent = Intent(this@DashboardAdminActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                    .show()
            }
        }
    }

    // Funcion para cambiar de fragment
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.containerFragment, fragment)
            .commit()
    }

    // Función auxiliar para que visualmente cambie el diseño del Tab seleccionado
    private fun actualizarEstiloTabs(pestanaActiva: String) {
        with(binding) {
            // Primero removemos el fondo resaltado de todos
            tabUsuarios.setBackgroundResource(0)
            tabEstadisticas.setBackgroundResource(0)
            tabGestiones.setBackgroundResource(0)

            // Se lo aplicamos únicamente al que está activo en el momento
            when (pestanaActiva) {
                "usuarios" -> tabUsuarios.setBackgroundResource(R.drawable.bg_tabs)
                "estadisticas" -> tabEstadisticas.setBackgroundResource(R.drawable.bg_tabs)
                "gestiones" -> tabGestiones.setBackgroundResource(R.drawable.bg_tabs)
            }
        }
    }
}