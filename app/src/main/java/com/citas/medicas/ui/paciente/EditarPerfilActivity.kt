package com.citas.medicas.ui.paciente

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.EditarPerfilRequest
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

        val etDireccion = findViewById<TextInputEditText>(R.id.etEditDireccion)
        val etTelefono = findViewById<TextInputEditText>(R.id.etEditTelefono)
        val etAlergias = findViewById<TextInputEditText>(R.id.etEditAlergias)
        val etCronicas = findViewById<TextInputEditText>(R.id.etEditCronicas)

        val telefonoRecibido = intent.getStringExtra("EXTRA_TELEFONO")
        val alergiasRecibidas = intent.getStringExtra("EXTRA_ALERGIAS")
        val cronicasRecibidas = intent.getStringExtra("EXTRA_CRONICAS")
        val direccionRecibida = intent.getStringExtra("EXTRA_DIRECCION")

        etTelefono.setText(telefonoRecibido)
        etAlergias.setText(alergiasRecibidas)
        etCronicas.setText(cronicasRecibidas)
        etDireccion.setText(direccionRecibida)

        btnAtras.setOnClickListener {
            finish()
        }

        btnGuardar.setOnClickListener {
            // 1. Extraer los textos
            val tel = etTelefono.text.toString().trim()
            val alg = etAlergias.text.toString().trim()
            val cro = etCronicas.text.toString().trim()

            if (tel.isEmpty() || alg.isEmpty() || cro.isEmpty()) {
                Toast.makeText(this, "Por favor, no deje campos obligatorios vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
            val pacienteIdStr = prefs.getString("user_usuarioid", "1")
            val pacienteIdReal = pacienteIdStr?.toIntOrNull() ?: 1

            //  objeto a enviar
            val request = EditarPerfilRequest(
                telefono = tel,
                alergias = alg,
                condiciones_cronicas = cro
            )

            btnGuardar.isEnabled = false
            btnGuardar.text = "Guardando..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.getApiService(this@EditarPerfilActivity).actualizarPerfilPaciente(pacienteIdReal, request)
                    if (response.isSuccessful && response.body()?.exito == true) {
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
}