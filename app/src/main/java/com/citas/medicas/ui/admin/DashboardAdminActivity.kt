package com.citas.medicas.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.citas.medicas.R
import com.citas.medicas.databinding.ActivityDashboardAdminBinding
import com.citas.medicas.ui.auth.LoginActivity
import com.citas.medicas.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private var dialogSalir: androidx.appcompat.app.AlertDialog? = null

    // Lista para iterar los tabs fácilmente usando View Binding
    private lateinit var listaTabs: List<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el binding
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Agrupamos las vistas de los tabs usando binding después de inflar la vista
        listaTabs = listOf(binding.tabUsuarios, binding.tabEstadisticas, binding.tabGestiones)

        // Inicializar con el fragmento y tab por defecto
        if (savedInstanceState == null) {
            replaceFragment(UsuariosFragment())
            seleccionarTabVisual(binding.tabUsuarios)
        }

        cargarDatosHeader()
        setupNavigation()
    }

    private fun cargarDatosHeader() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        // Recuperar valores reales del Administrador
        val nombre = prefs.getString("user_nombre", "") ?: ""
        val apellido = prefs.getString("user_apellido", "") ?: ""

        // Editar la UI usando View Binding
        with(binding) {
            tvHeaderRol.text = "Administrador del Sistema"
            tvHeaderNombre.text = "$nombre $apellido".trim().ifEmpty { "Administrador" }
        }
    }

    private fun setupNavigation() {
        with(binding) {
            // Click de pestaña Usuarios
            tabUsuarios.setOnClickListener {
                replaceFragment(UsuariosFragment())
                seleccionarTabVisual(tabUsuarios)
            }

            // Click de pestaña Estadísticas
            tabEstadisticas.setOnClickListener {
                // Asegúrate de que este fragmento exista con este nombre exacto
                replaceFragment(EstadisticasFragment())
                seleccionarTabVisual(tabEstadisticas)
            }

            // Click de pestaña Gestiones
            tabGestiones.setOnClickListener {
                // Asegúrate de que este fragmento exista con este nombre exacto
                replaceFragment(GestionesFragment())
                seleccionarTabVisual(tabGestiones)
            }

            // Botón Salir
            btnSalir.setOnClickListener {
                if (dialogSalir?.isShowing == true) return@setOnClickListener
                dialogSalir = MaterialAlertDialogBuilder(this@DashboardAdminActivity)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Está seguro de que desea salir del sistema?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ ->
                        btnSalir.isEnabled = false
                        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                        prefs.edit().clear().apply()
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

    // Función encargada del intercambio de fragmentos
    private fun replaceFragment(fragment: Fragment) {
        // Buscamos el fragmento que se encuentra cargado actualmente
        val fragmentActual = supportFragmentManager.findFragmentById(R.id.containerFragment)

        // Si el tipo de fragmento es exactamente igual al que se quiere abrir, abortamos la operación
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

    // Función optimizada: Cambia el estado de la pestaña seleccionada.
    // Al pasar a "true", el XML (bg_tab_item) cambia el color de fondo automáticamente.
    private fun seleccionarTabVisual(tabSeleccionado: LinearLayout) {
        listaTabs.forEach { tab ->
            tab.isSelected = (tab == tabSeleccionado)
        }
    }
}