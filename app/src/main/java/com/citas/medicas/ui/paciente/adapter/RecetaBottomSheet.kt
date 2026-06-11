package com.citas.medicas.ui.paciente.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.data.RetrofitClient
import com.citas.medicas.ui.paciente.adapter.MedicamentoRecetaAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class RecetaBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_CITA_ID = "CITA_ID"

        fun newInstance(citaId: String): RecetaBottomSheet {
            val fragment = RecetaBottomSheet()
            val args = Bundle()
            args.putString(ARG_CITA_ID, citaId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_receta, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val citaId = arguments?.getString(ARG_CITA_ID) ?: return

        view.findViewById<View>(R.id.btnCerrarReceta).setOnClickListener { dismiss() }

        // Referencias de UI
        val tvFecha = view.findViewById<TextView>(R.id.tvFechaReceta)
        val tvDoctor = view.findViewById<TextView>(R.id.tvDoctorReceta)
        val tvEspecialidad = view.findViewById<TextView>(R.id.tvEspecialidadReceta)
        val tvTelefono = view.findViewById<TextView>(R.id.tvTelefonoDoctor)
        val rvMedicamentos = view.findViewById<RecyclerView>(R.id.rvMedicamentosReceta)

        rvMedicamentos.layoutManager = LinearLayoutManager(context)

        // Llamada a la API
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(requireContext())
                val response = apiService.getRecetaCita(citaId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val recetaData = response.body()?.data

                    if (recetaData != null) {
                        tvDoctor.text = "Dr. ${recetaData.medico_nombre} ${recetaData.apellido}"
                        tvEspecialidad.text = recetaData.especialidad
                        tvTelefono.text = "Tel: ${recetaData.telefono}"

                        try {
                            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es", "ES"))
                            val date = inputFormat.parse(recetaData.fecha ?: "")
                            tvFecha.text = "Emitida el: ${outputFormat.format(date!!)}"
                        } catch (e: Exception) {
                            tvFecha.text = "Emitida el: ${recetaData.fecha?.take(10)}"
                        }

                        // Llenar el RecyclerView (Convertimos el objeto único en una lista de 1 elemento)
                        rvMedicamentos.adapter = MedicamentoRecetaAdapter(listOf(recetaData))
                    } else {
                        Toast.makeText(context, "Esta cita no tiene receta registrada", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                } else {
                    Toast.makeText(context, "Error al obtener la receta", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }
}