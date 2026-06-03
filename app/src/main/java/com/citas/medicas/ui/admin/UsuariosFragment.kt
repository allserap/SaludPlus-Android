package com.citas.medicas.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentUsuariosBinding
import com.citas.medicas.models.EspecialidadResponse
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.RolResponse
import com.citas.medicas.models.UnidadMedicaResponse
import com.citas.medicas.models.Usuario
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment
import com.citas.medicas.utils.RolesUsuario
import com.citas.medicas.utils.Validation.isValidDUI
import com.citas.medicas.utils.Validation.isValidEmail
import com.citas.medicas.utils.Validation.isValidPassword
import com.citas.medicas.utils.Validation.isValidPhone
import com.citas.medicas.utils.aplicarMascaraDUI
import com.citas.medicas.utils.aplicarMascaraTelefono
import com.citas.medicas.utils.cambiarColor
import com.citas.medicas.utils.configurarConHint
import com.citas.medicas.utils.limpiarCampos
import com.citas.medicas.utils.showDatePickerDialog
import kotlinx.coroutines.launch

class UsuariosFragment : BaseFragment(R.layout.fragment_usuarios) {

    //region Inicializaciones
    private var _binding: FragmentUsuariosBinding? = null
    private val binding get() = _binding!!

    private var isFormVisible = false
    private val authViewModel: AuthViewModel by viewModels()

    private var listaEspecialidades: List<EspecialidadResponse> = emptyList()
    private var listaUnidades: List<UnidadMedicaResponse> = emptyList()
    private var listaRoles: List<RolResponse> = emptyList()

    private lateinit var usuarioAdapter: UsuariosAdapter

    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsuariosBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        setupCatalogosObservers()
        setupMedicosObserver()
        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Ejecutamos las peticiones en paralelo para no bloquear el renderizado visual
                authViewModel.cargarCatalogos()
                authViewModel.cargarMedicos()
                authViewModel.cargarPacientes()

                android.util.Log.d("FRAGMENT_DEBUG", "Peticiones de arranque disparadas con éxito")
            } catch (e: Exception) {
                android.util.Log.e("FRAGMENT_DEBUG", "Error al inicializar datos: ${e.message}")
            }
        }
    }

    //regionSetups
    private fun setupRecyclerView() {
        // Inicializar el adaptador vacío
        usuarioAdapter = UsuariosAdapter(emptyList())
        binding.rvUsuarios.apply {
            // Obligatorio para que se vea algo
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usuarioAdapter
            setHasFixedSize(true)
        }
    }
    private fun setupObservers() {
        //feedback durante el registro
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCrear.isEnabled = !isLoading

            binding.btnCrear.text = if (isLoading) "Procesando..." else "Crear Cuenta"
        }

        authViewModel.formState.observe(viewLifecycleOwner) { estado ->
            with(binding) {
                etNombres.error = estado.nombreError
                etApellidos.error = estado.apellidoError
                etDui.error = estado.duiError
                etCorreo.error = estado.correoError
                etTelefono.error = estado.telefonoError
                etPassword.error = estado.passwordError
                etJvpm.error = estado.jvpmError
                estado.especialidadError?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
                estado.unidadError?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }

                if (estado.isValid) {
                    enviarRegistroMedicoAlServidor()
                }
            }
        }

        authViewModel.registroExitoso.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje != null) {
                Toast.makeText(requireContext(), "Médico creado exitosamente", Toast.LENGTH_SHORT).show()
                toggleFormulario()
            }
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupMedicosObserver() {
        authViewModel.listaMedicos.observe(viewLifecycleOwner) { medicos ->
            if (medicos != null && medicos.isNotEmpty()) {
                val soloMedicos = medicos.filter { it.rolId == RolesUsuario.ID_MEDICO }
                usuarioAdapter.updateList(soloMedicos)
                Log.d("DEBUG_VIEW", "Lista actualizada en el Adapter con ${medicos.size} médicos")
            } else {
                Log.d("DEBUG_VIEW", "La lista de médicos llegó vacía o nula al Fragment")
            }
        }
    }

    private fun setupCatalogosObservers() {
        val opcionesGenero = arrayOf("Masculino", "Femenino", "Otro")
        binding.spnGenero.configurarConHint(opcionesGenero, "Seleccione su género")

        // Observar Especialidades
        authViewModel.especialidades.observe(viewLifecycleOwner) { lista ->
            listaEspecialidades = lista
            val nombres = lista.map { it.nombre }.toTypedArray()
            binding.spnEspecialidad.configurarConHint(nombres, "Seleccione especialidad")
        }

        // Observar Unidades Médicas
        authViewModel.unidadesMedicas.observe(viewLifecycleOwner) { lista ->
            listaUnidades = lista
            val nombres = lista.map { it.unidadMedica }.toTypedArray()
            binding.spnUnidad.configurarConHint(nombres, "Seleccione unidad médica")
        }

        // Obtener roles
        authViewModel.roles.observe(viewLifecycleOwner) { lista ->
            listaRoles = lista
        }
    }

    private fun setupListeners() {
        binding.etTelefono.aplicarMascaraTelefono()
        binding.etDui.aplicarMascaraDUI()
        binding.etFechaNac.setOnClickListener {
            showDatePickerDialog(requireContext(),binding.etFechaNac)
        }
        // Botón superior: Nuevo / Cancelar
        binding.btnNuevoUsuario.setOnClickListener {
            binding.btnNuevoUsuario.cambiarColor(R.color.citas_secondary)
            toggleFormulario()
        }
        // Botón Crear Usuario
        binding.btnCrear.setOnClickListener {
            ocultarTeclado()
            authViewModel.validarFormulario(
                rolId = RolesUsuario.ID_MEDICO,
                nombres = binding.etNombres.text.toString(),
                apellidos = binding.etApellidos.text.toString(),
                dui = binding.etDui.text.toString(),
                correo = binding.etCorreo.text.toString(),
                telefono = binding.etTelefono.text.toString(),
                password = binding.etPassword.text.toString(),
                extraCampo = binding.etJvpm.text.toString(),
                especialidadPos = binding.spnEspecialidad.selectedItemPosition,
                unidadPos = binding.spnUnidad.selectedItemPosition
            )
        }

        binding.etJvpm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ocultarTeclado()
                true
            } else false
        }
    }

    //endregion
    private fun toggleFormulario() {
        ocultarTeclado()
        isFormVisible = !isFormVisible
        setVisibilidad(binding.cardFormulario, isFormVisible)

        if (isFormVisible) {
            binding.btnNuevoUsuario.text = "Cancelar"
            binding.btnNuevoUsuario.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.citas_secondary))
        } else {
            binding.btnNuevoUsuario.text = "Nuevo"
            resetearInterfaz()
        }
    }

    override fun resetearInterfaz() {
        // limpiarCampos del BaseFragment pasando todos los EditTexts
        limpiarCampos(
            binding.etNombres, binding.etApellidos, binding.etDui,
            binding.etFechaNac, binding.etCorreo, binding.etTelefono,
            binding.etJvpm, binding.etPassword
        )
        // Resetear Spinners
        binding.spnGenero.setSelection(0)
        binding.spnEspecialidad.setSelection(0)
        binding.spnUnidad.setSelection(0)

        if (isFormVisible) toggleFormulario()
    }

    private fun enviarRegistroMedicoAlServidor() {
        with(binding) {
            // Obtener las posiciones de spinners
            val posEsp = spnEspecialidad.selectedItemPosition - 1
            val posUni = spnUnidad.selectedItemPosition - 1
            val generoSeleccionado = binding.spnGenero.selectedItem.toString()
            val generoFinal = when(generoSeleccionado) {
                "Masculino" -> "M"
                "Femenino" -> "F"
                else -> "O"
            }

            val nuevoUsuario = RegistroRequest(
                nombre = etNombres.text.toString().trim(),
                apellido = etApellidos.text.toString().trim(),
                dui = etDui.text.toString().trim(),
                email = etCorreo.text.toString().trim(),
                password = etPassword.text.toString(),
                telefono = etTelefono.text.toString().trim(),
                fechaNacimiento = etFechaNac.text.toString(),
                numJvpm = etJvpm.text.toString().trim(),
                genero = generoFinal,

                // posiciones reales de los spinners para los IDs
                especialidad = listaEspecialidades.getOrNull(posEsp)?.id ?: 0,
                unidadMedica = listaUnidades.getOrNull(posUni)?.id ?: 0,

                rol = RolesUsuario.ID_MEDICO
            )

            authViewModel.ejecutarRegistro(nuevoUsuario)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
