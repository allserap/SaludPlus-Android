package com.citas.medicas.ui.paciente

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.PacienteUpdateRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditarPerfilActivity : AppCompatActivity() {
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

        // Recibir los datos enviados desde PerfilActivity
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

    private fun guardarPerfilEnServidor(
        nuevoTelefono: String,
        nuevasAlergias: String,
        nuevasCronicas: String,
        nuevasMedicinas: String,
        btnGuardar: MaterialButton
    ) {
        val prefs = getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
        val usuarioId = prefs.getString("user_usuarioid", "") ?: ""

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

        // Creamos el paquete completo
        val request = PacienteUpdateRequest(
            id = usuarioId,
            nombre = nombre,
            apellido = apellido,
            dui = dui,
            email = email,
            password = null,
            telefono = nuevoTelefono, // Editado
            fechaNacimiento = fechaNac,
            genero = genero,
            rol = 1,
            activo = true,
            estadoFamiliar = "No especificado",
            numAfiliado = afiliado,
            tipoSangre = sangre,
            alergias = nuevasAlergias, // Editado
            condicionesCronicas = nuevasCronicas, // Editado
            notaClinica = "",
            medicamentosRecurrentes = nuevasMedicinas // Editado
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