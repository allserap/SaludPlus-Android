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
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.RolResponse
import com.citas.medicas.models.UnidadMedicaResponse
import com.citas.medicas.utils.RolesUsuario
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application){
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

    fun ejecutarActualizacion(datos: MedicoUpdateRequest){
        viewModelScope.launch {
            viewModelScope.launch {
                _isLoading.value = true
                val resultado = repository.actualizarMedico(datos)

                resultado.onSuccess { mensaje ->
                    _registroExitoso.value = mensaje
                }.onFailure { excepcion ->
                    _error.value = excepcion.message
                }
                _isLoading.value = false
            }
        }
    }

    //endregion

    // region Cargar datos
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
                    // CAMBIO AQUÍ: Usamos resUni.body()
                    val listaReal = resUni.body()?.data ?: emptyList()
                    _unidadesMedicas.postValue(listaReal)
                    Log.d("DEBUG_API", "Unidades cargadas: ${listaReal.size}")
                }

                // 3. Obtener roles
                val resRol = repository.obtenerRoles()
                if (resRol.isSuccessful) {
                    val listaReal = resRol.body()?.data ?: emptyList()
                    _roles.postValue(listaReal)
                    Log.d("DEBUG_API", "Roles cargados: ${listaReal.size}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_API", "Error de red: ${e.message}")
                _error.postValue("Sin conexión al servidor")
            }
        }
    }


    //endregion


}