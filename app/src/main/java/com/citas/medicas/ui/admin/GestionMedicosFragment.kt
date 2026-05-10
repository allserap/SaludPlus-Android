package com.citas.medicas.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentGestionMedicosBinding
import com.citas.medicas.models.EspecialidadResponse
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.models.UnidadMedicaResponse
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment
import com.citas.medicas.utils.RolesUsuario
import com.citas.medicas.utils.aplicarMascaraDUI
import com.citas.medicas.utils.aplicarMascaraTelefono
import com.citas.medicas.utils.configurarConHint
import com.citas.medicas.utils.limpiarCampos
import com.citas.medicas.utils.showDatePickerDialog
import kotlin.getValue

class GestionMedicosFragment : BaseFragment(R.layout.fragment_gestion_medicos) {

    // regionInicializacion
    private var _binding: FragmentGestionMedicosBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    private var medicoIdSeleccionado: String? = null
    private var listaEspecialidades: List<EspecialidadResponse> = emptyList()
    private var listaUnidades: List<UnidadMedicaResponse> = emptyList()
    private var estadoOriginal: Boolean = true

    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGestionMedicosBinding.bind(view)

        setupObservers()
        setupUI()
        setupCatalogosObservers()
        setupListeners()

        authViewModel.cargarCatalogos()
        authViewModel.cargarMedicos()
    }

    private fun setupUI() {
        binding.etDui.aplicarMascaraDUI()
        binding.etTelefono.aplicarMascaraTelefono()

        val opcionesGenero = arrayOf("Masculino", "Femenino", "Otro")
        binding.spnGenero.configurarConHint(opcionesGenero, "Seleccione género")

        val opcionesRol = arrayOf("Paciente", "Médico", "Administrador")
        binding.spnRol.configurarConHint(opcionesRol, "Seleccione Rol")

        val opcionesEstado = arrayOf("Activo", "Inactivo")
        binding.spnEstado.configurarConHint(opcionesEstado, "Seleccione Estado")
    }

    private fun setupObservers() {
        // Feedback durante la actualización
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGuardarMedico.isEnabled = !isLoading
            binding.btnGuardarMedico.text = if (isLoading) "Actualizando..." else "Actualizar Datos"
        }

        // Estado de validación del formulario
        authViewModel.formState.observe(viewLifecycleOwner) { estado ->
            with(binding) {
                etNombreMedico.error = estado.nombreError
                etApellidoMedico.error = estado.apellidoError
                etDui.error = estado.duiError
                etEmail.error = estado.correoError
                etTelefono.error = estado.telefonoError
                etPassword.error = estado.passwordError
                etJvpm.error = estado.jvpmError

                estado.especialidadError?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
                estado.unidadError?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }

                if (estado.isValid) {
                    enviarActualizacionAlServidor()
                }
            }
        }

        // Lista de médicos para el buscador
        authViewModel.listaMedicos.observe(viewLifecycleOwner) { medicos ->
            if (!medicos.isNullOrEmpty()) {
                configurarBuscador(medicos)
            }
        }

        authViewModel.registroExitoso.observe(viewLifecycleOwner) {
            Toast.makeText(context, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            resetearInterfaz()
            authViewModel.cargarMedicos() // Recargar lista para el buscador
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupCatalogosObservers() {
        authViewModel.especialidades.observe(viewLifecycleOwner) { lista ->
            listaEspecialidades = lista
            binding.spnEspecialidad.configurarConHint(lista.map { it.nombre }.toTypedArray(), "Especialidad")
        }

        authViewModel.unidadesMedicas.observe(viewLifecycleOwner) { lista ->
            listaUnidades = lista
            binding.spnUnidad.configurarConHint(lista.map { it.nombreCompleto }.toTypedArray(), "Unidad Médica")
        }
    }

    private fun setupListeners() {
        binding.etFechaNac.setOnClickListener { showDatePickerDialog(requireContext(), binding.etFechaNac) }

        binding.btnGuardarMedico.setOnClickListener {
            ocultarTeclado()
            val idActual = medicoIdSeleccionado
            if (idActual == null) {
                Toast.makeText(context, "Selecciona un médico primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordInput = binding.etPassword.text.toString()
            val estadoSeleccionado = binding.spnEstado.selectedItem.toString() == "Activo"

            val ejecutarValidacion = {
                    authViewModel.validarFormulario(
                        rolId = RolesUsuario.ID_MEDICO,
                        nombres = binding.etNombreMedico.text.toString(),
                        apellidos = binding.etApellidoMedico.text.toString(),
                        dui = binding.etDui.text.toString(),
                        correo = binding.etEmail.text.toString(),
                        telefono = binding.etTelefono.text.toString(),
                        password = if (passwordInput.isEmpty()) "PasswordOpcional123*" else passwordInput,
                        extraCampo = binding.etJvpm.text.toString(),
                        especialidadPos = binding.spnEspecialidad.selectedItemPosition,
                        unidadPos = binding.spnUnidad.selectedItemPosition
                    )
            }

            // LÓGICA DE ELIMINACIÓN LÓGICA
            if (estadoOriginal && !estadoSeleccionado) {
                // Si antes era Activo y ahora es Inactivo, advertimos
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirmar Desactivación")
                    .setMessage("¿Está seguro de desactivar a este médico? No podrá iniciar sesión hasta que sea reactivado.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Desactivar") { _, _ -> ejecutarValidacion() }
                    .show()
            } else {
                ejecutarValidacion()
            }
        }
        binding.btnCancelarMedico.setOnClickListener { resetearInterfaz() }
    }

    private fun configurarBuscador(medicos: List<MedicoResponse>) {
        val sugerencias = medicos.map { "${it.nombre} ${it.apellido} (${it.dui})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerencias)

        binding.autoCompleteMedicos.setAdapter(adapter)
        binding.autoCompleteMedicos.setOnItemClickListener { _, _, position, _ ->
            // Buscamos el médico original basado en la sugerencia seleccionada
            val seleccion = adapter.getItem(position)
            val medico = medicos.find { "${it.nombre} ${it.apellido} (${it.dui})" == seleccion }
            medico?.let {
                this.medicoIdSeleccionado = it.usuarioId
                llenarFormulario(it) }
        }
    }

    private fun llenarFormulario(medico: MedicoResponse) {
        this.medicoIdSeleccionado = medico.usuarioId
        val fechaLimpia = medico.fechaNacimiento?.split("T")?.get(0) ?: ""
        this.medicoIdSeleccionado = medico.usuarioId
        this.estadoOriginal = medico.activo


        with(binding) {
            etNombreMedico.setText(medico.nombre)
            etApellidoMedico.setText(medico.apellido)
            etDui.setText(medico.dui)
            etDui.aplicarMascaraDUI()
            etTelefono.setText(medico.telefono)
            etEmail.setText(medico.email)
            etFechaNac.setText(fechaLimpia)
            etJvpm.setText(medico.numJvpm)
            val esActivo = medico.activo
            etNombreMedico.isEnabled = esActivo
            etApellidoMedico.isEnabled = esActivo
            etDui.isEnabled = esActivo
            etEmail.isEnabled = esActivo
            etTelefono.isEnabled = esActivo
            etJvpm.isEnabled = esActivo
            spnRol.isEnabled = esActivo
            spnGenero.isEnabled = esActivo
            spnEspecialidad.isEnabled = esActivo
            spnUnidad.isEnabled = esActivo
            // habilitado para poder reactivarlo
            spnEstado.isEnabled = true

            if (!esActivo) {
                Toast.makeText(context, "Usuario inactivo. Actívelo para editar sus datos.", Toast.LENGTH_LONG).show()
            }
            val posRol = when(medico.rolId) {
                1 -> 1 // Paciente
                2 -> 2 // Médico
                3 -> 3 // Administrador
                else -> 0
            }
            spnRol.setSelection(posRol)

            val posEstado = if (medico.activo) 1 else 2
            spnEstado.setSelection(posEstado)

            // Género
            val posGenero = if (medico.genero == "M") 1 else 2
            spnGenero.setSelection(posGenero)

            // Especialidad (por nombre o ID)
            authViewModel.especialidades.value?.let { lista ->
                val index = lista.indexOfFirst { it.id == medico.especialidadId }
                if (index != -1) spnEspecialidad.setSelection(index + 1)
            }

            // Unidad Médica
            authViewModel.unidadesMedicas.value?.let { lista ->
                val index = lista.indexOfFirst { it.id == medico.unidadMedicaId }
                if (index != -1) spnUnidad.setSelection(index + 1)
            }

            btnGuardarMedico.text = "Actualizar Datos"
        }
    }

    private fun enviarActualizacionAlServidor() {
        val passwordTexto = binding.etPassword.text.toString()
        val posEsp = binding.spnEspecialidad.selectedItemPosition - 1
        val posUni = binding.spnUnidad.selectedItemPosition - 1
        val generoRaw = binding.spnGenero.selectedItem.toString()
        val idRolFinal = binding.spnRol.selectedItemPosition
        val estadoSeleccionado = binding.spnEstado.selectedItem.toString() == "Activo"
        val fechaStr = binding.etFechaNac.text.toString().trim()

        val updateRequest = MedicoUpdateRequest(
            id = (medicoIdSeleccionado ?: 0).toString(),
            nombre = binding.etNombreMedico.text.toString().trim(),
            apellido = binding.etApellidoMedico.text.toString().trim(),
            dui = binding.etDui.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            password = if (passwordTexto.isNotEmpty()) passwordTexto else null,
            telefono = binding.etTelefono.text.toString().trim(),
            fechaNacimiento = if (fechaStr.isNotEmpty()) fechaStr else null,
            genero = if (generoRaw == "Masculino") "M" else "F",
            numJvpm = binding.etJvpm.text.toString().trim(),
            especialidad = listaEspecialidades.getOrNull(posEsp)?.id ?: 0,
            unidadMedica = listaUnidades.getOrNull(posUni)?.id ?: 0,
            rol = idRolFinal,
            activo = estadoSeleccionado
        )

        authViewModel.ejecutarActualizacion(updateRequest)
    }

    override fun resetearInterfaz() {
        limpiarCampos(
            binding.etNombreMedico, binding.etApellidoMedico, binding.etDui,
            binding.etFechaNac, binding.etEmail, binding.etTelefono,
            binding.etJvpm, binding.etPassword
        )
        binding.spnGenero.setSelection(0)
        binding.spnEspecialidad.setSelection(0)
        binding.spnUnidad.setSelection(0)
        binding.spnRol.setSelection(0)
        binding.spnEstado.setSelection(0)
        binding.autoCompleteMedicos.setText("")
        medicoIdSeleccionado = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



