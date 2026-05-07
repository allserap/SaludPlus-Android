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
            binding.tabAgenda.setBackgroundResource(R.drawable.bg_tabs)
        }

        setupNavigation()
    }
            private fun setupNavigation() {
                with(binding) {
                    tabAgenda.setOnClickListener {
                        replaceFragment(AgendaFragment())
                    }
                    tabHistorial.setOnClickListener {
                        replaceFragment(HistorialFragment())
                    }
                    tabRecetas.setOnClickListener {
                        replaceFragment(RecetasFragment())
                    }
                    tabPerfil.setOnClickListener {
                        replaceFragment(PerfilMedicoFragment())
                    }
                    btnSalir.setOnClickListener {
                        MaterialAlertDialogBuilder(this@DashboardMedicoActivity)
                            .setTitle("Cerrar Sesión")
                            .setMessage("¿Está seguro de que desea salir del sistema?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Salir") { _, _ ->
                                SessionManager.logout(this@DashboardMedicoActivity)
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
}