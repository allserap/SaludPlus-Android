package com.citas.medicas.ui.medico

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentAgendaBinding
import com.citas.medicas.models.CitaItem

class AgendaFragment : Fragment(R.layout.fragment_agenda) {

    private var _binding: FragmentAgendaBinding? = null
    private val binding get() = _binding!!
    private lateinit var citasAdapter: CitasAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAgendaBinding.bind(view)

        setupRecyclerView()
        fetchAppointments()
    }

    private fun setupRecyclerView() {
        // Inicializa el adaptador. El evento click recibe el id numérico del paciente
        citasAdapter = CitasAdapter(emptyList()) { idPacienteSeleccionado ->
            // Navegación limpia delegada a la función puente del DashboardActivity
            val dash = (activity as? DashboardMedicoActivity)
            //dash?.navigateToHistorial(idPacienteSeleccionado)
        }

        binding.rvCitas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citasAdapter
        }
    }

    private fun fetchAppointments() {
        // agregar la llamada asíncrona del ApiService cuando conecte Retrofit
        // lifecycleScope.launch { ... }

        // Datos mock simulados con la estructura real de la base de datos para probar el renderizado
        val mockData = listOf(
            CitaItem(
                pacienteid = 3,
                nombrepaciente = "María",
                apellidopaciente = "Guzmán",
                medicoid = 1,
                medicousuarioid = "f56e5a64-768a-47b0-af3e-5c5f74215b1f",
                estadocita = "pendiente",
                especialidadid = 1,
                especialidadcita = "Medicina General",
                horaasignada = "15:30:00"
            ),
            CitaItem(
                pacienteid = 1,
                nombrepaciente = "Juan Carlos",
                apellidopaciente = "Pérez",
                medicoid = 1,
                medicousuarioid = "f56e5a64-768a-47b0-af3e-5c5f74215b1f",
                estadocita = "pendiente",
                especialidadid = 1,
                especialidadcita = "Medicina General",
                horaasignada = "16:15:00"
            )
        )

        // Cargar los datos en el RecyclerView
        citasAdapter.updateList(mockData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}