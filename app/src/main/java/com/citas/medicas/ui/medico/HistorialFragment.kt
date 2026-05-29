package com.citas.medicas.ui.medico

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentHistorialBinding
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.models.PacienteUpdateRequest
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment
import com.citas.medicas.utils.limpiarCampos
import kotlinx.coroutines.launch

class HistorialFragment : BaseFragment(R.layout.fragment_historial) {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private var pacienteSeleccionado: PacienteResponse? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistorialBinding.bind(view)

        setupObservers()
        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.cargarPacientes()
        }

        // Verificar si viene direccionado de forma directa por la Agenda
        arguments?.let {
            val idAgenda = it.getInt("PACIENTE_ID", -1)
            if (idAgenda != -1) {
                // Buscar al paciente localmente si la lista ya cargó, o esperamos al observer
                mapearPacienteDesdeAgenda(idAgenda)
            }
        }
    }

    private fun setupObservers() {
        // Feedback de carga idéntico a Gestión
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            //binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGuardarExpediente.isEnabled = !isLoading
            binding.btnGuardarExpediente.text = if (isLoading) "Guardando..." else "Guardar Consulta"
        }

        // Lista de pacientes para el buscador (Filtro Interno)
        authViewModel.listaPacientes.observe(viewLifecycleOwner) { pacientes ->
            if (pacientes.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No se encontraron pacientes registrados", Toast.LENGTH_LONG).show()
                binding.autoCompleteConsultar.setAdapter(null)
            } else {
                configurarBuscador(pacientes)

                // Por si venía un ID de la agenda y la lista tardó en responder
                arguments?.let {
                    val idAgenda = it.getInt("PACIENTE_ID", -1)
                    if (idAgenda != -1) mapearPacienteDesdeAgenda(idAgenda)
                }
            }
        }

        // Éxito en la actualización del expediente
        authViewModel.registroExitoso.observe(viewLifecycleOwner) {
            Toast.makeText(context, "Expediente actualizado correctamente", Toast.LENGTH_SHORT).show()
            resetearInterfaz()
            authViewModel.cargarPacientes() // Refrescar lista local
        }

        // Manejo de errores
        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.btnGuardarExpediente.setOnClickListener {
            ocultarTeclado()

            if (pacienteSeleccionado == null) {
                Toast.makeText(context, "Selecciona un paciente primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val notasInput = binding.etNotasClinicas.text.toString().trim()
            if (notasInput.isEmpty()) {
                binding.etNotasClinicas.error = "Las notas clínicas son obligatorias para guardar la consulta"
                return@setOnClickListener
            }

            enviarActualizacionAlServidor()
        }

        binding.btnCancelarFormulario.setOnClickListener { resetearInterfaz() }
    }

    private fun configurarBuscador(pacientes: List<PacienteResponse>) {
        val sugerencias = pacientes.map { "${it.nombre} ${it.apellido} (${it.dui})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerencias)

        binding.autoCompleteConsultar.setAdapter(adapter)
        binding.autoCompleteConsultar.setOnItemClickListener { _, _, position, _ ->
            val seleccion = adapter.getItem(position)
            val paciente = pacientes.find { "${it.nombre} ${it.apellido} (${it.dui})" == seleccion }
            paciente?.let {
                this.pacienteSeleccionado = it
                llenarFormulario(it)
            }
        }
    }

    private fun mapearPacienteDesdeAgenda(pacienteId: Int) {
        val pacientes = authViewModel.listaPacientes.value
        val paciente = pacientes?.find { it.pacienteId == pacienteId }
        paciente?.let {
            this.pacienteSeleccionado = it
            // Setea visualmente el texto en el buscador para mantener coherencia
            binding.autoCompleteConsultar.setText("${it.nombre} ${it.apellido} (${it.dui})", false)
            llenarFormulario(it)
        }
    }

    private fun llenarFormulario(paciente: PacienteResponse) {
        with(binding) {
            // Datos de Identificación (Campos de solo lectura en tu Vista de Historial)
            tvDisplayPacienteNombre.setText("${paciente.nombre} ${paciente.apellido}")
            tvDisplayDui.setText(paciente.dui)
            tvDisplayGenero.setText(paciente.genero)
            tvDisplayEdad.setText(paciente.edad.toString())

            // Datos Clínicos Editables (Campos de Entrada de Gestión Médica)
            etAlergias.setText(paciente.alergias ?: "")
            etNotasClinicas.setText(paciente.notaClinica ?: "")
            etTipoSangre.setText(paciente.tipoSangre)
            etMedicamentosRecurrentes.setText(paciente.medicamentosRecurrentes)
            etCondicionesCronicas.setText(paciente.condicionesCronicas)

            // Si el paciente está inactivo, deshabilitamos la edición médica por seguridad
            val esActivo = paciente.activo
            etAlergias.isEnabled = esActivo
            etNotasClinicas.isEnabled = esActivo
            etTipoSangre.isEnabled = esActivo
            etMedicamentosRecurrentes.isEnabled = esActivo
            etCondicionesCronicas.isEnabled = esActivo
            btnGuardarExpediente.isEnabled = esActivo

            if (!esActivo) {
                Toast.makeText(context, "Paciente inactivo. No se puede modificar su historial.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun enviarActualizacionAlServidor() {
        val p = pacienteSeleccionado ?: return

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
            email = p.email,
            password = null,
            telefono = p.telefono,
            fechaNacimiento = p.fechaNacimiento,
            genero = p.genero,
            rol = p.rolId,
            activo = p.activo, // Mantiene su estado actual

            // Se guardan los nuevos datos ingresados por el médico en esta consulta
            estadoFamiliar = p.estadoFamiliar,
            numAfiliado = p.numAfiliado,
            tipoSangre = binding.etTipoSangre.text.toString().trim(),
            alergias = binding.etAlergias.text.toString().trim(),
            condicionesCronicas = binding.etCondicionesCronicas.text.toString().trim(),
            notaClinica = binding.etNotasClinicas.text.toString().trim(),
            medicamentosRecurrentes = binding.etMedicamentosRecurrentes.text.toString().trim()
        )

        // Consume exactamente el mismo flujo centralizado en tu AuthViewModel
        authViewModel.actualizarPaciente(updateRequest)
    }

    override fun resetearInterfaz() {
        limpiarCampos(
            binding.etAlergias,
            binding.etNotasClinicas,
            binding.autoCompleteConsultar,
            binding.etMedicamentosRecurrentes,
            binding.etTipoSangre,
            binding.tvDisplayGenero,
            binding.tvDisplayDui,
            binding.tvDisplayPacienteNombre,
            binding.tvDisplayEdad
        )
        pacienteSeleccionado = null
        binding.btnGuardarExpediente.text = "Guardar Consulta"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}