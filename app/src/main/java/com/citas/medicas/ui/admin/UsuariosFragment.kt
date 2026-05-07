package com.citas.medicas.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
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
import com.citas.medicas.utils.cambiarColor
import com.citas.medicas.utils.configurarConHint

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

        authViewModel.cargarCatalogos()
        authViewModel.cargarMedicos()
    }

    //regionSetups
    private fun setupRecyclerView() {
        // Inicializar el adaptador vacío al principio
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

        authViewModel.registroExitoso.observe(viewLifecycleOwner) { mensaje ->
            Toast.makeText(requireContext(), "Médico creado: $mensaje", Toast.LENGTH_SHORT).show()
            toggleFormulario()
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupMedicosObserver() {
        authViewModel.listaMedicos.observe(viewLifecycleOwner) { medicos ->
            if (medicos != null && medicos.isNotEmpty()) {
                // Importante: Usa la variable que declaraste arriba
                usuarioAdapter.updateList(medicos)
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
            val nombres = lista.map { it.nombreCompleto }.toTypedArray()
            binding.spnUnidad.configurarConHint(nombres, "Seleccione unidad médica")
        }

        // Obtener roles
        authViewModel.roles.observe(viewLifecycleOwner) { lista ->
            listaRoles = lista
        }
    }

    private fun setupListeners() {
        aplicarMascaraTelefono(binding.etTelefono)
        aplicarMascaraDUI(binding.etDui)
        binding.etFechaNac.setOnClickListener {
            showDatePickerDialog(binding.etFechaNac)
        }
        // Botón superior: Nuevo / Cancelar
        binding.btnNuevoUsuario.setOnClickListener {
            binding.btnNuevoUsuario.cambiarColor(R.color.citas_secondary)
            toggleFormulario()
        }
        // Botón Crear Usuario
        binding.btnCrear.setOnClickListener {
            if (validarForm()) {
                enviarRegistroMedicoAlServidor()
            }
        }
    }

    //endregion
    private fun toggleFormulario() {
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
            val idRolMedico = listaRoles.find { it.nombre.equals("Medico", ignoreCase = true) }?.id ?: 0

            val generoSeleccionado = binding.spnGenero.selectedItem.toString()
            val generoFinal = when(generoSeleccionado) {
                "Masculino" -> "M"
                "Femenino" -> "F"
                else -> "O"
            }

            val especialidadId = listaEspecialidades.getOrNull(posEsp)?.id ?: 0
            val unidadId = listaUnidades.getOrNull(posUni)?.id ?: 0

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
                especialidad = especialidadId,
                unidadMedica = unidadId,

                rol = idRolMedico
            )

            authViewModel.ejecutarRegistro(nuevoUsuario)
        }
    }

    private fun validarForm(): Boolean {
        with(binding) {
            // Validaciones de Campos Requeridos
            val vNombre = validarRequerido(etNombres, "Ingrese los nombres")
            val vApellido = validarRequerido(etApellidos, "Ingrese los apellidos")
            val vFecha = validarRequerido(etFechaNac, "Seleccione fecha de nacimiento")

            // Validaciones con Lógica Especial (Regex y Longitud)
            val vDui = if (!isValidDUI(etDui.text.toString().trim())) {
                etDui.error = "Formato inválido (00000000-0)"
                false
            } else true

            val vJvpm = if (etJvpm.text.toString().trim().length < 4) {
                etJvpm.error = "JVPM inválido (Mínimo 4 dígitos)"
                false
            } else true

            val vCorreo = if (!isValidEmail(etCorreo.text.toString().trim())) {
                etCorreo.error = "Correo electrónico inválido"
                false
            } else true

            val vTelefono = if (!isValidPhone(etTelefono.text.toString().trim())) {
                etTelefono.error = "Formato inválido (0000-0000)"
                false
            } else true

            val vPass = if (!isValidPassword(etPassword.text.toString())) {
                etPassword.error = "Mínimo 8 caracteres, 1 mayúscula y 1 símbolo"
                false
            } else true

            // Validaciones de Spinners
            var vSpinners = true
            if (spnGenero.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Seleccione el género", Toast.LENGTH_SHORT).show()
                vSpinners = false
            } else if (spnEspecialidad.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Seleccione la especialidad", Toast.LENGTH_SHORT).show()
                vSpinners = false
            }else if (spnUnidad.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Seleccione la unidad médica", Toast.LENGTH_SHORT).show()
                vSpinners = false
            }

            // Retorna True solo si todas las variables son True
            return vNombre && vApellido && vFecha && vDui && vJvpm && vCorreo && vTelefono && vPass && vSpinners
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
