package com.citas.medicas.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.databinding.ActivityLoginBinding
import com.citas.medicas.models.ErrorResponse
import com.citas.medicas.models.LoginRequest
import com.citas.medicas.ui.admin.DashboardAdminActivity
import com.citas.medicas.ui.medico.DashboardMedicoActivity
import com.citas.medicas.ui.paciente.HomePacienteActivity
import com.citas.medicas.utils.RolesUsuario
import com.google.gson.Gson
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Limpieza de caché previa para desarrollo
        //getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE).edit().clear().apply()

        // Configuración inicial de la UI genérica
        binding.tvIdentificador.text = "Identificador (N° Afiliado, JVPM o Correo)"
        binding.tvIrARegistro.visibility = View.VISIBLE

        setupListeners()
    }

    private fun setupListeners() {
        binding.tvIrARegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        // Detecta dinámicamente si escribe un correo para alterar la UI y mostrar el campo secreto
        /*binding.etAfiliado.doAfterTextChanged { text ->
            val input = text.toString().trim()
            if (input.contains("@") && input.contains(".")) {
                binding.tvLabelSecreto.visibility = View.VISIBLE
                binding.etFraseSecreta.visibility = View.VISIBLE
                binding.tvIrARegistro.visibility = View.GONE
            } else {
                binding.tvLabelSecreto.visibility = View.GONE
                binding.etFraseSecreta.visibility = View.GONE
                binding.tvIrARegistro.visibility = View.VISIBLE
                // Limpiamos el campo si el usuario borra el correo para evitar envíos accidentales
                binding.etFraseSecreta.text?.clear()
            }
        }*/

        binding.btnLogin.setOnClickListener {
            val identificador = binding.etAfiliado.text.toString().trim()
            val pass = binding.etClave.text.toString()

            if (identificador.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                ejecutarLoginUnificado(identificador, pass)
            }
        }
    }

    private fun ejecutarLoginUnificado(usuario: String, clave: String) {
        // Captura de la frase secreta desde la UI si el campo está visible y lleno
        //val fraseSecreta = binding.etFraseSecreta.text.toString().trim().takeIf { it.isNotEmpty() }

        // Mapeo Dinámico del Request con la integración de la frase secreta
        val request = when {
            usuario.contains("@") -> {
                // Validación local opcional: Evita enviar la petición si el administrador no digita la frase obligatoria
                /*if (fraseSecreta == null) {
                    Toast.makeText(this, "La frase secreta es obligatoria para Administradores", Toast.LENGTH_SHORT).show()
                    return
                }*/
                LoginRequest(
                    password = clave,
                    rolId = RolesUsuario.ID_ADMIN,
                    email = usuario,
                )
            }

            usuario.length == 9 ->
                LoginRequest(
                    password = clave,
                    rolId = RolesUsuario.ID_PACIENTE,
                    numAfiliado = usuario
                )

            else ->
                LoginRequest(
                    password = clave,
                    rolId = RolesUsuario.ID_MEDICO,
                    numJvpm = usuario
                )
        }

        // RESPALDO LOCAL: Calculamos el rol deducido en el Front-End
        val rolDeducidoPorFront = when {
            usuario.contains("@") -> RolesUsuario.ID_ADMIN
            usuario.length == 9 -> RolesUsuario.ID_PACIENTE
            else -> RolesUsuario.ID_MEDICO
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@LoginActivity).loginUsuario(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()!!
                    val user = loginResponse.data
                    val tokenExtraido = user?.token

                    // AJUSTE TÉCNICO EVASIVO
                    val serverRol = user?.rolId ?: 0
                    val rolIdAsignado = if (serverRol > 0) serverRol else rolDeducidoPorFront

                    // --- PERSISTENCIA DE DATOS ---
                    val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                    val exitoEscritura = prefs.edit().apply {
                        putString("user_usuarioid", user?.id ?: "")
                        putString("user_nombre", user?.nombre ?: "")
                        putString("user_apellido", user?.apellido ?: "")
                        putString("user_afiliado", user?.numAfiliado ?: binding.etAfiliado.text.toString())
                        putString("user_dui", user?.dui ?: "")
                        putString("user_email", user?.email ?: "")
                        putString("user_telefono", user?.telefono ?: "")

                        // Campos clínicos del paciente
                        putString("user_alergias", user?.alergias ?: "Ninguna registrada")
                        putString("user_cronicas", user?.condicionesCronicas ?: "Ninguna registrada")
                        putString("user_medicinas", user?.medicamentosRecurrentes ?: "Ninguna")
                        putString("user_sangre", user?.tipoSangre ?: "No especificado")

                        // Campos de perfil adicionales
                        putString("user_genero", user?.genero ?: "M")
                        putString("user_fechanacimiento", user?.fechaNacimiento ?: "")
                        putString("user_estadofamiliar", user?.estadoFamiliar ?: "No especificado")

                        putInt("user_rolid", rolIdAsignado)
                        putString("token_jwt", tokenExtraido)
                    }.commit()

                    if (!exitoEscritura) {
                        Log.e("LOGIN_ERROR", "Error crítico escribiendo en SharedPreferences")
                    }

                    val nombreUsuario = user?.nombre ?: "Usuario"

                    Log.d("LOGIN_DEBUG_FINAL", "JSON en crudo del usuario: ${Gson().toJson(user)}")
                    Log.d("LOGIN_DEBUG_FINAL", "rolIdAsignado final utilizado: $rolIdAsignado")

                    // Redirección dinámica según el rol final asignado
                    when (rolIdAsignado) {
                        RolesUsuario.ID_PACIENTE -> navegarA(HomePacienteActivity::class.java, rolIdAsignado, "Bienvenido $nombreUsuario")
                        RolesUsuario.ID_MEDICO -> navegarA(DashboardMedicoActivity::class.java, rolIdAsignado, "Bienvenido Dr. $nombreUsuario")
                        RolesUsuario.ID_ADMIN -> navegarA(DashboardAdminActivity::class.java, rolIdAsignado, "Panel Administración")
                        else -> Toast.makeText(this@LoginActivity, "Rol no reconocido por el sistema ($rolIdAsignado)", Toast.LENGTH_SHORT).show()
                    }
                    finish()

                } else {
                    val errorJson = response.errorBody()?.string()
                    val mensajeParaMostrar = try {
                        val parsedError = Gson().fromJson(errorJson, ErrorResponse::class.java)
                        parsedError.message
                    } catch (e: Exception) {
                        "Credenciales incorrectas"
                    }
                    Toast.makeText(this@LoginActivity, mensajeParaMostrar, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("LOGIN_ERROR", "Fallo: ${e.message}")
                Toast.makeText(this@LoginActivity, "Error de red: Verifique su conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navegarA(destino: Class<*>, rol: Int, mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, destino).apply {
            putExtra("rol", rol)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}