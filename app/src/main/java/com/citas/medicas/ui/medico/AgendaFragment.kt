package com.citas.medicas.ui.medico

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentAgendaBinding
import com.citas.medicas.ui.auth.AuthViewModel

class AgendaFragment : Fragment(R.layout.fragment_agenda) {

    private var _binding: FragmentAgendaBinding? = null
    private val binding get() = _binding!!
    private lateinit var citasAdapter: CitasAdapter

    // ViewModel compartido a nivel de Activity
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAgendaBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        fetchAppointments()
    }

    private fun setupRecyclerView() {
        citasAdapter = CitasAdapter(emptyList()) { idPacienteSeleccionado ->
            val dash = (activity as? DashboardMedicoActivity)
            //dash?.navigateToHistorial(idPacienteSeleccionado)
        }

        binding.rvCitas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citasAdapter
        }
    }

    private fun setupObservers() {
        // Eliminamos por completo CUALQUIER observador previo asignado a estas variables,
        // sin importar qué ciclo de vida (viejo o nuevo) lo esté reclamando.
        authViewModel.listaCitas.removeObservers(viewLifecycleOwner)
        authViewModel.error.removeObservers(viewLifecycleOwner)
        authViewModel.isLoading.removeObservers(viewLifecycleOwner)

        // authViewModel.listaCitas.removeObserver { }

        // Observar la lista de citas
        authViewModel.listaCitas.observe(viewLifecycleOwner, { listaCitas ->
            val citasFiltradas = listaCitas.filter { cita ->
                val estadoCita = cita.estadocita?.lowercase()?.trim() ?: ""

                // Compara contra las 3 variantes que necesitas
                estadoCita == "confirmada" ||
                        estadoCita == "pendiente" ||
                        estadoCita == "reprogramada"
            }

            // Evaluamos e inflamos la UI con la lista ya depurada
            if (citasFiltradas.isNotEmpty()) {
                citasAdapter.updateList(citasFiltradas)
            } else {
                Toast.makeText(requireContext(), "No hay citas pendientes o activas", Toast.LENGTH_SHORT).show()
                citasAdapter.updateList(emptyList())
            }
        })

        // Observar errores de red o servidor
        authViewModel.error.observe(viewLifecycleOwner, { mensajeError ->
            mensajeError?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })

        // Observar estado de carga (ProgressBar)
        authViewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            // binding.progressBar.visibility = if (isLoading == true) View.VISIBLE else View.GONE
        })
    }

    private fun fetchAppointments() {
        authViewModel.cargarTodasLasCitas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}