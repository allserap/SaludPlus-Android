package com.citas.medicas.ui.medico

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.citas.medicas.R
import com.citas.medicas.data.ApiService
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.databinding.FragmentRecetasBinding
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.models.RecetaRequest
import com.citas.medicas.models.DetalleRecetaItemRequest
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment
import com.citas.medicas.utils.limpiarCampos
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RecetasFragment : BaseFragment(R.layout.fragment_recetas) {

    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var apiService: ApiService
    private var pacienteSeleccionado: PacienteResponse? = null

    // Catálogos mockeados de IDs de medicamentos para poblar los spinners del listado dinámico
    private val mockupMedicamentosId = listOf(1, 2, 3, 4, 5)
    private val mockupMedicamentosNombres = arrayOf("Paracetamol 500mg", "Ibuprofeno 400mg", "Amoxicilina 500mg", "Loratadina 10mg", "Metformina 850mg")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecetasBinding.bind(view)
        apiService = RetrofitClient.getApiService(requireContext())

        setupObservers()
        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.cargarPacientes()
        }

        arguments?.let {
            val idAgenda = it.getInt("PACIENTE_ID", -1)
            if (idAgenda != -1) {
                mapearPacienteDesdeAgenda(idAgenda)
            }
        }
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnGenerarReceta.isEnabled = !isLoading
            binding.btnGenerarReceta.text = if (isLoading) "Guardando..." else "Generar Receta"
        }

        authViewModel.listaPacientes.observe(viewLifecycleOwner) { pacientes ->
            if (pacientes.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No se encontraron pacientes registrados", Toast.LENGTH_LONG).show()
                binding.autoCompleteConsultarReceta.setAdapter(null)
            } else {
                configurarBuscador(pacientes)
                arguments?.let {
                    val idAgenda = it.getInt("PACIENTE_ID", -1)
                    if (idAgenda != -1) mapearPacienteDesdeAgenda(idAgenda)
                }
            }
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.btnGenerarReceta.setOnClickListener {
            ocultarTeclado()

            if (pacienteSeleccionado == null) {
                Toast.makeText(context, "Selecciona un paciente primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validarReceta()) {
                enviarRecetaAlServidor()
            }
        }

        binding.btnAgregarMedicamento.setOnClickListener {
            agregarCampoMedicamento()
        }

        binding.btnCancelarReceta.setOnClickListener { resetearInterfaz() }
    }

    private fun configurarBuscador(pacientes: List<PacienteResponse>) {
        val sugerencias = pacientes.map { "${it.nombre} ${it.apellido} (${it.dui})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerencias)

        binding.autoCompleteConsultarReceta.setAdapter(adapter)
        binding.autoCompleteConsultarReceta.setOnItemClickListener { _, _, position, _ ->
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
            binding.autoCompleteConsultarReceta.setText("${it.nombre} ${it.apellido} (${it.dui})", false)
            llenarFormulario(it)
        }
    }

    private fun llenarFormulario(paciente: PacienteResponse) {
        with(binding) {
            tvDisplayPacienteNombreReceta.text = "Paciente: ${paciente.nombre} ${paciente.apellido}"
            tvDisplayDuiReceta.text = "DUI: ${paciente.dui}"
            tvDisplayEdadReceta.text = "Edad: ${paciente.edad}"

            val esActivo = paciente.activo
            btnAgregarMedicamento.isEnabled = esActivo
            btnGenerarReceta.isEnabled = esActivo
            etObservacionesReceta.isEnabled = esActivo

            if (!esActivo) {
                Toast.makeText(context, "Paciente inactivo. No se puede modificar su historial ni emitir recetas.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validarReceta(): Boolean {
        if (binding.containerMedicamentos.childCount == 0) {
            Toast.makeText(requireContext(), "Debe agregar al menos un medicamento", Toast.LENGTH_SHORT).show()
            return false
        }

        for (i in 0 until binding.containerMedicamentos.childCount) {
            val itemView = binding.containerMedicamentos.getChildAt(i)
            val txtDosis = itemView.findViewById<TextInputEditText>(R.id.txtDosis)
            val txtCantidad = itemView.findViewById<TextInputEditText>(R.id.txtCantidad)
            val txtDias = itemView.findViewById<TextInputEditText>(R.id.txtDuracionDias)
            val txtInstrucciones = itemView.findViewById<TextInputEditText>(R.id.txtInstrucciones)

            if (txtDosis.text.toString().trim().isEmpty()) {
                txtDosis.error = "Campo requerido"
                return false
            }
            if (txtCantidad.text.toString().trim().isEmpty()) {
                txtCantidad.error = "Requerido"
                return false
            }
            if (txtDias.text.toString().trim().isEmpty()) {
                txtDias.error = "Requerido"
                return false
            }
            if (txtInstrucciones.text.toString().trim().isEmpty()) {
                txtInstrucciones.error = "Requerido"
                return false
            }
        }
        return true
    }

    private fun enviarRecetaAlServidor() {
        val p = pacienteSeleccionado ?: return
        val observacionesInput = binding.etObservacionesReceta.text.toString().trim()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Crear la cabecera usando RecetaRequest
                val cabeceraRequest = RecetaRequest(observaciones = observacionesInput.ifEmpty { null })
                val responseCabecera = apiService.crearRecetaCabecera(p.pacienteId, cabeceraRequest)

                if (responseCabecera.isSuccessful && responseCabecera.body()?.success == true) {
                    val recetaId = responseCabecera.body()!!.data!!.recetaId

                    // Recolectar la lista dinámica de medicamentos usando DetalleRecetaItemRequest
                    val listaDetalle = mutableListOf<DetalleRecetaItemRequest>()
                    for (i in 0 until binding.containerMedicamentos.childCount) {
                        val viewMed = binding.containerMedicamentos.getChildAt(i)

                        val spinnerMed = viewMed.findViewById<Spinner>(R.id.spnMunicipioMedicamento)
                        val posSeleccionada = spinnerMed.selectedItemPosition
                        val idMedicamentoFinal = mockupMedicamentosId.getOrElse(posSeleccionada) { 1 }

                        val dosis = viewMed.findViewById<TextInputEditText>(R.id.txtDosis).text.toString().trim()
                        val cantidad = viewMed.findViewById<TextInputEditText>(R.id.txtCantidad).text.toString().toIntOrNull() ?: 1
                        val dias = viewMed.findViewById<TextInputEditText>(R.id.txtDuracionDias).text.toString().toIntOrNull() ?: 1
                        val instrucciones = viewMed.findViewById<TextInputEditText>(R.id.txtInstrucciones).text.toString().trim()

                        listaDetalle.add(
                            DetalleRecetaItemRequest(
                                recetaId = recetaId,
                                medicamentoId = idMedicamentoFinal,
                                dosis = dosis,
                                duracionDias = dias,
                                cantidad = cantidad,
                                instrucciones = instrucciones
                            )
                        )
                    }

                    // Paso 3: Enviar detalle usando agregarMedicamentosAReceta
                    val responseDetalle = apiService.agregarMedicamentosAReceta(listaDetalle)
                    if (responseDetalle.isSuccessful && responseDetalle.body()?.success == true) {
                        Toast.makeText(context, "Receta guardada con éxito", Toast.LENGTH_SHORT).show()
                        resetearInterfaz()
                    } else {
                        Toast.makeText(context, "Error al guardar el desglose de medicamentos", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "No se pudo procesar la receta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("RecetasFragment", "Error en transaccion de receta", e)
                Toast.makeText(context, "Error de comunicación: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun agregarCampoMedicamento() {
        val viewMed = layoutInflater.inflate(R.layout.item_medicamento, binding.containerMedicamentos, false)

        val spinnerMed = viewMed.findViewById<Spinner>(R.id.spnMunicipioMedicamento)
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mockupMedicamentosNombres)
        spinnerMed.adapter = adapterSpinner

        viewMed.findViewById<View>(R.id.btnEliminarItemMedicamento).setOnClickListener {
            binding.containerMedicamentos.removeView(viewMed)
        }

        binding.containerMedicamentos.addView(viewMed)
    }

    override fun resetearInterfaz() {
        limpiarCampos(
            binding.autoCompleteConsultarReceta,
            binding.etObservacionesReceta
        )
        // Reiniciamos los TextView fijos de forma manual
        binding.tvDisplayPacienteNombreReceta.text = ""
        binding.tvDisplayDuiReceta.text = ""
        binding.tvDisplayEdadReceta.text = ""

        binding.containerMedicamentos.removeAllViews()
        pacienteSeleccionado = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}