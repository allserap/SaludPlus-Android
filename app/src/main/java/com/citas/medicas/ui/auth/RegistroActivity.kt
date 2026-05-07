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
import com.citas.medicas.utils.RolesUsuario
import java.util.*

class RegistroActivity : AppCompatActivity() {

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
        setupListeners()
        configurarSpinners()
    }

        private fun setupListeners() {
            // Configuración del DatePicker para la fecha de nacimiento
            binding.etFechaR.setOnClickListener { showDatePickerDialog(binding.etFechaR) }

            // Volver al login
            binding.tvVolverALogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Cerrar registro para no acumular pantallas
            }

            binding.btnCrearCuenta.setOnClickListener {
                if (validarForm()) {
                    enviarRegistroAlServidor()
                }
            }
        }

    private fun enviarRegistroAlServidor() {
        //recuperar inicial del género
        if (binding.spGeneroR.selectedItemPosition == 0 || binding.spEstadoFamiliarR.selectedItemPosition == 0) {
            Toast.makeText(this, "Por favor seleccione todas las opciones", Toast.LENGTH_SHORT).show()
            return
        }

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
            rol = RolesUsuario.PACIENTE
        )
        authViewModel.ejecutarRegistro(nuevoUsuario)
    }
    private fun setupObservers() {
        // Escuchar el éxito
        authViewModel.registroExitoso.observe(this) { mensaje ->
            Toast.makeText(this, mensaje ?: "¡Cuenta creada con éxito!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Escuchar el error
        authViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
    // Validar los datos ingrsados por el usuario
    fun validarForm(): Boolean {

        with(binding) {
            val afiliado = etAfiliadoR.text.toString().trim()
            val nombre = etNombreR.text.toString().trim()
            val apellido = etApellidoR.text.toString().trim()
            val dui = etDuiR.text.toString().trim()
            val fecha = etFechaR.text.toString().trim()
            val correo = etCorreoR.text.toString().trim()
            val telefono = etTelefonoR.text.toString().trim()
            val password = etClaveR.text.toString()
            val confirmPassword = etConfirmarClaveR.text.toString()


            var isValid = true

            if (afiliado.length !in 6..10 || !afiliado.all { it.isDigit() }) {
                etAfiliadoR.error = "Número inválido (6-10 dígitos)"
                isValid = false
            }

            if (nombre.isEmpty()) {
                etNombreR.error = "Ingrese su nombre"
                isValid = false
            }

            if (apellido.isEmpty()) {
                etApellidoR.error = "Ingrese su apellido"
                isValid = false
            }

            if (!isValidDUI(dui)) {
                etDuiR.error = "Formato inválido (00000000-0)"
                isValid = false
            }

            if (fecha.isEmpty()) {
                etFechaR.error = "Seleccione su fecha"
                isValid = false
            }

            if (!isValidEmail(correo)) {
                etCorreoR.error = "Correo inválido"
                isValid = false
            }

            if (!isValidPhone(telefono)) {
                etTelefonoR.error = "Formato inválido (0000-0000)"
                isValid = false
            }

            if (!isValidPassword(password)) {
                etClaveR.error = "Mínimo 8 caracteres, 1 mayúscula y 1 símbolo"
                isValid = false
            }

            if (password != confirmPassword) {
                etConfirmarClaveR.error = "Las contraseñas no coinciden"
                isValid = false
            }


            return isValid
        }
    }

    fun isValidPassword(password: String): Boolean {
        // 6 caracteres, una mayúscula y un símbolo
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{6,}$")
        return password.matches(passwordRegex)
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidDUI(dui: String): Boolean {
        val regex = Regex("^\\d{8}-\\d$")
        return dui.matches(regex)
    }

    fun isValidPhone(phone: String): Boolean {
        val regex = Regex("^\\d{4}-\\d{4}$")
        return phone.matches(regex)
    }

    private fun configurarSpinners() {
        val opcionesGenero = arrayOf("Masculino", "Femenino", "Otro")
        configurarSpinnerConHint(binding.spGeneroR, opcionesGenero, "Seleccione su género")

        val opcionesEstado = arrayOf("Soltero/a", "Casado/a", "Divorciado/a", "Viudo/a")
        configurarSpinnerConHint(binding.spEstadoFamiliarR, opcionesEstado, "Seleccione estado familiar")
    }

    private fun configurarSpinnerConHint(spinner: Spinner, opciones: Array<String>, hint: String) {
        val listaConHint = arrayOf(hint) + opciones

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaConHint) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }
                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0 // Deshabilita la primera posición
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val realMonth = selectedMonth + 1
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, realMonth, selectedDay)
            editText.setText(formattedDate)
            // Limpiar el error si ya seleccionó fecha
            editText.error = null
        }, year, month, day)

        datePicker.show()
    }
}