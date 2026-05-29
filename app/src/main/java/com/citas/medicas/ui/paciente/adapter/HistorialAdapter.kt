package com.citas.medicas.ui.paciente // Ajusta tu paquete si es necesario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.models.CitaHistorial
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialAdapter(private var listaCitas: List<CitaHistorial>) :
    RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEspecialidad: TextView = view.findViewById(R.id.tvHistorialEspecialidad)
        val tvEstado: TextView = view.findViewById(R.id.tvHistorialEstado)
        val tvDoctor: TextView = view.findViewById(R.id.tvHistorialDoctor)
        val tvFecha: TextView = view.findViewById(R.id.tvHistorialFecha)
        val tvUnidad: TextView = view.findViewById(R.id.tvHistorialUnidad)
        val layoutBotones: View = view.findViewById(R.id.layoutButtons)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_cita, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val cita = listaCitas[position]

        holder.tvEspecialidad.text = cita.especialidad
        holder.tvEstado.text = cita.estado?.uppercase()
        holder.tvDoctor.text = "Doctor: ${cita.doctor}"
        holder.tvUnidad.text = "Unidad: ${cita.unidad_medica}"

        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
            val date = parser.parse(cita.fecha_solicitada ?: "")
            holder.tvFecha.text = "Fecha: ${formatter.format(date!!)} a las ${cita.hora_asignada}"
        } catch (e: Exception) {
            holder.tvFecha.text = "Fecha: ${cita.fecha_solicitada} a las ${cita.hora_asignada}"
        }

        if (cita.estado == "atendida" || cita.estado == "cancelada_paciente" || cita.estado == "cancelada_sistema") {
            holder.layoutBotones.visibility = View.GONE
        } else {
            holder.layoutBotones.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = listaCitas.size

    // Método para actualizar la lista cuando cambiamos de Pestaña
    fun actualizarDatos(nuevaLista: List<CitaHistorial>) {
        listaCitas = nuevaLista
        notifyDataSetChanged()
    }
}