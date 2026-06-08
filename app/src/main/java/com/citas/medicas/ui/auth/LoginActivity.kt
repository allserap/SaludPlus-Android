package com.citas.medicas.ui.auth

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager // 1. Importación necesaria
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

        verificarSesionGuardada()

        binding.tvIrARegistro.visibility = View.VISIBLE

        setupListeners()
        setupOcultarTecladoAlTocarFondo() // 2. Inicializar detector de toques en el fondo
    }

    // --- FUNCIÓN UTILITARIA GLOBAL PARA OCULTAR EL TECLADO ---
    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus() // Quita la línea de selección activa del EditText
        }
    }

    // 3. Permite ocultar el teclado si el usuario toca cualquier espacio vacío del layout raíz
    private fun setupOcultarTecladoAlTocarFondo() {
        binding.root.setOnClickListener {
            ocultarTeclado()
        }
    }

    private fun verificarSesionGuardada() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
        val rolGuardado = prefs.getInt("user_rolid", -1)

        if (rolGuardado == -1) return

        if (!hayConexionInternet()) {
            Toast.makeText(this, "Modo sin conexión. Entrando con sesión guardada...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bienvenido de nuevo", Toast.LENGTH_SHORT).show()
        }

        redireccionarSegunRol(rolGuardado)
    }

    private fun redireccionarSegunRol(rolId: Int) {
        when (rolId) {
            RolesUsuario.ID_PACIENTE -> {
                startActivity(Intent(this, HomePacienteActivity::class.java))
                finish()
            }
            RolesUsuario.ID_MEDICO -> {
                startActivity(Intent(this, DashboardMedicoActivity::class.java))
                finish()
            }
            RolesUsuario.ID_ADMIN -> {
                startActivity(Intent(this, DashboardAdminActivity::class.java))
                finish()
            }
        }
    }

    private fun hayConexionInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun setupListeners() {
        binding.tvIrARegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val identificador = binding.etAfiliado.text.toString().trim()
            val pass = binding.etClave.text.toString()

            if (identificador.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                ocultarTeclado() // 4. Ocultamos el teclado inmediatamente al presionar ingresar
                binding.btnLogin.isEnabled = false
                binding.btnLogin.text = "Cargando..."
                ejecutarLoginUnificado(identificador, pass)
            }
        }
    }

    private fun ejecutarLoginUnificado(usuario: String, clave: String) {
        val request = when {
            usuario.contains("@") -> {
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

                    val serverRol = user?.rolId ?: 0
                    val rolIdAsignado = if (serverRol > 0) serverRol else rolDeducidoPorFront

                    val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                    val exitoEscritura = prefs.edit().apply {
                        putString("user_usuarioid", user?.id ?: "")
                        putString("user_nombre", user?.nombre ?: "")
                        putString("user_apellido", user?.apellido ?: "")
                        putString("user_afiliado", user?.numAfiliado ?: binding.etAfiliado.text.toString())
                        putString("user_dui", user?.dui ?: "")
                        putString("user_email", user?.email ?: "")
                        putString("user_telefono", user?.telefono ?: "")

                        putString("user_alergias", user?.alergias ?: "Ninguna registrada")
                        putString("user_cronicas", user?.condicionesCronicas ?: "Ninguna registrada")
                        putString("user_medicinas", user?.medicamentosRecurrentes ?: "Ninguna")
                        putString("user_sangre", user?.tipoSangre ?: "No especificado")

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

                    when (rolIdAsignado) {
                        RolesUsuario.ID_PACIENTE -> navegarA(HomePacienteActivity::class.java, rolIdAsignado, "Bienvenido $nombreUsuario")
                        RolesUsuario.ID_MEDICO -> navegarA(DashboardMedicoActivity::class.java, rolIdAsignado, "Bienvenido Dr. $nombreUsuario")
                        RolesUsuario.ID_ADMIN -> navegarA(DashboardAdminActivity::class.java, rolIdAsignado, "Panel Administración")
                        else -> Toast.makeText(this@LoginActivity, "Rol no reconocido por el sistema ($rolIdAsignado)", Toast.LENGTH_SHORT).show()
                    }
                    finish()

                } else {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Ingresar"
                    val mensajeParaMostrar = when (response.code()) {
                        403 -> "Esta cuenta se encuentra inactiva. Por favor, contacta al administrador."
                        401 -> "Correo, código o contraseña incorrectos."
                        else -> {
                            try {
                                val errorJson = response.errorBody()?.string()
                                Gson().fromJson(errorJson, ErrorResponse::class.java).message
                            } catch (e: Exception) {
                                "Error al iniciar sesión (${response.code()})"
                            }
                        }
                    }
                    Toast.makeText(this@LoginActivity, mensajeParaMostrar, Toast.LENGTH_LONG).show()
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