package com.citas.medicas.ui.paciente

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.PacienteUpdateRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditarPerfilActivity : AppCompatActivity() {
    private var pacienteId: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil)

        val btnAtras = findViewById<TextView>(R.id.tvBackEdit)
        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardarPerfil)

        val etTelefono = findViewById<TextInputEditText>(R.id.etEditTelefono)
        val etAlergias = findViewById<TextInputEditText>(R.id.etEditAlergias)
        val etCronicas = findViewById<TextInputEditText>(R.id.etEditCronicas)
        val etMedicinas = findViewById<TextInputEditText>(R.id.etEditMedicinas)


        val telefonoRecibido = intent.getStringExtra("EXTRA_TELEFONO") ?: ""
        val alergiasRecibidas = intent.getStringExtra("EXTRA_ALERGIAS") ?: ""
        val cronicasRecibidas = intent.getStringExtra("EXTRA_CRONICAS") ?: ""
        val medicinasRecibidas = intent.getStringExtra("EXTRA_MEDICINAS") ?: "" // Asegúrate de enviarlo desde PerfilActivity

        etTelefono.setText(telefonoRecibido)
        etAlergias.setText(alergiasRecibidas)
        etCronicas.setText(cronicasRecibidas)
        etMedicinas.setText(medicinasRecibidas)

        btnAtras.setOnClickListener {
            finish()
        }

        btnGuardar.setOnClickListener {
            val tel = etTelefono.text.toString().trim()
            val alg = etAlergias.text.toString().trim()
            val cro = etCronicas.text.toString().trim()
            val med = etMedicinas.text.toString().trim()

            if (tel.isEmpty()) {
                Toast.makeText(this, "El teléfono es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarPerfilEnServidor(tel, alg, cro, med, btnGuardar)
        }
    }


    private fun mostrarDialogoEditarContacto() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_contacto_paciente, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEditarEmail)
        val etTelefono = dialogView.findViewById<TextInputEditText>(R.id.etEditarTelefono)
        val btnGuardar = dialogView.findViewById<MaterialButton>(R.id.btnGuardarContacto)

        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
        etEmail.setText(prefs.getString("user_email", ""))
        etTelefono.setText(prefs.getString("user_telefono", ""))

        btnGuardar.setOnClickListener {
            val nuevoEmail = etEmail.text.toString().trim()
            val nuevoTelefono = etTelefono.text.toString().trim()

            if (nuevoEmail.isEmpty() || nuevoTelefono.isEmpty()) {
                Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateRequest = crearRequestCompleto(
                nuevoEmail = nuevoEmail,
                nuevoTelefono = nuevoTelefono
            )
            ejecutarActualizacionServidor(updateRequest, dialog, isPasswordChange = false)
        }
        dialog.show()
    }


    private fun mostrarDialogoCambiarClave() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cambiar_clave, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val txtClaveActual = dialogView.findViewById<TextInputEditText>(R.id.txtClaveActual)
        val txtClaveNueva = dialogView.findViewById<TextInputEditText>(R.id.txtClaveNueva)
        val btnActualizar = dialogView.findViewById<MaterialButton>(R.id.btnActualizarClave)

        btnActualizar.setOnClickListener {
            val claveActual = txtClaveActual.text.toString().trim()
            val claveNueva = txtClaveNueva.text.toString().trim()

            if (claveActual.isEmpty() || claveNueva.isEmpty()) {
                Toast.makeText(this, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateRequest = crearRequestCompleto(
                nuevaClave = claveNueva
            )
            ejecutarActualizacionServidor(updateRequest, dialog, isPasswordChange = true)
        }
        dialog.show()
    }



    private fun ejecutarActualizacionServidor(request: PacienteUpdateRequest, dialog: AlertDialog, isPasswordChange: Boolean) {
        val overlayCarga = findViewById<FrameLayout>(R.id.overlayCarga)

        overlayCarga.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@EditarPerfilActivity)
                val response = apiService.actualizarPaciente(pacienteId, request)

                if (response.isSuccessful) {
                    Toast.makeText(this@EditarPerfilActivity, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()

                    if (!isPasswordChange) {
                        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("user_email", request.email)
                            putString("user_telefono", request.telefono)
                            apply()
                        }

                        findViewById<TextView>(R.id.tvPerfilCorreo).text = request.email
                        findViewById<TextView>(R.id.tvPerfilTelefono).text = request.telefono
                    }
                    dialog.dismiss()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorCode = response.code()
                    Log.e("API_ERROR_BACKEND", "Código HTTP $errorCode - Detalle: $errorBody")

                    Toast.makeText(this@EditarPerfilActivity, "Error $errorCode al actualizar", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fallo de conexión", e)
                Toast.makeText(this@EditarPerfilActivity, "Error de red", Toast.LENGTH_LONG).show()
            } finally {
                overlayCarga.visibility = android.view.View.GONE
            }
        }
    }

    private fun crearRequestCompleto(
        nuevoEmail: String? = null,
        nuevoTelefono: String? = null,
        nuevaClave: String? = null
    ): PacienteUpdateRequest {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        val emailFinal = nuevoEmail ?: prefs.getString("user_email", "") ?: ""
        val telefonoFinal = nuevoTelefono ?: prefs.getString("user_telefono", "") ?: ""

        return PacienteUpdateRequest(
            id = pacienteId,
            nombre = prefs.getString("user_nombre", "") ?: "",
            apellido = prefs.getString("user_apellido", "") ?: "",
            dui = prefs.getString("user_dui", "") ?: "",
            email = emailFinal,
            password = nuevaClave,
            telefono = telefonoFinal,
            fechaNacimiento = prefs.getString("user_fechanacimiento", ""),
            genero = prefs.getString("user_genero", "M") ?: "M",
            rol = prefs.getInt("user_role", 1),
            activo = true,
            estadoFamiliar = prefs.getString("user_estadofamiliar", ""),
            numAfiliado = prefs.getString("user_afiliado", ""),
            tipoSangre = prefs.getString("user_sangre", ""),
            alergias = prefs.getString("user_alergias", ""),
            condicionesCronicas = prefs.getString("user_cronicas", ""),
            notaClinica = "",
            medicamentosRecurrentes = prefs.getString("user_medicinas", "")
        )
    }


    private fun guardarPerfilEnServidor(
        nuevoTelefono: String,
        nuevasAlergias: String,
        nuevasCronicas: String,
        nuevasMedicinas: String,
        btnGuardar: MaterialButton
    ) {
        val prefs = getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
        val usuarioId = prefs.getString("user_usuarioid", "") ?: ""

        val btnEditar = findViewById<MaterialButton>(R.id.btnEditarPerfil)
        btnEditar.setOnClickListener {

            val opciones = arrayOf("Actualizar Correo/Teléfono", "Cambiar Contraseña")
            AlertDialog.Builder(this)
                .setTitle("¿Qué deseas modificar?")
                .setItems(opciones) { _, cual ->
                    when (cual) {
                        0 -> mostrarDialogoEditarContacto()
                        1 -> mostrarDialogoCambiarClave()
                    }
                }
                .show()
        }

        if (usuarioId.isEmpty()) {
            Toast.makeText(this, "Error de sesión. Vuelve a ingresar.", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = prefs.getString("user_nombre", "") ?: ""
        val apellido = prefs.getString("user_apellido", "") ?: ""
        val dui = prefs.getString("user_dui", "") ?: ""
        val email = prefs.getString("user_email", "") ?: ""
        val afiliado = prefs.getString("user_afiliado", "") ?: ""
        val sangrePref = prefs.getString("user_sangre", "")
        val sangre = if (sangrePref == "No especificado" || sangrePref.isNullOrBlank()) null else sangrePref
        val genero = prefs.getString("user_genero", "M") ?: "M"
        val fechaNac = prefs.getString("user_fechanacimiento", "1990-01-01") ?: "1990-01-01"

        val request = PacienteUpdateRequest(
            id = usuarioId,
            nombre = nombre,
            apellido = apellido,
            dui = dui,
            email = email,
            password = null,
            telefono = nuevoTelefono,
            fechaNacimiento = fechaNac,
            genero = genero,
            rol = 1,
            activo = true,
            estadoFamiliar = "No especificado",
            numAfiliado = afiliado,
            tipoSangre = sangre,
            alergias = nuevasAlergias,
            condicionesCronicas = nuevasCronicas,
            notaClinica = "",
            medicamentosRecurrentes = nuevasMedicinas
        )

        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@EditarPerfilActivity).actualizarPaciente(usuarioId, request)

                if (response.isSuccessful) {
                    with(prefs.edit()) {
                        putString("user_telefono", nuevoTelefono)
                        putString("user_alergias", nuevasAlergias)
                        putString("user_cronicas", nuevasCronicas)
                        putString("user_medicinas", nuevasMedicinas)
                        apply()
                    }

                    Toast.makeText(this@EditarPerfilActivity, "¡Perfil actualizado correctamente!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditarPerfilActivity, "No se pudo actualizar el perfil", Toast.LENGTH_SHORT).show()
                    btnGuardar.isEnabled = true
                    btnGuardar.text = "Guardar Cambios"
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al actualizar perfil", e)
                Toast.makeText(this@EditarPerfilActivity, "Error de red. Verifique su conexión.", Toast.LENGTH_SHORT).show()
                btnGuardar.isEnabled = true
                btnGuardar.text = "Guardar Cambios"
            }
        }
    }
}