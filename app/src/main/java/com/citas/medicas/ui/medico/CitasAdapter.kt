package com.citas.medicas.ui.medico

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.databinding.ItemCitaMedicoBinding
import com.citas.medicas.models.CitaItem
import com.citas.medicas.models.CitaResponse

class CitasAdapter(
    private var citas: List<CitaResponse>,
    private val onPacienteClick: (Int) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(val binding: ItemCitaMedicoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val binding = ItemCitaMedicoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CitaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = citas[position]
        with(holder.binding) {
            // Unir nombre y apellido para mostrarlo en la tarjeta
            tvNombrePaciente.text = "${cita.nombrepaciente} ${cita.apellidopaciente}"
            tvTipoCita.text = cita.especialidadcita
            tvHoraCita.text = cita.horaasignada
            tvFechaCita.text = cita.fechacita
            tvEstadoCita.text = cita.estadocita.replaceFirstChar { it.uppercase() }

            // Lógica para calcular y setear iniciales (Ej: María Guzmán -> MG)
            val primeraLetraNombre = cita.nombrepaciente.firstOrNull()?.toString() ?: ""
            val primeraLetraApellido = cita.apellidopaciente.firstOrNull()?.toString() ?: ""
            tvIniciales.text = "$primeraLetraNombre$primeraLetraApellido".uppercase()

            // Delegar el evento click enviando el id del paciente al fragmento padre
            root.setOnClickListener {
                onPacienteClick(cita.pacienteid)
            }
        }
    }

    override fun getItemCount(): Int = citas.size

    fun updateList(newList: List<CitaResponse>) {
        this.citas = newList
        notifyDataSetChanged()
    }
}