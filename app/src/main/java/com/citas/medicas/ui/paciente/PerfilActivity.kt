package com.citas.medicas.ui.paciente

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.models.DatosPerfil
import com.citas.medicas.models.PacienteUpdateRequest
import com.citas.medicas.ui.AppDatabase
import com.citas.medicas.ui.auth.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private var perfilActual: DatosPerfil? = null
    private var pacienteId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
        pacienteId = prefs.getString("user_usuarioid", "") ?: ""

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationPerfil)
        bottomNav.selectedItemId = R.id.nav_perfil

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, HomePacienteActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_solicitar -> {
                    val intent = Intent(this, SolicitarCitaActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_historial -> {
                    val intent = Intent(this, HistorialCitasActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_mapa -> {
                    val intent = Intent(this, MapaActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> true
                else -> false
            }
        }

        val btnEditar = findViewById<MaterialButton>(R.id.btnEditarPerfil)
        btnEditar.setOnClickListener {
            val opciones = arrayOf("Actualizar Correo y Teléfono", "Cambiar Contraseña")
            AlertDialog.Builder(this)
                .setTitle("Opciones de Edición")
                .setItems(opciones) { _, cual ->
                    when (cual) {
                        0 -> mostrarDialogoEditarContacto()
                        1 -> mostrarDialogoCambiarClave()
                    }
                }
                .show()
        }

        val btnCerrarSesion = findViewById<LinearLayout>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("🚪 Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas salir de tu cuenta?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salir") { _, _ ->
                    val prefsEditor = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE).edit()
                    prefsEditor.clear().apply()

                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(this@PerfilActivity)
                        db.clearAllTables()
                    }

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .show()
        }

        cargarPerfil()
    }


    private fun cargarPerfil() {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        val nombre = prefs.getString("user_nombre", "Usuario")
        val apellido = prefs.getString("user_apellido", "")
        val afiliado = prefs.getString("user_afiliado", "No disponible")
        val dui = prefs.getString("user_dui", "No disponible")
        val email = prefs.getString("user_email", "No disponible")
        val telefono = prefs.getString("user_telefono", "No disponible")
        val cronicas = prefs.getString("user_cronicas", "Ninguna registrada")
        val alergias = prefs.getString("user_alergias", "Ninguna registrada")
        val medicinas = prefs.getString("user_medicinas", "Ninguna registrada")
        val sangre = prefs.getString("user_sangre", "No especificado")

        val iniciales = "${nombre?.firstOrNull() ?: ""}${apellido?.firstOrNull() ?: ""}"
        findViewById<TextView>(R.id.tvAvatarInitials).text = iniciales.uppercase()
        findViewById<TextView>(R.id.tvPerfilNombre).text = "$nombre $apellido"

        findViewById<TextView>(R.id.tvPerfilAfiliado).text = afiliado
        findViewById<TextView>(R.id.tvPerfilDUI).text = dui
        findViewById<TextView>(R.id.tvPerfilCorreo).text = email
        findViewById<TextView>(R.id.tvPerfilTelefono).text = telefono

        findViewById<TextView>(R.id.tvSaludAlergias).text = alergias
        findViewById<TextView>(R.id.tvSaludCronicas).text = cronicas
        findViewById<TextView>(R.id.tvSaludMedicinas).text = medicinas

        perfilActual = DatosPerfil(
            nombre = nombre,
            apellido = apellido,
            num_afiliado = afiliado,
            dui = dui,
            email = email,
            condiciones_cronicas = cronicas,
            alergias = alergias,
            telefono = telefono,
            tipo_sangre = sangre,
            medicinas = medicinas
        )
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

            val updateRequest = crearRequestCompleto(nuevoEmail = nuevoEmail, nuevoTelefono = nuevoTelefono)
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
                Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateRequest = crearRequestCompleto(nuevaClave = claveNueva)
            ejecutarActualizacionServidor(updateRequest, dialog, isPasswordChange = true)
        }
        dialog.show()
    }


    private fun crearRequestCompleto(
        nuevoEmail: String? = null,
        nuevoTelefono: String? = null,
        nuevaClave: String? = null
    ): PacienteUpdateRequest {
        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)

        val emailFinal = nuevoEmail ?: prefs.getString("user_email", "") ?: ""
        val telefonoFinal = nuevoTelefono ?: prefs.getString("user_telefono", "") ?: ""

        val sangreGuardada = prefs.getString("user_sangre", "")
        val sangreSegura = if (sangreGuardada == "No especificado" || sangreGuardada!!.length > 5) null else sangreGuardada

        val estadoFamiliarGuardado = prefs.getString("user_estadofamiliar", "")
        val estadoFamiliarSeguro = if (estadoFamiliarGuardado.isNullOrEmpty() || estadoFamiliarGuardado == "No especificado") "" else estadoFamiliarGuardado

        var fechaGuardada = prefs.getString("user_fechanacimiento", "")
        if (!fechaGuardada.isNullOrEmpty() && !fechaGuardada.contains("T")) {
            fechaGuardada += "T00:00:00.000Z"
        } else if (fechaGuardada.isNullOrEmpty()) {
            fechaGuardada = null
        }

        val numAfiliadoGuardado = prefs.getString("user_afiliado", "")
        val numAfiliadoSeguro = if (numAfiliadoGuardado == "No disponible") "" else numAfiliadoGuardado

        return PacienteUpdateRequest(
            id = pacienteId,
            nombre = prefs.getString("user_nombre", "") ?: "",
            apellido = prefs.getString("user_apellido", "") ?: "",
            dui = prefs.getString("user_dui", "") ?: "",
            email = emailFinal,
            password = nuevaClave,
            telefono = telefonoFinal,
            fechaNacimiento = fechaGuardada,
            genero = prefs.getString("user_genero", "M") ?: "M",
            rol = prefs.getInt("user_role", 1),
            activo = true,
            estadoFamiliar = estadoFamiliarSeguro,
            numAfiliado = numAfiliadoSeguro,
            tipoSangre = sangreSegura,
            alergias = prefs.getString("user_alergias", ""),
            condicionesCronicas = prefs.getString("user_cronicas", ""),
            notaClinica = "",
            medicamentosRecurrentes = prefs.getString("user_medicinas", "")
        )
    }


    private fun ejecutarActualizacionServidor(request: PacienteUpdateRequest, dialog: AlertDialog, isPasswordChange: Boolean) {
        val overlayCarga = findViewById<FrameLayout>(R.id.overlayCarga)
        overlayCarga.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@PerfilActivity)
                val response = apiService.actualizarPaciente(pacienteId, request)

                if (response.isSuccessful) {
                    Toast.makeText(this@PerfilActivity, "Actualizado correctamente", Toast.LENGTH_SHORT).show()

                    if (!isPasswordChange) {
                        val prefs = getSharedPreferences("CitasMedicasPrefs", MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("user_email", request.email)
                            putString("user_telefono", request.telefono)
                            apply()
                        }

                        // Refrescar UI directamente
                        findViewById<TextView>(R.id.tvPerfilCorreo).text = request.email
                        findViewById<TextView>(R.id.tvPerfilTelefono).text = request.telefono
                    }
                    dialog.dismiss()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorCode = response.code()
                    Log.e("API_ERROR_BACKEND", "Código HTTP $errorCode - Detalle: $errorBody")

                    Toast.makeText(this@PerfilActivity, "Error $errorCode al actualizar", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fallo de conexión", e)
                Toast.makeText(this@PerfilActivity, "Error de red", Toast.LENGTH_LONG).show()
            } finally {
                overlayCarga.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPerfil()
    }
}