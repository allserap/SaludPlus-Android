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

    private var listaUnidadesEspecialidad: List<UnidadEspecialidadResponse> = emptyList()
    private var listaUnidades: List<UnidadMedicaResponse> = emptyList()
    private var listaEspecialidades: List<EspecialidadResponse> = emptyList()

    private var unidadEspecialidadSeleccionada: UnidadEspecialidadResponse? = null

    private var isEditMode = false
    private var estadoOriginal: Boolean = true
    // endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUnidadesEspecialidadBinding.bind(view)

        setupObservers()
        setupUnidadEspecialidadObserver()
        setupListeners()

        // Disparar la carga inicial de los catálogos y la tabla intermedia
        authViewModel.cargarCatalogos()
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnGuardarUnidad.isEnabled = !isLoading
            binding.btnGuardarUnidad.text = if (isLoading) "Procesando..." else "Guardar"
        }

        authViewModel.formState.observe(viewLifecycleOwner) { estado ->
            with(binding) {
                etCupoDiario.error = estado.cupoDiarioError

                if (estado.isValid) {
                    val r = unidadEspecialidadSeleccionada
                    if (r != null) {
                        enviarActualizacionAlServidor(r.unidad_medica_id, r.especialidad_id)
                    } else {
                        Toast.makeText(requireContext(), "Por favor, seleccione una Unidad Especialidad válida.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        authViewModel.registroExitoso.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                resetearInterfaz()
                authViewModel.cargarCatalogos() // Refrescar fuentes de datos de forma limpia
            }
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupUnidadEspecialidadObserver() {
        authViewModel.unidadesMedicas.observe(viewLifecycleOwner) { unidades ->
            if (unidades != null) {
                listaUnidades = unidades
                refrescarSpinnerIntermedio()
            }
        }

        authViewModel.especialidades.observe(viewLifecycleOwner) { especialidades ->
            if (especialidades != null) {
                listaEspecialidades = especialidades
                refrescarSpinnerIntermedio()
            }
        }

        authViewModel.unidadesEspecialidad.observe(viewLifecycleOwner) { lista ->
            if (lista != null) {
                listaUnidadesEspecialidad = lista
                refrescarSpinnerIntermedio()
            }
        }
    }

    private fun refrescarSpinnerIntermedio() {
        if (listaUnidades.isEmpty() || listaEspecialidades.isEmpty() || listaUnidadesEspecialidad.isEmpty()) {
            return
        }

        // Llenar el Spinner de Unidades Médicas con sus nombres limpios
        val sugerenciasUnidades = listaUnidades.map { it.unidadMedica }
        val adapterUnidades = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerenciasUnidades)
        binding.acUnidadMedica.setAdapter(adapterUnidades)

        // Llenar el Spinner de Especialidades con sus nombres limpios
        val sugerenciasEspecialidades = listaEspecialidades.map { it.nombre }
        val adapterEspecialidades = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerenciasEspecialidades)
        binding.spnEspecialidad.setAdapter(adapterEspecialidades)

        // Escuchador para cuando seleccionan una Unidad
        binding.acUnidadMedica.setOnItemClickListener { _, _, position, _ ->
            evaluarCombinacionSeleccionada()
        }

        // 4. Escuchador para cuando seleccionan una Especialidad
        binding.spnEspecialidad.setOnItemClickListener { _, _, position, _ ->
            evaluarCombinacionSeleccionada()
        }
    }

    private fun evaluarCombinacionSeleccionada() {
        val textoUnidad = binding.acUnidadMedica.text.toString()
        val textoEspecialidad = binding.spnEspecialidad.text.toString()

        // Buscar los IDs correspondientes en los catálogos maestros basados en el texto seleccionado
        val unidadId = listaUnidades.find { it.unidadMedica == textoUnidad }?.id
        val especialidadId = listaEspecialidades.find { it.nombre == textoEspecialidad }?.id

        if (unidadId != null && especialidadId != null) {
            // Buscamos si existe la combinación en la tabla intermedia
            val registroExistente = listaUnidadesEspecialidad.find {
                it.unidad_medica_id == unidadId && it.especialidad_id == especialidadId
            }

            if (registroExistente != null) {
                unidadEspecialidadSeleccionada = registroExistente
                idUnidadEspecialidadActual = registroExistente.id

                // Pasamos a modo edición automáticamente con los datos del servidor
                this.isEditMode = true
                this.estadoOriginal = registroExistente.activo

                binding.tvTitulo.text = "Editar Unidad Especialidad"
                binding.etCupoDiario.setText(registroExistente.cupo_diario.toString())
                binding.etCupoDiario.isEnabled = registroExistente.activo

                Log.d("COMBINACION", "Match encontrado: ID ${registroExistente.id}")
            } else {
                // Si la combinación no existe, preparamos el formulario para una nueva asignación (Modo Creación)
                idUnidadEspecialidadActual = null
                unidadEspecialidadSeleccionada = null
                this.isEditMode = false

                binding.tvTitulo.text = "Nueva Asignación"
                binding.etCupoDiario.setText("20") // Cupo por defecto
                binding.etCupoDiario.isEnabled = true

                Log.d("COMBINACION", "Nueva combinación detectada")
            }
        }
    }

    private fun setupListeners() {
        binding.btnGuardarUnidad.setOnClickListener {
            ocultarTeclado()

            if (unidadEspecialidadSeleccionada == null) {
                Toast.makeText(context, "Debe seleccionar una Unidad Especialidad de la lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ejecutarValidacion = {
                authViewModel.validarFormularioUnidadEspecialidad(
                    cupoDiarioStr = binding.etCupoDiario.text.toString()
                )
            }

            ejecutarValidacion()
        }

        binding.btnCancelarUnidad.setOnClickListener { resetearInterfaz() }
    }

    private fun enviarActualizacionAlServidor(unidadId: Int, especialidadId: Int) {
        val requestBody = UnidadEspecialidadRequest(
            unidad_medica_id = unidadId,
            especialidad_id = especialidadId,
            cupo_diario = binding.etCupoDiario.text.toString().toIntOrNull() ?: 20,
            activo = estadoOriginal
        )

        Log.d("API_PAYLOAD", "Enviando payload con snake_case nativo: $requestBody")

        if (isEditMode && idUnidadEspecialidadActual != null) {
            authViewModel.ejecutarActualizacionUnidadEspecialidad(idUnidadEspecialidadActual!!, requestBody)
        }
    }

    override fun resetearInterfaz() {
        limpiarCampos(binding.etCupoDiario, binding.acUnidadMedica, binding.spnEspecialidad)
        binding.tvTitulo.text = "Unidad Especialidad"

        binding.acUnidadMedica.isEnabled = true
        binding.layoutEspecialidad.isEnabled = true
        binding.etCupoDiario.isEnabled = true

        unidadEspecialidadSeleccionada = null
        idUnidadEspecialidadActual = null
        isEditMode = false
        estadoOriginal = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}