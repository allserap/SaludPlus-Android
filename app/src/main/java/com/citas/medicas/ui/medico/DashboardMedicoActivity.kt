package com.citas.medicas.ui.medico

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.citas.medicas.R
import com.citas.medicas.databinding.ActivityDashboardMedicoBinding
import com.citas.medicas.ui.auth.LoginActivity
import com.citas.medicas.utils.SessionDialogHelper
import com.citas.medicas.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardMedicoBinding

    private var dialogSalir: androidx.appcompat.app.AlertDialog? = null

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
                if (dialogSalir?.isShowing == true) return@setOnClickListener

                dialogSalir = MaterialAlertDialogBuilder(this@DashboardMedicoActivity)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Está seguro de que desea salir del sistema?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ ->
                        btnSalir.isEnabled = false
                        SessionDialogHelper.ejecutarLogoutCompleto(this@DashboardMedicoActivity)
                    }
                    .show()
            }
        }
    }

    // Función genérica para cambiar fragmentos
    private fun replaceFragment(fragment: Fragment) {
        // Buscar el fragmento que actualmente está pintado en el contenedor
        val fragmentActual = supportFragmentManager.findFragmentById(R.id.containerFragment)

        // Si el fragmento actual es de la misma clase que el que queremos abrir, ignoramos el click
        if (fragmentActual != null && fragmentActual::class.java == fragment::class.java) {
            return
        }
        listaTabs.forEach { it.isClickable = false }

        supportFragmentManager.beginTransaction()
            .replace(R.id.containerFragment, fragment)
            .commit()
        binding.root.postDelayed({
            listaTabs.forEach { it.isClickable = true }
        }, 300)
    }

    // Cambia el estado .isSelected. Android se encarga de pintar el fondo con el XML
    private fun seleccionarTabVisual(tabSeleccionado: LinearLayout) {
        listaTabs.forEach { tab ->
            tab.isSelected = (tab == tabSeleccionado)
        }
    }
}