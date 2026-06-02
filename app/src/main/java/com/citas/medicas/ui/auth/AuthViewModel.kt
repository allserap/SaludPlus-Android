package com.citas.medicas.ui.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.citas.medicas.data.AuthRepository
import com.citas.medicas.models.AgendaCitasWrapper
import com.citas.medicas.models.EspecialidadResponse
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.models.PacienteUpdateRequest
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.RolResponse
import com.citas.medicas.models.UnidadMedicaResponse
import com.citas.medicas.models.UnidadEspecialidadRequest
import com.citas.medicas.models.UnidadEspecialidadResponse
import com.citas.medicas.models.ApiResponse
import com.citas.medicas.models.CitaItem
import com.citas.medicas.models.CitaResponse
import com.citas.medicas.models.DetalleRecetaItemRequest
import com.citas.medicas.models.HistoricoCitasResponse
import com.citas.medicas.models.MedicamentoResponse
import com.citas.medicas.models.RecetaRequest
import com.citas.medicas.utils.RolesUsuario
import com.citas.medicas.utils.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val cupoDiarioError: String? = null,
    val isValid: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // region Inicializaciones
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

    // --- Mostrar pacientes ---
    private val _listaPacientes = MutableLiveData<List<PacienteResponse>>()
    val listaPacientes: LiveData<List<PacienteResponse>> = _listaPacientes

    // --- Mostrar Citas ---
    private val _listaCitas = MutableLiveData<List<CitaResponse>>()
    val listaCitas: LiveData<List<CitaResponse>> get() = _listaCitas

    // --- Mostrar catálogos ---
    private val _especialidades = MutableLiveData<List<EspecialidadResponse>>()
    val especialidades: LiveData<List<EspecialidadResponse>> = _especialidades
    private val _unidadesMedicas = MutableLiveData<List<UnidadMedicaResponse>>()
    val unidadesMedicas: LiveData<List<UnidadMedicaResponse>> = _unidadesMedicas
    private val _roles = MutableLiveData<List<RolResponse>>()
    val roles: LiveData<List<RolResponse>> = _roles
    private val _unidadesEspecialidad = MutableLiveData<List<UnidadEspecialidadResponse>>()
    val unidadesEspecialidad: LiveData<List<UnidadEspecialidadResponse>> get() = _unidadesEspecialidad
    private val _listaMedicamentos = MutableLiveData<List<MedicamentoResponse>>()
    val listaMedicamentos: LiveData<List<MedicamentoResponse>> get() = _listaMedicamentos


    // --- Receta ---
    private val _recetaProcesadaExito = MutableLiveData<Boolean>()
    val recetaProcesadaExito: LiveData<Boolean> get() = _recetaProcesadaExito

    // --- Mostrar histórico ---
    private val _historicoCitas = MutableLiveData<List<HistoricoCitasResponse>>()
    val historicoCitas: LiveData<List<HistoricoCitasResponse>> get() = _historicoCitas
    // endregion

    // region Ejecuciones Médicos y Pacientes
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
                resultado.onSuccess {
                    _registroExitoso.value = Unit.toString()
                }.onFailure { error ->
                    _error.value = error.message ?: "Error desconocido"
                }
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarRecetaCompleta(
        pacienteId: Int,
        cabecera: RecetaRequest,
        constructorDetalles: (Int) -> List<DetalleRecetaItemRequest>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _isLoading.value = true }

            try {
                // 1. Petición de la Cabecera
                val responseCabecera = repository.crearRecetaCabecera(pacienteId, cabecera)

                if (responseCabecera.isSuccessful && responseCabecera.body()?.success == true) {
                    val recetaIdAsignado = responseCabecera.body()?.data?.recetaId

                    if (recetaIdAsignado != null && recetaIdAsignado != 0) {
                        // 2. Construimos la lista de medicamentos inyectando el ID real retornado
                        val listaDetallesFinal = constructorDetalles(recetaIdAsignado)

                        // 3. Petición del Desglose de Medicamentos
                        val responseDetalle = repository.agregarMedicamentosAReceta(listaDetallesFinal)

                        withContext(Dispatchers.Main) {
                            if (responseDetalle.isSuccessful && responseDetalle.body()?.success == true) {
                                _recetaProcesadaExito.value = true
                            } else {
                                val errorBodyDetalle = responseDetalle.errorBody()?.string()
                                val codigoHttp = responseDetalle.code()

                                // Esto pintará en consola la razón exacta (Ej: Llave foránea inexistente, ID inválido, etc.)
                                android.util.Log.e("API_ERROR_DETALLE", "Código: $codigoHttp | Cuerpo: $errorBodyDetalle")

                                _error.value = "Error $codigoHttp en desglose: $errorBodyDetalle"
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) { _error.value = "Error: El servidor no retornó un ID de receta válido" }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorBody = responseCabecera.errorBody()?.string()
                        val codigoHttp = responseCabecera.code()
                        // Esto te dirá exactamente qué no le gusta a tu Backend
                        _error.value = "Error $codigoHttp: $errorBody"
                        android.util.Log.e("API_ERROR", "Cuerpo del error: $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Fallo de comunicación: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }

    fun resetRecetaStatus() {
        _recetaProcesadaExito.value = false
    }
    // endregion

    // region Ejecuciones transaccionales de UnidadEspecialidad
    fun ejecutarActualizacionUnidadEspecialidad(id: Int, datos: UnidadEspecialidadRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resultado = repository.actualizarUnidadEspecialidad(id, datos)

                resultado.onSuccess {
                    _registroExitoso.value = "Asignación actualizada exitosamente"
                }.onFailure { excepcion ->
                    _error.value = excepcion.message ?: "Error al actualizar la asignación médica"
                }
            } catch (e: Exception) {
                _error.value = "Fallo crítico en la red remota: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    // endregion

    // region Cargar datos
    fun cargarPacientes() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _isLoading.value = true }
            try {
                val context = getApplication<Application>().applicationContext
                val prefs = context.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
                val tokenDirecto = prefs.getString("token_jwt", "") ?: ""
                val tokenLimpio = if (tokenDirecto.startsWith("Bearer ", ignoreCase = true)) tokenDirecto.substring(7).trim() else tokenDirecto
                val authHeader = if (tokenLimpio.isNotEmpty()) tokenLimpio else null

                val response = repository.obtenerPacientes(authHeader)

                if (response.isSuccessful) {
                    val contenedorBody = response.body()
                    val listaPacientesReal = contenedorBody?.data ?: emptyList()

                    withContext(Dispatchers.Main) {
                        _listaPacientes.value = listaPacientesReal
                        Log.d("API_SUCCESS", "Pacientes cargados en UI: ${listaPacientesReal.size}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _error.value = "Error al obtener pacientes: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Fallo de conexión: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }

    fun cargarMedicos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val prefs = context.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
                val tokenDirecto = prefs.getString("token_jwt", "")
                val authHeader = if (!tokenDirecto.isNullOrEmpty()) tokenDirecto else null

                val response = repository.obtenerMedicos(authHeader)

                if (response.isSuccessful) {
                    val contenedorBody = response.body()
                    val listaMedicosReal = contenedorBody?.data ?: emptyList()

                    withContext(Dispatchers.Main) {
                        _listaMedicos.value = listaMedicosReal
                        Log.d("API_SUCCESS", "Médicos cargados en UI: ${listaMedicosReal.size}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _error.value = "Error del servidor: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Fallo de conexión: ${e.message}"
                }
            }
        }
    }

    fun cargarCatalogos() {
        viewModelScope.launch {
            try {
                // Unidades Médicas
                val resUni = repository.obtenerUnidadesMedicas()
                if (resUni.isSuccessful) {
                    val listaReal = resUni.body()?.data ?: emptyList()
                    _unidadesMedicas.postValue(listaReal)
                    Log.d("DEBUG_API", "Unidades cargadas: ${listaReal.size}")
                }

                // Especialidades
                val resEsp = repository.obtenerEspecialidades()
                if (resEsp.isSuccessful) _especialidades.postValue(resEsp.body()?.data ?: emptyList())

                val context = getApplication<Application>().applicationContext
                val prefs = context.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
                val tokenDirecto = prefs.getString("token_jwt", "") ?: ""
                val tokenLimpio = if (tokenDirecto.startsWith("Bearer ", ignoreCase = true)) tokenDirecto.substring(7).trim() else tokenDirecto
                val authHeader = if (tokenLimpio.isNotEmpty()) tokenLimpio else null

                // Pasar el token obtenido a la consulta intermedia
                val resIntermedia = repository.obtenerUnidadesEspecialidad(authHeader)
                if (resIntermedia.isSuccessful) {
                    val listaReal = resIntermedia.body()?.data ?: emptyList()
                    _unidadesEspecialidad.postValue(listaReal)
                    Log.d("DEBUG_API", "Tabla intermedia asignada con token: ${listaReal.size}")
                } else {
                    Log.e("DEBUG_API", "Error intermedio (Falta Auth/Token): ${resIntermedia.code()}")
                }

                // Roles
                val resRol = repository.obtenerRoles()
                if (resRol.isSuccessful) {
                    val listaReal = resRol.body()?.data ?: emptyList()
                    RolesUsuario.inicializar(listaReal)
                    _roles.postValue(listaReal)
                    Log.d("DEBUG_API", "Roles cargados: ${listaReal.size}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_API", "Error de red: ${e.message}")
                _error.postValue("Sin conexión al servidor")
            }
        }
    }

    fun cargarMedicamentosCatalogos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.obtenerTodosLosMedicamentos()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    // Usamos .medicamentos que es el miembro real de tu MedicamentoWrapper
                    val listaReal = apiResponse?.data?.medicamentos ?: emptyList()

                    withContext(Dispatchers.Main) {
                        _listaMedicamentos.value = listaReal
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        // Esto te ayudará a ver si da otro código diferente a 500
                        _error.value = "Error del servidor al cargar medicinas: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Fallo de conexión: ${e.message}"
                }
            }
        }
    }

    fun cargarReporteHistorico(unidadMedicaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _isLoading.value = true }
            try {
                val context = getApplication<Application>().applicationContext
                val prefs = context.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
                val tokenDirecto = prefs.getString("token_jwt", "") ?: ""
                val tokenLimpio = if (tokenDirecto.startsWith("Bearer ", ignoreCase = true)) tokenDirecto.substring(7).trim() else tokenDirecto
                val authHeader = if (tokenLimpio.isNotEmpty()) "Bearer $tokenLimpio" else null

                val response = repository.obtenerHistoricoCitas(authHeader, unidadMedicaId)

                if (response.isSuccessful) {
                    val listaReal = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        _historicoCitas.value = listaReal
                        Log.d("API_GRAPH", "Datos de histórico cargados: ${listaReal.size} registros")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _error.value = "Error al obtener estadísticas: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error de red en gráficos: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }

    fun cargarTodasLasCitas() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _isLoading.value = true }

            try {
                val response = repository.obtenerTodasLasCitas()

                if (response.isSuccessful) {
                    // 1. Especificamos explícitamente el tipo de dato esperado de la API
                    val apiResponse: ApiResponse<AgendaCitasWrapper>? = response.body()

                    // 2. Extraemos el wrapper interno de forma segura
                    val citasWrapper: AgendaCitasWrapper? = apiResponse?.data

                    // 3. Extraemos la lista de citas real usando el campo interno de tu wrapper
                    val listaReal = citasWrapper?.citas ?: emptyList()

                    withContext(Dispatchers.Main) {
                        _listaCitas.value = listaReal
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _error.value = "Error del servidor: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Fallo de conexión: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }
    // endregion

    // region Validaciones
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
            estado = estado.copy(duiError = "Formato de DUI incorrecto (00000000-0)"); hayError = true
        }
        if (!Validation.isValidEmail(correo)) {
            estado = estado.copy(correoError = "Correo inválido"); hayError = true
        }
        if (!Validation.isValidPassword(password)) {
            estado = estado.copy(passwordError = "Mínimo 8 caracteres, 1 mayúscula y 1 símbolo"); hayError = true
        }
        if (!Validation.isValidPhone(telefono)) {
            estado = estado.copy(telefonoError = "Formato inválido (0000-0000)"); hayError = true
        }
        if (especialidadPos <= 0) {
            estado = estado.copy(especialidadError = "Especialidad requerida"); hayError = true
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

    fun validarFormularioUnidadEspecialidad(cupoDiarioStr: String) {
        var estado = FormularioState()
        var hayError = false

        val cupoNum = cupoDiarioStr.trim().toIntOrNull()
        if (cupoNum == null || cupoNum <= 0) {
            estado = estado.copy(cupoDiarioError = "El cupo diario debe ser un número mayor a 0")
            hayError = true
        }

        _formState.value = estado.copy(isValid = !hayError)
    }
    // endregion
}