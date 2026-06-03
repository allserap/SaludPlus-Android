package com.citas.medicas.ui.medico

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.databinding.ItemCitaMedicoBinding
import com.citas.medicas.models.CitaItem
import com.citas.medicas.models.CitaResponse
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
            tvHoraCita.text = formatearHora(cita.horaasignada)
            tvFechaCita.text = formatearFechaIso(cita.fechacita)
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

    private fun formatearHora(horaRaw: String?): String {
        if (horaRaw.isNullOrEmpty()) return "No disponible"
        return try {
            // Si contiene los dos puntos de los segundos (ej: 11:00:00), corta antes del último
            if (horaRaw.count { it == ':' } == 2) {
                horaRaw.substringBeforeLast(":")
            } else {
                horaRaw.take(5) // Resguardo por si viene con microsegundos o formatos raros
            }
        } catch (e: Exception) {
            android.util.Log.e("CitasAdapter", "Error formateando hora: $horaRaw", e)
            horaRaw // Si falla por algo, retorna la hora original para no romper la UI
        }
    }

    private fun formatearFechaIso(fechaIso: String?): String {
        if (fechaIso.isNullOrEmpty()) return "No disponible"
        return try {
            val fechaParseada = ZonedDateTime.parse(fechaIso)
            val formateador = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
            fechaParseada.format(formateador)
        } catch (e: Exception) {
            android.util.Log.e("PerfilMedico", "Error mapeando fecha: $fechaIso", e)
            fechaIso
        }
    }

    override fun getItemCount(): Int = citas.size

    fun updateList(newList: List<CitaResponse>) {
        this.citas = newList
        notifyDataSetChanged()
    }
}