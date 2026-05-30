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
    private var lista: List<com.citas.medicas.models.UnidadMedicaMapa> = emptyList()
) : RecyclerView.Adapter<UnidadMedicaAdapter.ViewHolder>() {

    private var onItemClickListener: ((com.citas.medicas.models.UnidadMedicaMapa) -> Unit)? = null

    fun setOnItemClickListener(listener: (com.citas.medicas.models.UnidadMedicaMapa) -> Unit) {
        onItemClickListener = listener
    }

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

        if (indexSeleccionado == position) {
            holder.card.setCardBackgroundColor(android.graphics.Color.parseColor("#1565C0"))
            holder.tvNombre.setTextColor(android.graphics.Color.WHITE)
            holder.tvDireccion.setTextColor(android.graphics.Color.WHITE)

            val telefonoInfo = if (!item.telefono.isNullOrEmpty()) "📞 Tel: ${item.telefono}" else "📞 Tel: No disponible"
            holder.tvDireccion.text = "${item.direccion}\n$telefonoInfo"

        } else {
            holder.card.setCardBackgroundColor(android.graphics.Color.WHITE)
            holder.tvNombre.setTextColor(android.graphics.Color.parseColor("#1565C0"))
            holder.tvDireccion.setTextColor(android.graphics.Color.GRAY)

            holder.tvDireccion.text = item.direccion
        }

        holder.card.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                indexSeleccionado = pos
                notifyDataSetChanged()

                onItemClickListener?.invoke(item)
            }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarDatos(nuevaLista: List<com.citas.medicas.models.UnidadMedicaMapa>) {
        lista = nuevaLista
        indexSeleccionado = -1
        notifyDataSetChanged()
    }
}