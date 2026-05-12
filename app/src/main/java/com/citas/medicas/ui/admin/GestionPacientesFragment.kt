package com.citas.medicas.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentGestionPacientesBinding
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.models.PacienteUpdateRequest
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment
import com.citas.medicas.utils.aplicarMascaraTelefono
import com.citas.medicas.utils.configurarConHint
import com.citas.medicas.utils.limpiarCampos
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GestionPacientesFragment : BaseFragment(R.layout.fragment_gestion_pacientes) {

    private var _binding: FragmentGestionPacientesBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private var pacienteSeleccionado: PacienteResponse? = null
    private var estadoOriginal: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGestionPacientesBinding.bind(view)

        setupUI()
        setupObservers()
        setupListeners()

        authViewModel.cargarPacientes()
    }

    private fun setupUI() {
        binding.etTelefonoContacto.aplicarMascaraTelefono()

        val opcionesEstado = arrayOf("Activo", "Inactivo")
        binding.spnActivoPaciente.configurarConHint(opcionesEstado, "Seleccione Estado")
    }

    private fun setupObservers() {
        // Feedback de carga
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGuardarPaciente.isEnabled = !isLoading
            binding.btnGuardarPaciente.text = if (isLoading) "Actualizando..." else "Actualizar"
        }

        // Lista de pacientes para el buscador
        authViewModel.listaPacientes.observe(viewLifecycleOwner) { pacientes ->
            if (pacientes.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No se encontraron pacientes registrados", Toast.LENGTH_LONG).show()

                // limpiar el buscador si estaba lleno
                binding.autoCompletePacientes.setAdapter(null)
            } else {
                configurarBuscador(pacientes)
            }
        }

        // Éxito en la actualización
        authViewModel.registroExitoso.observe(viewLifecycleOwner) {
            Toast.makeText(context, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            resetearInterfaz()
            authViewModel.cargarPacientes()
        }

        // Manejo de errores
        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.btnGuardarPaciente.setOnClickListener {
            ocultarTeclado()

            if (pacienteSeleccionado == null) {
                Toast.makeText(context, "Selecciona un paciente primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val estadoSeleccionado = binding.spnActivoPaciente.selectedItem.toString() == "Activo"

            // Lógica de advertencia al desactivar
            if (estadoOriginal && !estadoSeleccionado) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirmar Desactivación")
                    .setMessage("¿Está seguro de desactivar a este paciente? No podrá acceder al sistema.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Desactivar") { _, _ -> enviarActualizacionAlServidor() }
                    .show()
            } else {
                enviarActualizacionAlServidor()
            }
        }

        binding.btnCancelarPaciente.setOnClickListener { resetearInterfaz() }
    }

    private fun configurarBuscador(pacientes: List<PacienteResponse>) {
        val sugerencias = pacientes.map { "${it.nombre} ${it.apellido} (${it.dui})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerencias)

        binding.autoCompletePacientes.setAdapter(adapter)
        binding.autoCompletePacientes.setOnItemClickListener { _, _, position, _ ->
            val seleccion = adapter.getItem(position)
            val paciente = pacientes.find { "${it.nombre} ${it.apellido} (${it.dui})" == seleccion }
            paciente?.let {
                this.pacienteSeleccionado = it
                this.estadoOriginal = it.activo
                llenarFormulario(it)
            }
        }
    }

    private fun llenarFormulario(paciente: PacienteResponse) {
        with(binding) {
            // Datos de Identificación (Solo lectura)
            etNombreIdentificacion.setText("${paciente.nombre} ${paciente.apellido}")
            etDuiPaciente.setText(paciente.dui)
            etNumAfiliado.setText(paciente.numAfiliado ?: "N/A")
            etAlergias.setText(paciente.alergias ?: "N/A")
            etTipoSangre.setText(paciente.tipoSangre ?: "N/A")
            // Datos Editables (Gestión)
            etTelefonoContacto.setText(paciente.telefono)
            etCorreoContacto.setText(paciente.email)
            // Lógica de validación de estado
            val esActivo = paciente.activo
            etTelefonoContacto.isEnabled = esActivo
            etCorreoContacto.isEnabled = esActivo

            spnActivoPaciente.isEnabled = true

            val posEstado = if (paciente.activo) 1 else 2
            spnActivoPaciente.setSelection(posEstado)

            if (!paciente.activo) {
                Toast.makeText(context, "Paciente inactivo. Actívelo para permitir acceso.", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun enviarActualizacionAlServidor() {
        val p = pacienteSeleccionado ?: return

        // Verificamos que el ID no sea nulo antes de crear el Request
        val idUsuario = p.usuarioId
        if (idUsuario.isNullOrBlank()) {
            Toast.makeText(context, "Error: El ID del paciente no es válido", Toast.LENGTH_SHORT).show()
            return
        }

            val updateRequest = PacienteUpdateRequest(
                id = p.usuarioId,
                nombre = p.nombre,
                apellido = p.apellido,
                dui = p.dui,
                email = binding.etCorreoContacto.text.toString().trim(),
                telefono = binding.etTelefonoContacto.text.toString().trim(),
                fechaNacimiento = p.fechaNacimiento,
                genero = p.genero,
                rol = p.rolId,
                activo = binding.spnActivoPaciente.selectedItem.toString() == "Activo",

                // Datos clínicos originales para no perderlos
                estadoFamiliar = p.estadoFamiliar,
                numAfiliado = p.numAfiliado,
                tipoSangre = p.tipoSangre,
                alergias = p.alergias,
                condicionesCronicas = p.condicionesCronicas,
                notaClinica = p.notaClinica,
                medicamentosRecurrentes = p.medicamentosRecurrentes
            )

            authViewModel.actualizarPaciente(updateRequest)
        }


    override fun resetearInterfaz() {
        limpiarCampos(
            binding.etNombreIdentificacion,
            binding.etDuiPaciente,
            binding.etAlergias,
            binding.etTipoSangre,
            binding.etNumAfiliado,
            binding.etTelefonoContacto,
            binding.etCorreoContacto,
            binding.autoCompletePacientes
        )
        binding.spnActivoPaciente.setSelection(0)
        pacienteSeleccionado = null
        estadoOriginal = true
        binding.btnGuardarPaciente.text = "Actualizar"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}