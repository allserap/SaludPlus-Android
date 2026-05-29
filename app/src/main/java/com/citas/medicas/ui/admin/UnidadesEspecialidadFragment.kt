package com.citas.medicas.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentUnidadesEspecialidadBinding
import com.citas.medicas.models.EspecialidadResponse
import com.citas.medicas.models.UnidadMedicaResponse
import com.citas.medicas.models.UnidadEspecialidadRequest
import com.citas.medicas.models.UnidadEspecialidadResponse
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment
import com.citas.medicas.utils.limpiarCampos
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UnidadEspecialidadFragment : BaseFragment(R.layout.fragment_unidades_especialidad) {

    // region Inicializacion
    private var _binding: FragmentUnidadesEspecialidadBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    private var idUnidadEspecialidadActual: Int? = null
    private var listaEspecialidades: List<EspecialidadResponse> = emptyList()
    private var listaUnidades: List<UnidadMedicaResponse> = emptyList()

    private var unidadSeleccionada: UnidadMedicaResponse? = null
    private var especialidadSeleccionada: EspecialidadResponse? = null

    private var isEditMode = false
    private var estadoOriginal: Boolean = true
    // endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUnidadesEspecialidadBinding.bind(view)

        setupObservers()
        setupCatalogosObservers()
        setupListeners()

        // Carga inicial sincronizada con la arquitectura de SaludPlus
        authViewModel.cargarCatalogos()
    }

    private fun setupObservers() {
        // Feedback visual
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnGuardarMedico.isEnabled = !isLoading
            binding.btnGuardarMedico.text = if (isLoading) "Procesando..." else "Guardar"
        }

        // Estado reactivo de validación del formulario
        authViewModel.formState.observe(viewLifecycleOwner) { estado ->
            with(binding) {
                etCupoDiario.error = estado.cupoDiarioError

                estado.especialidadError?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
                estado.unidadError?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }

                if (estado.isValid) {
                    // Capturamos los IDs de manera segura y definitiva aquí
                    val uId = unidadSeleccionada?.id
                    val eId = especialidadSeleccionada?.id

                    if (uId != null && eId != null) {
                        enviarActualizacionAlServidor(uId, eId)
                    } else {
                        Toast.makeText(requireContext(), "Error interno: Selección de catálogo perdida. Intente seleccionar de nuevo.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Observer para capturar el resultado si la combinación intermedia ya existía
        authViewModel.unidadEspecialidadEncontrada.observe(viewLifecycleOwner) { apiResponse ->
            val registro = apiResponse?.data
            if (registro != null) {
                llenarFormulario(registro)
            }
        }

        // Éxito en la persistencia transaccional
        authViewModel.registroExitoso.observe(viewLifecycleOwner) {
            val msg = if (isEditMode) "Asignación modificada exitosamente" else "Asignación vinculada con éxito"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            resetearInterfaz()
            authViewModel.cargarCatalogos()
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupCatalogosObservers() {
        // 1. Observer del buscador predictivo de Unidades Médicas
        authViewModel.unidadesMedicas.observe(viewLifecycleOwner) { lista ->
            listaUnidades = lista

            // NOTA: Si en tu modelo de SaludPlus el nombre se guarda en '.nombre', cambia 'it.unidadMedica' por 'it.nombre'
            val sugerencias = lista.map { it.unidadMedica }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerencias)

            binding.acUnidadMedica.setAdapter(adapter)
            binding.acUnidadMedica.setOnItemClickListener { _, _, position, _ ->
                val seleccion = adapter.getItem(position)

                // Buscamos el objeto haciendo la misma igualación que arriba
                unidadSeleccionada = listaUnidades.find { it.unidadMedica == seleccion }

                // IMPRIME ESTO EN EL LOGCAT: Verificamos si Android de verdad encontró el ID en la lista
                Log.d("DEBUG_SELECCION", "Unidad Teclada: $seleccion | ID Encontrado en Objeto: ${unidadSeleccionada?.id}")

                verificarExistenciaRegistro()
            }
        }

        // 2. Observer de Especialidades adaptado al AutoCompleteTextView
        authViewModel.especialidades.observe(viewLifecycleOwner) { lista ->
            listaEspecialidades = lista
            val nombres = lista.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres)

            binding.spnEspecialidad.setAdapter(adapter)
            binding.spnEspecialidad.setOnItemClickListener { _, _, position, _ ->
                val seleccion = adapter.getItem(position)

                especialidadSeleccionada = listaEspecialidades.find { it.nombre == seleccion }

                // IMPRIME ESTO EN EL LOGCAT: Verificamos el ID de la especialidad
                Log.d("DEBUG_SELECCION", "Especialidad: $seleccion | ID Encontrado: ${especialidadSeleccionada?.id}")

                verificarExistenciaRegistro()
            }
        }
    }

    private fun setupListeners() {
        binding.btnGuardarMedico.setOnClickListener {
            ocultarTeclado()

            if (unidadSeleccionada == null || especialidadSeleccionada == null) {
                Toast.makeText(context, "Debe seleccionar la Unidad Médica y la Especialidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val estadoSeleccionado = binding.switchActivo.isChecked

            val ejecutarValidacion = {
                authViewModel.validarFormularioUnidadEspecialidad(
                    cupoDiarioStr = binding.etCupoDiario.text.toString()
                )
            }

            // GESTIÓN DE ELIMINACIÓN LÓGICA INTERMEDIA
            if (estadoOriginal && !estadoSeleccionado) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirmar Desactivación")
                    .setMessage("¿Está seguro de desactivar esta especialidad en la unidad médica? No se podrán agendar citas en este rubro.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Desactivar") { _, _ -> ejecutarValidacion() }
                    .show()
            } else {
                ejecutarValidacion()
            }
        }

        binding.btnCancelarMedico.setOnClickListener { resetearInterfaz() }
    }

    private fun verificarExistenciaRegistro() {
        val uId = unidadSeleccionada?.id
        val eId = especialidadSeleccionada?.id

        // Si se han seleccionado ambos nodos, disparamos la búsqueda reactiva en el ViewModel
        if (uId != null && eId != null && !isEditMode) {
            authViewModel.buscarUnidadEspecialidad(uId, eId)
        }
    }

    private fun llenarFormulario(registro: UnidadEspecialidadResponse) {
        this.isEditMode = true
        this.idUnidadEspecialidadActual = registro.id
        this.estadoOriginal = registro.activo

        with(binding) {
            tvTitulo.text = "Editar Unidad Especialidad"
            etCupoDiario.setText(registro.cupo_diario.toString())
            switchActivo.isChecked = registro.activo

            // RESTRICCIÓN DE NEGOCIO: Inhabilitar alteración de llaves foráneas en edición
            acUnidadMedica.isEnabled = false
            layoutEspecialidad.isEnabled = false

            // Modificación del cupo de cada unidad de especialidad habilitado según el requerimiento
            val esActivo = registro.activo
            etCupoDiario.isEnabled = esActivo
            switchActivo.isEnabled = true

            if (!esActivo) {
                Toast.makeText(context, "Asignación inactiva. Actívela para editar sus datos.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun enviarActualizacionAlServidor(unidadId: Int, especialidadId: Int) {
        // Construimos el objeto usando los nuevos nombres con guion bajo
        val requestBody = UnidadEspecialidadRequest(
            unidad_medica_id = unidadId,
            especialidad_id = especialidadId,
            cupo_diario = binding.etCupoDiario.text.toString().toIntOrNull() ?: 20,
            activo = binding.switchActivo.isChecked
        )

        Log.d("API_PAYLOAD", "Enviando con guiones bajos nativos: $requestBody")

        if (isEditMode && idUnidadEspecialidadActual != null) {
            authViewModel.ejecutarActualizacionUnidadEspecialidad(idUnidadEspecialidadActual!!, requestBody)
        } else {
            authViewModel.ejecutarCreacionUnidadEspecialidad(requestBody)
        }
    }

    override fun resetearInterfaz() {
        limpiarCampos(binding.etCupoDiario, binding.acUnidadMedica, binding.spnEspecialidad)
        binding.switchActivo.isChecked = true
        binding.tvTitulo.text = "Unidad Especialidad"

        // Restauración total de controles de entrada de la UI
        binding.acUnidadMedica.isEnabled = true
        binding.layoutEspecialidad.isEnabled = true
        binding.etCupoDiario.isEnabled = true

        unidadSeleccionada = null
        especialidadSeleccionada = null
        idUnidadEspecialidadActual = null
        isEditMode = false
        estadoOriginal = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}