package com.citas.medicas.ui.medico

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
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

    // Se recomienda activityViewModels para evitar re-descargar la lista que ya tiene la Agenda
    private val authViewModel: AuthViewModel by activityViewModels()

    // Variables de respaldo para mitigar cierres inesperados de la vista de entrada
    private var usuarioIdRespaldo: String? = null
    private var pacienteSeleccionado: PacienteResponse? = null

    // Opciones del Spinner para mitigar errores de digitación por el médico
    private val opcionesTipoSangre = arrayOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistorialBinding.bind(view)

        setupSpinnerTipoSangre()
        setupObservers()
        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.cargarPacientes()
        }

    }

    private fun setupSpinnerTipoSangre() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesTipoSangre)
        binding.spinnerTipoSangre.setAdapter(adapter)
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGuardarExpediente.isEnabled = !isLoading
            binding.btnGuardarExpediente.text = if (isLoading) "Guardando..." else "Guardar Consulta"
        }

        authViewModel.listaPacientes.observe(viewLifecycleOwner) { pacientes ->
            if (pacientes.isNullOrEmpty()) {
                android.util.Log.d("JSON_CRUDO_FRONT", "Primer paciente completo: ${pacientes[0]}")
                Toast.makeText(requireContext(), "No se encontraron pacientes registrados", Toast.LENGTH_LONG).show()
                binding.autoCompleteConsultar.setAdapter(null)
            } else {
                configurarBuscador(pacientes)
            }
        }

        authViewModel.registroExitoso.observe(viewLifecycleOwner) {
            if (usuarioIdRespaldo != null) {
                Toast.makeText(context, "Expediente actualizado correctamente", Toast.LENGTH_SHORT).show()
                resetearInterfaz()
                authViewModel.cargarPacientes()
            }
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnGuardarExpediente.setOnClickListener {
            binding.root.post { ocultarTeclado() }

            if (usuarioIdRespaldo == null) {
                Toast.makeText(context, "Selecciona un paciente primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val notasInput = binding.etNotasClinicas.text.toString().trim()
            if (notasInput.isEmpty()) {
                binding.etNotasClinicas.error = "Las notas clínicas son obligatorias para guardar la consulta"
                return@setOnClickListener
            } else {
                binding.etNotasClinicas.error = null
            }
            binding.btnGuardarExpediente.isEnabled = false
            binding.btnGuardarExpediente.text = "Guardando..."
            binding.btnCancelarFormulario.isEnabled = false

            enviarActualizacionAlServidor()
        }

        binding.btnCancelarFormulario.setOnClickListener {
            binding.btnCancelarFormulario.isEnabled = false
            resetearInterfaz()
        }
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
                this.usuarioIdRespaldo = it.usuarioId
                llenarFormulario(it)
            }
        }
    }

    private fun llenarFormulario(paciente: PacienteResponse) {
        android.util.Log.d("API_DEBUG", "Medicamentos del paciente: ${paciente.medicamentosRecurrentes}")
        with(binding) {
            tvDisplayPacienteNombre.setText("${paciente.nombre} ${paciente.apellido}")
            tvDisplayDui.setText(paciente.dui)
            tvDisplayGenero.setText(paciente.genero)
            tvDisplayEdad.setText(paciente.edad?.toString() ?: "N/A")

            etAlergias.setText(paciente.alergias ?: "")
            etNotasClinicas.setText(paciente.notaClinica ?: "")
            etMedicamentosRecurrentes.setText(paciente.medicamentosRecurrentes ?: "")
            etCondicionesCronicas.setText(paciente.condicionesCronicas ?: "")

            if (!paciente.tipoSangre.isNullOrBlank() && opcionesTipoSangre.contains(paciente.tipoSangre)) {
                spinnerTipoSangre.setText(paciente.tipoSangre, false)
            } else {
                spinnerTipoSangre.setText("", false)
            }

            val esActivo = paciente.activo
            etAlergias.isEnabled = esActivo
            etNotasClinicas.isEnabled = esActivo
            tilTipoSangre.isEnabled = esActivo
            etMedicamentosRecurrentes.isEnabled = esActivo
            etCondicionesCronicas.isEnabled = esActivo
            btnGuardarExpediente.isEnabled = esActivo

            if (!esActivo) {
                Toast.makeText(context, "Paciente inactivo. No se puede modificar su historial.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun enviarActualizacionAlServidor() {
        val p = pacienteSeleccionado ?: run {
        binding.btnGuardarExpediente.isEnabled = true
        binding.btnGuardarExpediente.text = "Guardar Consulta"
        binding.btnCancelarFormulario.isEnabled = true
        return
    }
    val currentUsuarioId = usuarioIdRespaldo ?: run {
        binding.btnGuardarExpediente.isEnabled = true
        binding.btnGuardarExpediente.text = "Guardar Consulta"
        binding.btnCancelarFormulario.isEnabled = true
        return
    }

        // Extracción segura del valor del spinner de tipo de sangre
        val tipoSangreSeleccionado = binding.spinnerTipoSangre.text.toString().trim()

    val updateRequest = PacienteUpdateRequest(
        id = currentUsuarioId,
        nombre = p.nombre,
        apellido = p.apellido,
        dui = p.dui,
        email = p.email,
        password = null,
        telefono = p.telefono,
        fechaNacimiento = p.fechaNacimiento,
        genero = p.genero,
        rol = p.rolId,
        activo = p.activo,

        estadoFamiliar = p.estadoFamiliar,
        numAfiliado = p.numAfiliado,
        tipoSangre = if (tipoSangreSeleccionado.isNotEmpty()) tipoSangreSeleccionado else null,
        alergias = binding.etAlergias.text.toString().trim(),
        condicionesCronicas = binding.etCondicionesCronicas.text.toString().trim(),
        notaClinica = binding.etNotasClinicas.text.toString().trim(),
        medicamentosRecurrentes = binding.etMedicamentosRecurrentes.text.toString().trim()
    )

    authViewModel.actualizarPaciente(updateRequest)
}

    override fun resetearInterfaz() {
        limpiarCampos(
            binding.etAlergias,
            binding.etNotasClinicas,
            binding.autoCompleteConsultar,
            binding.etMedicamentosRecurrentes,
            binding.etCondicionesCronicas,
            binding.tvDisplayGenero,
            binding.tvDisplayDui,
            binding.tvDisplayPacienteNombre,
            binding.tvDisplayEdad
        )

        // Manejo del reseteo del spinner fuera de la función limpiarCampos
        binding.spinnerTipoSangre.setText("", false)
        setupSpinnerTipoSangre()

        pacienteSeleccionado = null
        usuarioIdRespaldo = null

        binding.btnGuardarExpediente.isEnabled = true
        binding.btnGuardarExpediente.text = "Guardar Consulta"
        binding.btnCancelarFormulario.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}