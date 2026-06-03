package com.citas.medicas.ui.medico

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.citas.medicas.R
import com.citas.medicas.databinding.ActivityDashboardMedicoBinding
import com.citas.medicas.ui.auth.LoginActivity
import com.citas.medicas.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardMedicoBinding

    // Lista para iterar y manejar los estados de las pestañas de forma masiva
    private lateinit var listaTabs: List<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el binding
        binding = ActivityDashboardMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Agrupamos las referencias de los tabs usando View Binding
        listaTabs = listOf(binding.tabAgenda, binding.tabHistorial, binding.tabRecetas, binding.tabPerfil)

        // Inicializar fragmento por defecto
        if (savedInstanceState == null) {
            replaceFragment(AgendaFragment())
            seleccionarTabVisual(binding.tabAgenda)
        }

        cargarDatosHeader()
        setupNavigation()
    }

    private fun cargarDatosHeader() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        val nombre = prefs.getString("user_nombre", "") ?: ""
        val apellido = prefs.getString("user_apellido", "") ?: ""
        val BlacklistEspecialidad = prefs.getString("user_especialidad", "Medicina General") ?: "Medicina General"

        with(binding) {
            tvHeaderRol.text = "Médico Activo"
            tvHeaderNombre.text = "Dr. $nombre $apellido".trim()
            tvHeaderEspecialidad.text = BlacklistEspecialidad
        }
    }

    private fun setupNavigation() {
        with(binding) {
            tabAgenda.setOnClickListener {
                replaceFragment(AgendaFragment())
                seleccionarTabVisual(tabAgenda)
            }
            tabHistorial.setOnClickListener {
                replaceFragment(HistorialFragment())
                seleccionarTabVisual(tabHistorial)
            }
            tabRecetas.setOnClickListener {
                replaceFragment(RecetasFragment())
                seleccionarTabVisual(tabRecetas)
            }
            tabPerfil.setOnClickListener {
                replaceFragment(PerfilMedicoFragment())
                seleccionarTabVisual(tabPerfil)
            }

            btnSalir.setOnClickListener {
                MaterialAlertDialogBuilder(this@DashboardMedicoActivity)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Está seguro de que desea salir del sistema?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ ->
                        // Limpiar SharedPreferences al salir
                        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                        prefs.edit().clear().apply()

                        SessionManager.logout(this@DashboardMedicoActivity)

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

    // Función genérica para cambiar fragmentos
    private fun replaceFragment(fragment: Fragment) {
        // 1. Buscamos el fragmento que actualmente está pintado en el contenedor
        val fragmentActual = supportFragmentManager.findFragmentById(R.id.containerFragment)

        // 2. Si el fragmento actual es de la misma clase que el que queremos abrir, ignoramos el click
        if (fragmentActual != null && fragmentActual::class.java == fragment::class.java) {
            return
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.containerFragment, fragment)
            .commit()
    }

    // Cambia el estado .isSelected. Android se encarga de pintar el fondo con el XML
    private fun seleccionarTabVisual(tabSeleccionado: LinearLayout) {
        listaTabs.forEach { tab ->
            tab.isSelected = (tab == tabSeleccionado)
        }
    }
}