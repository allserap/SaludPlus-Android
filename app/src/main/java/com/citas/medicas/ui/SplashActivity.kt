package com.citas.medicas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.citas.medicas.R
import com.citas.medicas.ui.admin.DashboardAdminActivity
import com.citas.medicas.ui.auth.LoginActivity
import com.citas.medicas.ui.auth.RegistroActivity
import com.citas.medicas.ui.medico.DashboardMedicoActivity
import com.citas.medicas.ui.paciente.HomePacienteActivity
import com.citas.medicas.utils.RolesUsuario

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si hay sesión activa persistida físicamente
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
        val token = prefs.getString("token_jwt", null)

        val rolGuardado = prefs.getInt("user_rolid", -1)

        if (token != null && rolGuardado != -1) {
            val intent = when (rolGuardado) {
                RolesUsuario.ID_PACIENTE -> Intent(this, HomePacienteActivity::class.java)
                RolesUsuario.ID_MEDICO -> Intent(this, DashboardMedicoActivity::class.java)
                RolesUsuario.ID_ADMIN -> Intent(this, DashboardAdminActivity::class.java)
                else -> Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
            return
        }

        // Si no hay sesión válida, inflamos la vista base del Splash
        setContentView(R.layout.activity_splash)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Flujo Unificado: Va directo al Login sin diálogos emergentes ni clicks largos ocultos
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Destruye el Splash para liberar memoria
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}