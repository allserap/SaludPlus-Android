package com.citas.medicas.ui.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.citas.medicas.databinding.ActivityRegistroBinding
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.RolResponse
import com.citas.medicas.utils.RolesUsuario
import com.citas.medicas.utils.Validation.isValidEmail
import com.citas.medicas.utils.Validation.isValidPhone
import com.citas.medicas.utils.aplicarMascaraDUI
import com.citas.medicas.utils.aplicarMascaraTelefono
import com.citas.medicas.utils.configurarConHint
import com.citas.medicas.utils.limpiarCampos
import com.citas.medicas.utils.showDatePickerDialog
import java.util.*

interface Reseteable {
    fun resetearInterfaz()
}
class RegistroActivity : AppCompatActivity(),  Reseteable {

    private lateinit var binding: ActivityRegistroBinding

    // Inicialización del ViewModel
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar el binding
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        // Uso de binding.root en lugar de R.layout
        setContentView(binding.root)

        //feedback durante el registro
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCrearCuenta.isEnabled = !isLoading

            binding.btnCrearCuenta.text = if (isLoading) "Procesando..." else "Crear Cuenta"
        }

        setupObservers()
        setupCatalogosObservers()
        setupListeners()

        authViewModel.cargarCatalogos()
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCrearCuenta.isEnabled = !isLoading
            binding.btnCrearCuenta.text = if (isLoading) "Procesando..." else "Crear Cuenta"
        }

        authViewModel.formState.observe(this) { estado ->
            with(binding) {
                etNombreR.error = estado.nombreError
                etApellidoR.error = estado.apellidoError
                etDuiR.error = estado.duiError
                etCorreoR.error = estado.correoError
                etTelefonoR.error = estado.telefonoError
                etClaveR.error = estado.passwordError
                etAfiliadoR.error = estado.afiliadoError

                // Validar visualmente los spinners si fallan
                if (estado.isValid) {
                    enviarRegistroAlServidor()
                }
            }
        }

        authViewModel.registroExitoso.observe(this) { mensaje ->
            if (mensaje != null) {
                Toast.makeText(this, "¡Cuenta creada con éxito!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        authViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCatalogosObservers() {
        val opcionesGenero = arrayOf("Masculino", "Femenino", "Otro")
        binding.spGeneroR.configurarConHint(opcionesGenero, "Seleccione su género")

        val opcionesEstado = arrayOf("Soltero/a", "Casado/a", "Divorciado/a", "Viudo/a")
        binding.spEstadoFamiliarR.configurarConHint(opcionesEstado, "Seleccione estado familiar")
    }

        private fun setupListeners() {
            binding.etTelefonoR.aplicarMascaraTelefono()
            binding.etDuiR.aplicarMascaraDUI()
            binding.etFechaR.setOnClickListener {
                showDatePickerDialog(this, binding.etFechaR)
            }

            // Volver al login
            binding.tvVolverALogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Cerrar registro para no acumular pantallas
            }

            binding.btnCrearCuenta.setOnClickListener {
                authViewModel.validarFormulario(
                    rolId = RolesUsuario.ID_PACIENTE,
                    nombres = binding.etNombreR.text.toString(),
                    apellidos = binding.etApellidoR.text.toString(),
                    dui = binding.etDuiR.text.toString(),
                    correo = binding.etCorreoR.text.toString(),
                    telefono = binding.etTelefonoR.text.toString(),
                    password = binding.etClaveR.text.toString(),
                    extraCampo = binding.etAfiliadoR.text.toString(),
                    especialidadPos = 1, // 1 para que no de error
                    unidadPos = 1
                )
            }
        }
    override fun resetearInterfaz() {
        // limpiarCampos del BaseFragment pasando todos los EditTexts
        limpiarCampos(
            binding.etNombreR, binding.etApellidoR, binding.etDuiR,
            binding.etFechaR, binding.etCorreoR, binding.etTelefonoR,
            binding.etAfiliadoR, binding.etClaveR
        )
        // Resetear Spinner
        binding.spGeneroR.setSelection(0)
    }

    private fun enviarRegistroAlServidor() {
        val generoSeleccionado = binding.spGeneroR.selectedItem.toString()
        val generoFinal = when(generoSeleccionado) {
            "Masculino" -> "M"
            "Femenino" -> "F"
            else -> "O"
        }
        // Preparar los datos
        val nuevoUsuario = RegistroRequest(
            nombre = binding.etNombreR.text.toString().trim(),
            apellido = binding.etApellidoR.text.toString().trim(),
            dui = binding.etDuiR.text.toString().trim(),
            email = binding.etCorreoR.text.toString().trim(),
            password = binding.etClaveR.text.toString(),
            telefono = binding.etTelefonoR.text.toString().trim(),
            fechaNacimiento = binding.etFechaR.text.toString(),
            numAfiliado = binding.etAfiliadoR.text.toString().trim(),
            genero = generoFinal,
            estadoFamiliar = binding.spEstadoFamiliarR.selectedItem.toString(),
            rol = RolesUsuario.ID_PACIENTE
        )
        authViewModel.ejecutarRegistro(nuevoUsuario)
    }
}