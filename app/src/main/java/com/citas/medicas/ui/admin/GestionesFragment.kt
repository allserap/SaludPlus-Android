package com.citas.medicas.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentGestionesBinding
import com.google.android.material.navigation.NavigationView

class GestionesFragment : Fragment(R.layout.fragment_gestiones) {
    // Inicializar el binding
    private var _binding: FragmentGestionesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGestionesBinding.bind(view)

        setupNavigation()
    }

    private fun setupNavigation() {
        with(binding) {
            cardMedicos.setOnClickListener {
                congelarTarjetasTemporales()
                // Instanciar el fragment de destino
                val nuevoFragment = GestionMedicosFragment()
                // Iniciar la transacción
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    .replace(R.id.containerFragment, nuevoFragment)
                    .addToBackStack(null)
                    .commit()
            }
            cardPacientes.setOnClickListener {
                congelarTarjetasTemporales()
                val nuevoFragment = GestionPacientesFragment()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    .replace(R.id.containerFragment, nuevoFragment)
                    .addToBackStack(null)
                    .commit()
            }
            cardUnidadesEspecialidad.setOnClickListener {
                congelarTarjetasTemporales()
                val nuevoFragment = UnidadEspecialidadFragment()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    .replace(R.id.containerFragment, nuevoFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
    private fun congelarTarjetasTemporales() {
        with(binding) {
            cardMedicos.isClickable = false
            cardPacientes.isClickable = false
            cardUnidadesEspecialidad.isClickable = false

            // Reactivamos los clics después de 400ms
            root.postDelayed({
                cardMedicos.isClickable = true
                cardPacientes.isClickable = true
                cardUnidadesEspecialidad.isClickable = true
            }, 400)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}