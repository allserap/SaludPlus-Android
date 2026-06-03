package com.citas.medicas.ui.medico

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentRecetasBinding
import com.citas.medicas.models.CitaResponse
import com.citas.medicas.models.RecetaRequest
import com.citas.medicas.models.DetalleRecetaItemRequest
import com.citas.medicas.models.MedicamentoResponse
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment
import com.citas.medicas.utils.limpiarCampos
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RecetasFragment : BaseFragment(R.layout.fragment_recetas) {

    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    private var citaSeleccionada: CitaResponse? = null
    private var catalogoMedicamentosReales: List<MedicamentoResponse> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecetasBinding.bind(view)

        setupObservers()
        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.cargarTodasLasCitas(requireContext())
            authViewModel.cargarMedicamentosCatalogos()
        }
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnGenerarReceta.isEnabled = !isLoading
            binding.btnGenerarReceta.text = if (isLoading) "Guardando..." else "Generar Receta"
        }

        authViewModel.listaCitas.observe(viewLifecycleOwner) { citas ->
            val citasPendientes = citas?.filter { it.estadocita?.lowercase()?.trim() in listOf("confirmada" , "reprogramada", "pendiente") }

            if (citasPendientes.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No hay citas confirmadas para atención", Toast.LENGTH_LONG).show()
                binding.autoCompleteConsultarReceta.setAdapter(null)
            } else {
                configurarBuscadorCitas(citasPendientes)
            }
        }

        authViewModel.listaMedicamentos.observe(viewLifecycleOwner) { medicamentos ->
            if (!medicamentos.isNullOrEmpty()) {
                this.catalogoMedicamentosReales = medicamentos
            }
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // PASO 2: Éxito al marcar la asistencia y cambiar el estado en Node.js de Confirmada -> Atendida
        authViewModel.asistenciaMarcadaExito.observe(viewLifecycleOwner) { asistenciaOk ->
            if (asistenciaOk) {
                Toast.makeText(context, "Receta generada y consulta finalizada.", Toast.LENGTH_SHORT).show()
                resetearInterfaz()
                authViewModel.resetRecetaStatus()
                authViewModel.resetAsistenciaStatus()
                authViewModel.cargarTodasLasCitas(requireContext())
            }
        }
    }

    private fun setupListeners() {
        binding.btnGenerarReceta.setOnClickListener {
            ocultarTeclado()

            if (citaSeleccionada == null) {
                Toast.makeText(context, "Seleccione un paciente de la lista de citas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validarReceta()) {
                enviarRecetaAlServidor()
            }
        }

        binding.btnAgregarMedicamento.setOnClickListener { agregarCampoMedicamento() }
        binding.btnCancelarReceta.setOnClickListener { resetearInterfaz() }
    }

    private fun configurarBuscadorCitas(citas: List<CitaResponse>) {
        val sugerencias = citas.map { "[${it.horaasignada}] ${it.nombrepaciente} ${it.apellidopaciente}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerencias)

        binding.autoCompleteConsultarReceta.setAdapter(adapter)
        binding.autoCompleteConsultarReceta.setOnItemClickListener { _, _, position, _ ->
            val seleccion = adapter.getItem(position)
            val cita = citas.find { "[${it.horaasignada}] ${it.nombrepaciente} ${it.apellidopaciente}" == seleccion }

            cita?.let {
                this.citaSeleccionada = it
                llenarFormularioCita(it)
            }
        }
    }

    private fun llenarFormularioCita(cita: CitaResponse) {
        with(binding) {
            tvDisplayPacienteNombreReceta.text = "Paciente: ${cita.nombrepaciente} ${cita.apellidopaciente}"
            tvDisplayDuiReceta.text = "Especialidad: ${cita.especialidadcita}"
            tvDisplayEdadReceta.text = "Hora: ${cita.horaasignada}"

            btnAgregarMedicamento.isEnabled = true
            btnGenerarReceta.isEnabled = true
            etObservacionesReceta.isEnabled = true
        }
    }

    private fun validarReceta(): Boolean {
        if (binding.containerMedicamentos.childCount == 0) {
            Toast.makeText(requireContext(), "Debe agregar al menos un medicamento", Toast.LENGTH_SHORT).show()
            return false
        }
        if (catalogoMedicamentosReales.isEmpty()) {
            Toast.makeText(requireContext(), "Catálogo de medicinas no cargado", Toast.LENGTH_SHORT).show()
            return false
        }

        for (i in 0 until binding.containerMedicamentos.childCount) {
            val itemView = binding.containerMedicamentos.getChildAt(i)
            val txtDosis = itemView.findViewById<TextInputEditText>(R.id.txtDosis)
            val txtCantidad = itemView.findViewById<TextInputEditText>(R.id.txtCantidad)
            val txtDias = itemView.findViewById<TextInputEditText>(R.id.txtDuracionDias)
            val txtInstrucciones = itemView.findViewById<TextInputEditText>(R.id.txtInstrucciones)

            if (txtDosis.text.toString().trim().isEmpty() || txtCantidad.text.toString().trim().isEmpty() ||
                txtDias.text.toString().trim().isEmpty() || txtInstrucciones.text.toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "Complete todos los campos de los medicamentos", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun enviarRecetaAlServidor() {
        val c = citaSeleccionada ?: return
        val observacionesInput = binding.etObservacionesReceta.text.toString().trim()
        val cabeceraRequest = RecetaRequest(observaciones = observacionesInput.ifEmpty { null })

        authViewModel.guardarRecetaCompleta(c.pacienteid, cabeceraRequest) { recetaIdObtenido ->
            val listaDetalle = mutableListOf<DetalleRecetaItemRequest>()
            for (i in 0 until binding.containerMedicamentos.childCount) {
                val viewMed = binding.containerMedicamentos.getChildAt(i)
                val spinnerMed = viewMed.findViewById<Spinner>(R.id.spnMunicipioMedicamento)
                val posSeleccionada = spinnerMed.selectedItemPosition

                val medicinaSeleccionada = catalogoMedicamentosReales.getOrNull(posSeleccionada)
                val idMedicamentoFinal = medicinaSeleccionada?.id ?: 0

                val dosis = viewMed.findViewById<TextInputEditText>(R.id.txtDosis).text.toString().trim()
                val cantidad = viewMed.findViewById<TextInputEditText>(R.id.txtCantidad).text.toString().toIntOrNull() ?: 1
                val dias = viewMed.findViewById<TextInputEditText>(R.id.txtDuracionDias).text.toString().toIntOrNull() ?: 1
                val instrucciones = viewMed.findViewById<TextInputEditText>(R.id.txtInstrucciones).text.toString().trim()

                listaDetalle.add(
                    DetalleRecetaItemRequest(
                        recetaId = recetaIdObtenido,
                        medicamentoId = idMedicamentoFinal,
                        dosis = dosis,
                        duracionDias = dias,
                        cantidad = cantidad,
                        instrucciones = instrucciones
                    )
                )
            }
            listaDetalle
        }
    }

    private fun agregarCampoMedicamento() {
        if (catalogoMedicamentosReales.isEmpty()) return
        val viewMed = layoutInflater.inflate(R.layout.item_medicamento, binding.containerMedicamentos, false)
        val spinnerMed = viewMed.findViewById<Spinner>(R.id.spnMunicipioMedicamento)

        val nombresMedicamentos = catalogoMedicamentosReales.map {
            "${it.nombreGenerico} (${it.nombreComercial}) - ${it.concentracion}"
        }.toTypedArray()

        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombresMedicamentos)
        spinnerMed.adapter = adapterSpinner

        viewMed.findViewById<View>(R.id.btnEliminarItemMedicamento).setOnClickListener {
            binding.containerMedicamentos.removeView(viewMed)
        }
        binding.containerMedicamentos.addView(viewMed)
    }

    override fun resetearInterfaz() {
        limpiarCampos(binding.autoCompleteConsultarReceta, binding.etObservacionesReceta)
        binding.tvDisplayPacienteNombreReceta.text = ""
        binding.tvDisplayDuiReceta.text = ""
        binding.tvDisplayEdadReceta.text = ""
        binding.containerMedicamentos.removeAllViews()

        citaSeleccionada = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}