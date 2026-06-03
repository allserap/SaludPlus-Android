package com.citas.medicas.ui.medico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.citas.medicas.databinding.ViewHistorialLecturaBinding
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.ui.auth.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HistorialBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: ViewHistorialLecturaBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    companion object {
        private const val ARG_PACIENTE_ID = "PACIENTE_ID"

        fun newInstance(pacienteId: Int): HistorialBottomSheetFragment {
            val fragment = HistorialBottomSheetFragment()
            val args = Bundle()
            args.putInt(ARG_PACIENTE_ID, pacienteId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewHistorialLecturaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCerrarBottomSheet.setOnClickListener { dismiss() }

        arguments?.let {
            val idPaciente = it.getInt(ARG_PACIENTE_ID, -1)
            if (idPaciente != -1) {
                mapearPacienteDesdeAgenda(idPaciente)
            }
        }
    }

    private fun mapearPacienteDesdeAgenda(pacienteId: Int) {
        val pacientes = authViewModel.listaPacientes.value
        val paciente = pacientes?.find { it.pacienteId == pacienteId }
        paciente?.let {
            llenarFormularioSoloLectura(it)
        } ?: run {
            Toast.makeText(context, "No se encontraron los datos.", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun llenarFormularioSoloLectura(paciente: PacienteResponse) {
        with(binding) {
            // Asignación de IDs exclusivos del nuevo layout
            tvReadNombre.text = "${paciente.nombre} ${paciente.apellido}"
            tvReadDui.text = paciente.dui
            tvReadGenero.text = if (paciente.genero == "F") "Femenino" else "Masculino"
            tvReadEdad.text = "${paciente.edad?.toString() ?: "N/A"} años"

            tvReadAlergias.text = paciente.alergias ?: "Ninguna"
            tvReadCondiciones.text = paciente.condicionesCronicas ?: "Ninguna"
            tvReadMedicamentos.text = paciente.medicamentosRecurrentes ?: "Ninguno"
            tvReadNotas.text = paciente.notaClinica ?: "Sin anotaciones previas."
            tvReadTipoSangre.text = paciente.tipoSangre ?: "N/D"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}