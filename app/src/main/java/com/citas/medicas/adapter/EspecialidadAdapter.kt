package com.citas.medicas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.models.EspecialidadResponse
import com.google.android.material.card.MaterialCardView

class EspecialidadAdapter(
    private var lista: List<EspecialidadResponse>,
    private val onItemClick: (EspecialidadResponse) -> Unit
) : RecyclerView.Adapter<EspecialidadAdapter.EspecialidadViewHolder>() {

    private var indexSeleccionado = -1

    class EspecialidadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvItemName)
        val card: MaterialCardView = view as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EspecialidadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_button, parent, false)
        return EspecialidadViewHolder(view)
    }

    override fun onBindViewHolder(holder: EspecialidadViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre

        // Pintar de color primario si está seleccionado
        if (indexSeleccionado == position) {
            holder.card.setCardBackgroundColor(android.graphics.Color.parseColor("#1565C0")) // Tu color_primary
            holder.tvNombre.setTextColor(android.graphics.Color.WHITE)
        } else {
            holder.card.setCardBackgroundColor(android.graphics.Color.WHITE)
            holder.tvNombre.setTextColor(android.graphics.Color.BLACK)
        }

        holder.card.setOnClickListener {
            val posicionActual = holder.adapterPosition
            if (posicionActual != RecyclerView.NO_POSITION) {
                indexSeleccionado = posicionActual
                notifyDataSetChanged()
                onItemClick(item) // Usamos el nombre correcto del parámetro
            }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarDatos(nuevaLista: List<EspecialidadResponse>) {
        lista = nuevaLista
        indexSeleccionado = -1 // Reseteamos la selección cuando cambian los datos
        notifyDataSetChanged()
    }
}