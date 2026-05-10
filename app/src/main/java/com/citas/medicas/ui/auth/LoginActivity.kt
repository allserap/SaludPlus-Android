package com.citas.medicas.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
    private var idRol: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //inicializar binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Recuperar rol del splash
        idRol = intent.getIntExtra("rol", -1)
        Log.d("LOGIN_DEBUG", "Rol recibido del Splash: $idRol")

        if (idRol == -1) {
            Toast.makeText(this, "Error al recuperar el rol", Toast.LENGTH_SHORT).show()
            finish() // Regresa al Splash si no hay rol
        }

        interfazPorRol()
        setupListeners()

    }

    private fun setupListeners() {
        binding.tvIrARegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val numAfiliado = binding.etAfiliado.text.toString()
            val pass = binding.etClave.text.toString()

            if (numAfiliado.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }else{
                ejecutarLogin(numAfiliado, pass)
            }
        }
    }

    //Login segun rol
    private fun interfazPorRol(){
        when(idRol){
            RolesUsuario.ID_PACIENTE -> binding.tvIdentificador.text = "Número de Afiliado"
            RolesUsuario.ID_MEDICO -> binding.tvIdentificador.text = "JVPM"
            else -> binding.tvIdentificador.text = "Identificador"
        }
    }

    private fun ejecutarLogin(usuario: String, clave: String) {
        val request = when (idRol) {
            RolesUsuario.ID_PACIENTE -> LoginRequest(numAfiliado = usuario, password = clave, rolId = idRol)
            RolesUsuario.ID_MEDICO -> LoginRequest(numJvpm = usuario, password = clave, rolId = idRol)
            RolesUsuario.ID_ADMIN -> LoginRequest(email = usuario, password = clave, rolId = idRol)
            else -> LoginRequest(password = clave, rolId = idRol)
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@LoginActivity).loginUsuario(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()!!
                    val user = loginResponse.data
                    val tokenExtraido = user?.token

                    // --- PERSISTENCIA DE DATOS ---
                    val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                    with(prefs.edit()) {
                        putString("user_usuarioid", user?.id)
                        putString("user_nombre", user?.nombre)
                        putString("user_apellido", user?.apellido)
                        putString("user_afiliado", user?.numAfiliado)
                        putInt("user_id_rol",  idRol)
                        putString("token_jwt", tokenExtraido)
                        apply()
                    }

                    val nombreUsuario = user?.nombre ?: "Usuario"

                        when (idRol) {
                            RolesUsuario.ID_PACIENTE -> navegarA(HomePacienteActivity::class.java, "Bienvenido $nombreUsuario")
                            RolesUsuario.ID_MEDICO -> navegarA(DashboardMedicoActivity::class.java, "Bienvenido Dr. $nombreUsuario")
                            RolesUsuario.ID_ADMIN -> navegarA(DashboardAdminActivity::class.java, "Panel Administración")
                            else -> Toast.makeText(this@LoginActivity, "Rol no reconocido", Toast.LENGTH_SHORT).show()
                        }
                    finish()
                    } else {
                    // Obtener error como String
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

    // Función auxiliar
    private fun navegarA(destino: Class<*>, mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, destino).apply {
            // "matar" el Login para que no se pueda volver atrás
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

}