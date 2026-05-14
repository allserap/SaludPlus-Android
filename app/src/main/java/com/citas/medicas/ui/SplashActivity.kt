package com.citas.medicas.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.citas.medicas.R
import com.citas.medicas.ui.admin.DashboardAdminActivity
import com.citas.medicas.ui.auth.LoginActivity
import com.citas.medicas.ui.auth.RegistroActivity
import com.citas.medicas.ui.medico.DashboardMedicoActivity
import com.citas.medicas.ui.paciente.HomePacienteActivity
import com.citas.medicas.ui.paciente.SolicitarCitaActivity
import com.citas.medicas.utils.RolesUsuario
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // verificar si hay una sesión activa
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
        val token = prefs.getString("token_jwt", null)
        val rolGuardado = prefs.getInt("user_id_rol", -1)

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
        if (token == null && rolGuardado != -1) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("rol", rolGuardado)
            startActivity(intent)
            finish()
            return
        }
        setContentView(R.layout.activity_splash)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val ivLogo = findViewById<ImageView>(R.id.ivLogo)

        btnLogin.setOnClickListener {
            val roles = arrayOf("Paciente", "Medico")

            MaterialAlertDialogBuilder(this)
                .setTitle("Ingresar como: ")
                .setItems(roles){ _, which ->
                    //Map de la seleccion
                    val rolSeleccionado = if (which == 0) RolesUsuario.ID_PACIENTE else RolesUsuario.ID_MEDICO

                    Log.d("SPLASH_DEBUG", "Rol seleccionado: $rolSeleccionado")

                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    intent.putExtra("rol", rolSeleccionado)
                    startActivity(intent)
                }
                .show()
        }

        ivLogo.setOnLongClickListener {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(100)
            }

            // Redireccionamiento discreto
            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtra("rol", RolesUsuario.ID_ADMIN)
                putExtra("is_admin_flow", true)
            }
            startActivity(intent)
            true // Indica que el evento fue consumido
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}