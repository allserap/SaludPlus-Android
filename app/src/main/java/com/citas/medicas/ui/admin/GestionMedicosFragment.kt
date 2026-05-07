package com.citas.medicas.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentGestionMedicosBinding
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.utils.RolesUsuario
import com.citas.medicas.utils.Validation.isValidDUI
import com.citas.medicas.utils.Validation.isValidEmail
import com.citas.medicas.utils.Validation.isValidPassword
import com.citas.medicas.utils.Validation.isValidPhone
import kotlin.getValue

class GestionMedicosFragment : Fragment(R.layout.fragment_gestion_medicos) {
    // Inicializar el binding
    /*private var _binding: FragmentGestionMedicosBinding? = null
    private val binding get() = _binding!!

    private var medicoIdSeleccionado: Int? = null
    private var listaMedicosGlobal: List<MedicoResponse> = emptyList()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGestionMedicosBinding.bind(view)

        setupObservers()
        setupListeners()
        setupCatalogosObservers()

        configurarBuscador(listaMedicosGlobal)
    }

    private fun setupListeners() {
        binding.btnGuardarMedico.setOnClickListener {
            // 1. Validamos que los campos cumplan con el formato (DUI, teléfono, etc.)
            if (validarForm()) {
                // 2. Como este fragment solo edita, llamamos directo a la confirmación
                confirmarActualizacion()
            }
        }

        binding.btnCancelarMedico.setOnClickListener {
            // En un fragment de "Solo Edición", este botón suele ser para
            // "Limpiar" o "Cerrar" el formulario.
            limpiarCampos()
            medicoIdSeleccionado = null
        }
    }

    private fun configurarBuscador(medicos: List<MedicoResponse>) {
        // Creamos la lista de sugerencias (Nombre + DUI)
        val sugerencias = medicos.map { "${it.nombre} ${it.apellido} - ${it.dui}" }

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerencias)
        binding.autoCompleteMedicos.setAdapter(adapter)

        // ¿Qué pasa cuando el admin selecciona un médico de la lista?
        binding.autoCompleteMedicos.setOnItemClickListener { _, _, position, _ ->
            val medico = medicos[position]
            llenarFormularioParaEditar(medico)
        }

    }

    private fun llenarFormularioParaEditar(medico: MedicoResponse) {
        medicoIdSeleccionado = medico.id

        with(binding) {
            // Llenamos los campos de texto
            etNombres.setText(medico.nombre)
            etApellidos.setText(medico.apellido)
            etDui.setText(medico.dui)
            etTelefono.setText(medico.telefono)
            etCorreo.setText(medico.email)
            etFechaNac.setText(medico.fechaNacimiento)
            etJvpm.setText(medico.numJvpm)

            // Cambiamos el texto del botón para dar feedback visual
            btnCrear.text = "Actualizar Médico"

            // Si el formulario estaba oculto, lo mostramos
            if (binding.cardFormulario.visibility == View.GONE) {
                toggleFormulario()
            }
        }
    }

    private fun confirmarActualizacion() {
        // Es buena práctica preguntar antes de cambiar datos en la DB
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Actualización")
            .setMessage("¿Desea guardar los cambios para este médico?")
            .setPositiveButton("Sí, actualizar") { _, _ ->
                ejecutarLogicaUpdate()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarLogicaUpdate() {
        val generoSeleccionado = binding.spGenero.selectedItem.toString()
        val generoFinal = if (generoSeleccionado == "Masculino") "M" else "F"

        val idUpdate = medicoIdSeleccionado ?: run {
            Toast.makeText(requireContext(), "Error: No se ha seleccionado un médico", Toast.LENGTH_SHORT).show()
            return
        }
        val datosEditados = MedicoUpdateRequest(
            id = idUpdate,
            nombre = binding.etNombres.text.toString().trim(),
            apellido = binding.etApellidos.text.toString().trim(),
            dui = binding.etDui.text.toString().trim(),
            email = binding.etCorreo.text.toString().trim(),
            password = binding.etPassword.text.toString()
                .takeIf { it.isNotEmpty() }, // Pass opcional
            telefono = binding.etTelefono.text.toString().trim(),
            fechaNacimiento = binding.etFechaNac.text.toString(),
            genero = generoFinal,
            activo = true, // agrefar Spinner para activar/desactivar
            numJvpm = binding.etJvpm.text.toString().trim(),
            especialidad = 3, // ID temporal
            unidadMedica = 1,  // ID temporal
            rol = RolesUsuario.MEDICO
        )

        // Llamamos al ViewModel (el cual activará el ProgressBar)
        authViewModel.ejecutarActualizacion(datosEditados)
    }

    private fun validarForm(): Boolean {
        with(binding) {
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val dui = etDui.text.toString().trim()
            val jvpm = etJvpm.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val fecha = etFechaNac.text.toString().trim()
            val pass = etPassword.text.toString()

            var isValid = true

            if (nombres.isEmpty()) {
                etNombres.error = "Ingrese los nombres"
                isValid = false
            }
            if (apellidos.isEmpty()) {
                etApellidos.error = "Ingrese los apellidos"
                isValid = false
            }

            if (!isValidDUI(dui)) {
                etDui.error = "Formato inválido (00000000-0)"
                isValid = false
            }

            // Validar JVPM
            if (jvpm.isEmpty() || jvpm.length < 4) {
                etJvpm.error = "JVPM inválido (Mínimo 4 dígitos)"
                isValid = false
            }

            if (fecha.isEmpty()) {
                etFechaNac.error = "Seleccione fecha de nacimiento"
                isValid = false
            }

            if (!isValidEmail(correo)) {
                etCorreo.error = "Correo electrónico inválido"
                isValid = false
            }

            if (!isValidPhone(telefono)) {
                etTelefono.error = "Formato inválido (0000-0000)"
                isValid = false
            }

            if (!isValidPassword(pass)) {
                etPassword.error = "Mínimo 6 caracteres, 1 mayúscula y 1 símbolo"
                isValid = false
            }

            if (spGenero.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Seleccione el género", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (spnEspecialidad.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Seleccione la especialidad", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            return isValid
        }
    }*/

}



