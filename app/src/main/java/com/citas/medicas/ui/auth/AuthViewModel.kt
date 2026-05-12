package com.citas.medicas.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citas.medicas.data.AuthRepository
import com.citas.medicas.models.EspecialidadResponse
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.models.PacienteUpdateRequest
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.RolResponse
import com.citas.medicas.models.UnidadMedicaResponse
import com.citas.medicas.utils.RolesUsuario
import com.citas.medicas.utils.Validation
import kotlinx.coroutines.launch

data class FormularioState(
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val duiError: String? = null,
    val correoError: String? = null,
    val telefonoError: String? = null,
    val passwordError: String? = null,
    val jvpmError: String? = null,
    val afiliadoError: String? = null,
    val especialidadError: String? = null,
    val unidadError: String? = null,
    val isValid: Boolean = false
)
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    //region inicializaciones
    // permitir que el RetrofitClient acceda a "CitasMedicasPrefs"
    private val repository = AuthRepository(application.applicationContext)
    private val _registroExitoso = MutableLiveData<String?>()
    val registroExitoso: LiveData<String?> get() = _registroExitoso
    private val _isLoading = MutableLiveData<Boolean>()

    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // --- Mostrar médicos ---
    private val _listaMedicos = MutableLiveData<List<MedicoResponse>>()
    val listaMedicos: LiveData<List<MedicoResponse>> = _listaMedicos

    // --- Mostar pacientes ---
    private val _listaPacientes = MutableLiveData<List<PacienteResponse>>()
    val listaPacientes: LiveData<List<PacienteResponse>> = _listaPacientes

    // --- Mostrar catálogos ---
    private val _especialidades = MutableLiveData<List<EspecialidadResponse>>()
    val especialidades: LiveData<List<EspecialidadResponse>> = _especialidades

    private val _unidadesMedicas = MutableLiveData<List<UnidadMedicaResponse>>()
    val unidadesMedicas: LiveData<List<UnidadMedicaResponse>> = _unidadesMedicas

    private val _roles = MutableLiveData<List<RolResponse>>()
    val roles: LiveData<List<RolResponse>> = _roles

    //endregion

    // regionEjecuciones
    fun ejecutarRegistro(datos: RegistroRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            val resultado = if (datos.rol == 2) {
                repository.registrarMedico(datos)
            } else {
                repository.registrarPaciente(datos)
            }
            resultado.onSuccess { mensaje ->
                _registroExitoso.value = mensaje
            }.onFailure { excepcion ->
                _error.value = excepcion.message
            }
            _isLoading.value = false
        }
    }

    fun ejecutarActualizacion(datos: MedicoUpdateRequest) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val resultado = repository.actualizarMedico(datos)

                resultado.onSuccess { mensaje ->
                    _registroExitoso.value = mensaje
                    cargarMedicos()
                }.onFailure { excepcion ->
                    _error.value = excepcion.message ?: "Error desconocido al actualizar"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarPaciente(datos: PacienteUpdateRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resultado = repository.actualizarPaciente(datos)

                resultado.onSuccess { mensaje ->
                    // Notificamos éxito a la UI
                    _registroExitoso.value = Unit.toString()
                }.onFailure { error ->
                    // Notificamos el error
                    _error.value = error.message ?: "Error desconocido"
                }
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    //endregion

    // region Cargar datos
    fun cargarPacientes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.obtenerPacientes()
                if (response.success) {
                    _listaPacientes.value = response.data
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarMedicos() {
        viewModelScope.launch {
            try {
                val res = repository.obtenerMedicos()
                if (res.isSuccessful && res.body() != null) {
                    val listado = res.body()!!.data

                    _listaMedicos.postValue(listado)
                    Log.d("DEBUG_VM", "Médicos cargados: ${listado.size}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_VM", "Error al cargar", e)
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun cargarCatalogos() {
        viewModelScope.launch {
            try {
                // obtener especialidades
                val resEsp = repository.obtenerEspecialidades()
                if (resEsp.isSuccessful) {
                    val listaReal = resEsp.body()?.data ?: emptyList()
                    _especialidades.postValue(listaReal)
                    Log.d("DEBUG_API", "Especialidades cargadas: ${listaReal.size}")
                }

                // obtener unidades médicas
                val resUni = repository.obtenerUnidadesMedicas()
                if (resUni.isSuccessful) {
                    val listaReal = resUni.body()?.data ?: emptyList()
                    _unidadesMedicas.postValue(listaReal)
                    Log.d("DEBUG_API", "Unidades cargadas: ${listaReal.size}")
                }

                // obtener roles
                val resRol = repository.obtenerRoles()
                if (resRol.isSuccessful) {
                    val listaReal = resRol.body()?.data ?: emptyList()
                    RolesUsuario.inicializar(listaReal)
                    _roles.postValue(listaReal)
                    Log.d("DEBUG_API", "Roles cargados: ${listaReal.size}")
                    Log.d("DEBUG_API", "ID Paciente asignado: ${RolesUsuario.ID_PACIENTE}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_API", "Error de red: ${e.message}")
                _error.postValue("Sin conexión al servidor")
            }
        }
    }
    //endregion

    // region Validacion
    private val _formState = MutableLiveData<FormularioState>()
    val formState: LiveData<FormularioState> get() = _formState

    fun validarFormulario(
        rolId: Int,
        nombres: String,
        apellidos: String,
        dui: String,
        correo: String,
        telefono: String,
        password: String,
        extraCampo: String,
        especialidadPos: Int,
        unidadPos: Int
    ) {
        var estado = FormularioState()
        var hayError = false

        if (nombres.isBlank()) {
            estado = estado.copy(nombreError = "Nombre requerido"); hayError = true
        }
        if (apellidos.isBlank()) {
            estado = estado.copy(apellidoError = "Apellido requerido"); hayError = true
        }
        if (!Validation.isValidDUI(dui)) {
            estado = estado.copy(duiError = "Formato de DUI incorrecto (00000000-0)"); hayError =
                true
        }
        if (!Validation.isValidEmail(correo)) {
            estado = estado.copy(correoError = "Correo inválido"); hayError = true
        }
        if (!Validation.isValidPassword(password)) {
            estado =
                estado.copy(passwordError = "Mínimo 8 caracteres, 1 mayúscula y 1 símbolo"); hayError =
                true
        }
        if (!Validation.isValidPhone(telefono)) {
            estado = estado.copy(telefonoError = "Formato inválido (0000-0000)")
        }
        if (especialidadPos <= 0) {
            estado = estado.copy(especialidadError = "Especialidad requerida"); hayError = true
        }
        if (unidadPos <= 0) {

        }

        when (rolId) {
            RolesUsuario.ID_MEDICO -> if (extraCampo.length < 4) {
                estado = estado.copy(jvpmError = "JVPM inválido"); hayError = true
            }

            RolesUsuario.ID_PACIENTE -> if (extraCampo.isEmpty()) {
                estado = estado.copy(afiliadoError = "Requerido"); hayError = true
            }
        }
        _formState.value = estado.copy(isValid = !hayError)
    }
// endregion
}