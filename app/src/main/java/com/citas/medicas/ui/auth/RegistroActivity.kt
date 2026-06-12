package com.citas.medicas.ui.auth

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager // Importación para el teclado
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

class RegistroActivity : AppCompatActivity(), Reseteable {

    private lateinit var binding: ActivityRegistroBinding

    // Inicialización del ViewModel
    private val authViewModel: AuthViewModel by viewModels()

    // Forzar idioma Español globalmente en la Activity heredable por el DatePicker
    override fun attachBaseContext(newBase: Context) {
        val localeEspanol = Locale("es", "ES")
        Locale.setDefault(localeEspanol)

        val config = newBase.resources.configuration
        config.setLocale(localeEspanol)

        val contextoEspanol = newBase.createConfigurationContext(config)
        super.attachBaseContext(contextoEspanol)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar el binding
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Feedback durante el registro
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCrearCuenta.isEnabled = !isLoading
            binding.btnCrearCuenta.text = if (isLoading) "Procesando..." else "Crear Cuenta"
        }

        setupObservers()
        setupCatalogosObservers()
        setupListeners()
        setupOcultarTecladoAlTocarFondo()
        authViewModel.cargarCatalogos()
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus() // Quita la selección activa del campo
        }
    }

    // Configuración del listener para capturar toques en el fondo del ScrollView
    private fun setupOcultarTecladoAlTocarFondo() {
        binding.containerForm.setOnClickListener {
            ocultarTeclado()
        }
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
                } else {
                    btnCrearCuenta.isEnabled = true
                    btnCrearCuenta.text = "Crear Cuenta"
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
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()

                if (errorMsg.contains("afiliado", ignoreCase = true)) {
                    binding.etAfiliadoR.requestFocus()
                }
            }
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

        // Control y restricción de fechas futuras nativas con DatePickerDialog directo
        binding.etFechaR.setOnClickListener {
            ocultarTeclado() // Ocultamos el teclado para que no tape el calendario

            val calendarioActual = Calendar.getInstance()
            val año = calendarioActual.get(Calendar.YEAR)
            val mes = calendarioActual.get(Calendar.MONTH)
            val dia = calendarioActual.get(Calendar.DAY_OF_MONTH)

            val picker = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                val mesFormateado = String.format("%02d", monthOfYear + 1)
                val diaFormateado = String.format("%02d", dayOfMonth)
                val fechaSeleccionada = "$year-$mesFormateado-$diaFormateado"

                val fechaSeleccionadaCal = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }

                if (fechaSeleccionadaCal.after(calendarioActual)) {
                    Toast.makeText(this, "La fecha de nacimiento no puede ser una fecha futura", Toast.LENGTH_LONG).show()
                } else {
                    binding.etFechaR.setText(fechaSeleccionada)
                    binding.etFechaR.error = null
                }
            }, año, mes, dia)

            // Bloquear visualmente los días posteriores al día de hoy
            picker.datePicker.maxDate = System.currentTimeMillis()
            picker.show()
        }

        // Volver al login
        binding.tvVolverALogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnCrearCuenta.setOnClickListener {
            val contrasena = binding.etClaveR.text.toString()
            val confirmarContrasena = binding.etConfirmarClaveR.text.toString()
            val numeroAfiliado = binding.etAfiliadoR.text.toString().trim()

            // Validación local para cerciorarse de que las claves coincidan
            if (contrasena != confirmarContrasena) {
                binding.etConfirmarClaveR.error = "Las contraseñas no coinciden"
                Toast.makeText(this, "Las contraseñas ingresadas deben ser iguales", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Frena la ejecución si falla
            } else {
                binding.etConfirmarClaveR.error = null
            }

            // Verificar que el número de afiliado tenga exactamente 9 dígitos numéricos
            if (!numeroAfiliado.matches(Regex("^\\d{9}$"))) {
                binding.etAfiliadoR.error = "El número de afiliado debe tener exactamente 9 dígitos numéricos"
                Toast.makeText(this, "Por favor, ingrese un número de afiliado válido (9 dígitos)", Toast.LENGTH_LONG).show()
                binding.etAfiliadoR.requestFocus()
                return@setOnClickListener
            } else {
                binding.etAfiliadoR.error = null
            }

            ocultarTeclado()
            binding.btnCrearCuenta.isEnabled = false
            binding.btnCrearCuenta.text = "Procesando..."

            authViewModel.validarFormulario(
                rolId = RolesUsuario.ID_PACIENTE,
                nombres = binding.etNombreR.text.toString(),
                apellidos = binding.etApellidoR.text.toString(),
                dui = binding.etDuiR.text.toString(),
                correo = binding.etCorreoR.text.toString(),
                telefono = binding.etTelefonoR.text.toString(),
                password = binding.etClaveR.text.toString(),
                extraCampo = binding.etAfiliadoR.text.toString(),
                especialidadPos = 1, // 1 para evitar errores de validación interna
                unidadPos = 1
            )
        }
    }

    override fun resetearInterfaz() {
        limpiarCampos(
            binding.etNombreR, binding.etApellidoR, binding.etDuiR,
            binding.etFechaR, binding.etCorreoR, binding.etTelefonoR,
            binding.etAfiliadoR, binding.etClaveR
        )
        binding.spGeneroR.setSelection(0)
    }

    private fun enviarRegistroAlServidor() {
        val generoSeleccionado = binding.spGeneroR.selectedItem.toString()
        val generoFinal = when(generoSeleccionado) {
            "Masculino" -> "M"
            "Femenino" -> "F"
            else -> "O"
        }

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