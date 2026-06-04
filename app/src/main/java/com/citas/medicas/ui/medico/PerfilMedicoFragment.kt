package com.citas.medicas.ui.medico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.ApiService
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.databinding.FragmentPerfilMedicoBinding
import com.citas.medicas.models.MedicoProfileResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.ui.AppDatabase
import com.citas.medicas.ui.medico.local.entities.MedicoPerfilEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PerfilMedicoFragment : Fragment() {

    private var _binding: FragmentPerfilMedicoBinding? = null
    private val binding get() = _binding!!

    private var isOffline = false
    private lateinit var apiService: ApiService
    private var medicoActual: MedicoProfileResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilMedicoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getApiService(requireContext())

        cargarPerfilMedico()

        binding.btnEditarContacto.setOnClickListener {
            if (isOffline) {
                Toast.makeText(context, "Necesitas internet para editar tu perfil", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            medicoActual?.let { mostrarDialogoEditarContacto(it) } ?: mostrarErrorCarga()
        }

        binding.btnCambiarClave.setOnClickListener {
            if (isOffline) {
                Toast.makeText(context, "Necesitas internet para cambiar tu contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            medicoActual?.let { mostrarDialogoCambiarClave(it) } ?: mostrarErrorCarga()
        }
    }

    private fun cargarPerfilMedico() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val dao = db.medicoPerfilDao()

            //  local primero (Offline-First)
            val perfilGuardado = dao.obtenerPerfil()
            if (perfilGuardado != null) {
                medicoActual = perfilGuardado.toModel()
                mapearDatosAInterfaz(medicoActual!!)
            }

            try {
                val response = apiService.obtenerPerfilMedicoLogueado()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {

                        isOffline = false
                        medicoActual = apiResponse.data
                        mapearDatosAInterfaz(apiResponse.data)

                        dao.limpiarTabla()
                        dao.guardarPerfil(MedicoPerfilEntity.fromModel(apiResponse.data))

                    } else {
                        Toast.makeText(context, "Error: Datos no encontrados", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PerfilMedico", "Error de red al cargar perfil", e)
                isOffline = true

                if (perfilGuardado != null) {
                    Toast.makeText(context, "Modo sin conexión. Mostrando datos guardados.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No hay internet y no hay datos guardados.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mapearDatosAInterfaz(medico: MedicoProfileResponse) {
        val jvpmTexto = if (medico.numJvpm.isNullOrEmpty() || medico.numJvpm == "null") "No asignado" else medico.numJvpm
        binding.tvJVPM.text = "JVPM $jvpmTexto"

        binding.tvDui.text = medico.dui
        binding.tvFechaNacimiento.text = formatearFechaIso(medico.fechaNacimiento)
        binding.tvGenero.text = medico.genero
        binding.tvEmail.text = medico.email
        binding.tvTelefono.text = medico.telefono
    }

    private fun mostrarDialogoEditarContacto(medico: MedicoProfileResponse) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_editar_contacto, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val txtEditEmail = dialogView.findViewById<TextInputEditText>(R.id.txtEditEmail)
        val txtEditTelefono = dialogView.findViewById<TextInputEditText>(R.id.txtEditTelefono)
        val btnGuardar = dialogView.findViewById<MaterialButton>(R.id.btnGuardarContacto)

        txtEditEmail.setText(medico.email)
        txtEditTelefono.setText(medico.telefono)

        btnGuardar.setOnClickListener {
            val nuevoEmail = txtEditEmail.text.toString().trim()
            val nuevoTelefono = txtEditTelefono.text.toString().trim()

            if (nuevoEmail.isEmpty() || nuevoTelefono.isEmpty()) {
                Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            btnGuardar.text = "Guardando..."
            dialog.setCancelable(false)

            val updateRequest = MedicoUpdateRequest(
                id = medico.id,
                nombre = medico.nombre,
                apellido = medico.apellido,
                dui = medico.dui,
                email = nuevoEmail,
                telefono = nuevoTelefono,
                fechaNacimiento = medico.fechaNacimiento?.split("T")?.get(0),
                genero = medico.generoId,
                rol = medico.rolId,
                activo = medico.activo,
                numJvpm = if (medico.numJvpm.isNullOrEmpty() || medico.numJvpm == "null") "" else medico.numJvpm,
                especialidad = medico.especialidad,
                unidadMedica = medico.unidadMedica,
                password = null
            )

            ejecutarActualizacionServidor(medico.id, updateRequest, dialog, btnGuardar, "Actualizar Contraseña")
        }
        dialog.show()
    }

    private fun mostrarDialogoCambiarClave(medico: MedicoProfileResponse) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cambiar_clave, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val txtClaveActual = dialogView.findViewById<TextInputEditText>(R.id.txtClaveActual)
        val txtClaveNueva = dialogView.findViewById<TextInputEditText>(R.id.txtClaveNueva)
        val btnActualizar = dialogView.findViewById<MaterialButton>(R.id.btnActualizarClave)

        btnActualizar.setOnClickListener {
            val claveActual = txtClaveActual.text.toString().trim()
            val claveNueva = txtClaveNueva.text.toString().trim()

            if (claveActual.isEmpty() || claveNueva.isEmpty()) {
                Toast.makeText(context, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnActualizar.isEnabled = false
            btnActualizar.text = "Actualizando..."
            dialog.setCancelable(false)

            val updateRequest = MedicoUpdateRequest(
                id = medico.id,
                nombre = medico.nombre,
                apellido = medico.apellido,
                dui = medico.dui,
                email = medico.email,
                telefono = medico.telefono,
                fechaNacimiento = medico.fechaNacimiento?.split("T")?.get(0),
                genero = medico.generoId,
                rol = medico.rolId,
                activo = medico.activo,
                numJvpm = if (medico.numJvpm.isNullOrEmpty() || medico.numJvpm == "null") "" else medico.numJvpm,
                especialidad = medico.especialidad,
                unidadMedica = medico.unidadMedica,
                password = claveNueva
            )

            ejecutarActualizacionServidor(medico.id, updateRequest, dialog, btnActualizar, "Actualizar Contraseña")
        }
        dialog.show()
    }

    private fun ejecutarActualizacionServidor(
        id: String,
        request: MedicoUpdateRequest,
        dialog: AlertDialog,
        botonAccion: MaterialButton,
        textoOriginalBoton: String
    ) {lifecycleScope.launch {
            try {
                val response = apiService.actualizarMedico(id, request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    cargarPerfilMedico()
                } else {
                    val errorContenido = response.errorBody()?.string() ?: ""
                    android.util.Log.e("PerfilMedico", "Error servidor: $errorContenido")
                    Toast.makeText(context, "No se pudo actualizar el perfil en el servidor", Toast.LENGTH_LONG).show()
                    botonAccion.isEnabled = true
                    botonAccion.text = textoOriginalBoton
                    dialog.setCancelable(true)
                }
            } catch (e: Exception) {
                android.util.Log.e("PerfilMedico", "Fallo de conexión", e)
                Toast.makeText(context, "Error de comunicación de red", Toast.LENGTH_LONG).show()
                botonAccion.isEnabled = true
                botonAccion.text = textoOriginalBoton
                dialog.setCancelable(true)
            }
        }
    }

    private fun formatearFechaIso(fechaIso: String?): String {
        if (fechaIso.isNullOrEmpty()) return "No disponible"
        return try {
            val fechaParseada = ZonedDateTime.parse(fechaIso)
            val formateador = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
            fechaParseada.format(formateador)
        } catch (e: Exception) {
            android.util.Log.e("PerfilMedico", "Error mapeando fecha: $fechaIso", e)
            fechaIso
        }
    }

    private fun mostrarErrorCarga() {
        Toast.makeText(context, "Los datos del perfil no se han cargado aún", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}