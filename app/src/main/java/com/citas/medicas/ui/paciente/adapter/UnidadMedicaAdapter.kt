package com.citas.medicas.ui.paciente.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.models.UnidadMedica
import com.google.android.material.card.MaterialCardView

class UnidadMedicaAdapter(
    private var lista: List<UnidadMedica>,
    private val alSeleccionar: (UnidadMedica) -> Unit
) : RecyclerView.Adapter<UnidadMedicaAdapter.ViewHolder>() {

    private var indexSeleccionado = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUnidad)
        val tvDireccion: TextView = view.findViewById(R.id.tvDireccionUnidad)
        val card: MaterialCardView = view as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_unidad_medica, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre
        holder.tvDireccion.text = item.direccion

        if (indexSeleccionado == position) {
            holder.card.setCardBackgroundColor(Color.parseColor("#1565C0"))
            holder.tvNombre.setTextColor(Color.WHITE)
            holder.tvDireccion.setTextColor(Color.WHITE)
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.tvNombre.setTextColor(Color.parseColor("#1565C0"))
            holder.tvDireccion.setTextColor(Color.GRAY) // Secondary
        }

        holder.card.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                indexSeleccionado = pos
                notifyDataSetChanged()
                alSeleccionar(item)
            }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarDatos(nuevaLista: List<UnidadMedica>) {
        lista = nuevaLista
        indexSeleccionado = -1
        notifyDataSetChanged()
    }
}