package com.citas.medicas.ui.medico

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.citas.medicas.R
import com.citas.medicas.databinding.ActivityDashboardMedicoBinding
import com.citas.medicas.databinding.ActivityRegistroBinding
import com.citas.medicas.ui.auth.LoginActivity
import com.citas.medicas.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardMedicoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar el binding
        binding = ActivityDashboardMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar fragment
        if (savedInstanceState == null) {
            replaceFragment(AgendaFragment())
            actualizarEstiloTabs(pestanaActiva = "agenda")
        }

        cargarDatosHeader()
        setupNavigation()
    }

    private fun cargarDatosHeader() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        // Recuperar valores reales del Médico desde las SharedPreferences
        val nombre = prefs.getString("user_nombre", "") ?: ""
        val apellido = prefs.getString("user_apellido", "") ?: ""

        // Recupera la especialidad
        val especialidad = prefs.getString("user_especialidad", "Medicina General") ?: "Medicina General"

        with(binding) {
            tvHeaderRol.text = "Médico Activo"

            // Formatear el nombre con el prefijo "Dr." de forma limpia
            tvHeaderNombre.text = "Dr. $nombre $apellido".trim()
            tvHeaderEspecialidad.text = especialidad
        }
    }
            private fun setupNavigation() {
                with(binding) {
                    tabAgenda.setOnClickListener {
                        replaceFragment(AgendaFragment())
                        actualizarEstiloTabs(pestanaActiva = "agenda")
                    }
                    tabHistorial.setOnClickListener {
                        replaceFragment(HistorialFragment())
                        actualizarEstiloTabs(pestanaActiva = "historial")
                    }
                    tabRecetas.setOnClickListener {
                        replaceFragment(RecetasFragment())
                        actualizarEstiloTabs(pestanaActiva = "recetas")
                    }
                    tabPerfil.setOnClickListener {
                        replaceFragment(PerfilMedicoFragment())
                        actualizarEstiloTabs(pestanaActiva = "perfil")
                    }
                    btnSalir.setOnClickListener {
                        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                        prefs.edit().clear().apply()
                        MaterialAlertDialogBuilder(this@DashboardMedicoActivity)
                            .setTitle("Cerrar Sesión")
                            .setMessage("¿Está seguro de que desea salir del sistema?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Salir") { _, _ ->
                                SessionManager.logout(this@DashboardMedicoActivity)

                                // Redirige al Login limpiando el historial de navegación
                                val intent = Intent(this@DashboardMedicoActivity, LoginActivity::class.java).apply {
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

    // Función dinámica para alternar el fondo de la pestaña activa
    private fun actualizarEstiloTabs(pestanaActiva: String) {
        with(binding) {
            // Removemos el fondo de todas las pestañas primero
            tabAgenda.setBackgroundResource(0)
            tabHistorial.setBackgroundResource(0)
            tabRecetas.setBackgroundResource(0)
            tabPerfil.setBackgroundResource(0)

            // Asignamos el recurso visual solo a la seleccionada
            when (pestanaActiva) {
                "agenda" -> tabAgenda.setBackgroundResource(R.drawable.bg_tabs)
                "historial" -> tabHistorial.setBackgroundResource(R.drawable.bg_tabs)
                "recetas" -> tabRecetas.setBackgroundResource(R.drawable.bg_tabs)
                "perfil" -> tabPerfil.setBackgroundResource(R.drawable.bg_tabs)
            }
        }
    }
}