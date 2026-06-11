package com.citas.medicas.ui.paciente.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.models.DetalleRecetaPaciente

class MedicamentoRecetaAdapter(private val lista: List<DetalleRecetaPaciente>) :
    RecyclerView.Adapter<MedicamentoRecetaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreMedicamento)
        val tvForma: TextView = view.findViewById(R.id.tvFormaFarmaceutica)
        val tvDosis: TextView = view.findViewById(R.id.tvDosis)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidadTotal)
        val tvDuracion: TextView = view.findViewById(R.id.tvDuracion)
        val tvInstrucciones: TextView = view.findViewById(R.id.tvInstrucciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento_receta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = "${item.nombre_generico} ${item.concentracion}"
        holder.tvForma.text = item.forma_farmaceutica?.uppercase() ?: "N/A"
        holder.tvDosis.text = "Dosis: ${item.dosis}"
        holder.tvCantidad.text = "Cant: ${item.cantidad}"
        holder.tvDuracion.text = "Duración: ${item.duracion_dias} días"
        holder.tvInstrucciones.text = "Nota: ${item.instrucciones}"
    }

    override fun getItemCount() = lista.size
}