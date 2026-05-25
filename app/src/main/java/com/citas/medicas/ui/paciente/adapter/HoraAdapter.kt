package com.citas.medicas.ui.paciente.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.google.android.material.card.MaterialCardView

class HoraAdapter(
    private var lista: List<String>,
    private val alSeleccionar: (String) -> Unit
) : RecyclerView.Adapter<HoraAdapter.ViewHolder>() {

    private var indexSeleccionado = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHora: TextView = view.findViewById(R.id.tvHora) // ID de tu XML
        val card: MaterialCardView = view as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hora, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val horaStr = lista[position]
        holder.tvHora.text = horaStr

        if (indexSeleccionado == position) {
            holder.card.setCardBackgroundColor(Color.parseColor("#1565C0"))
            holder.tvHora.setTextColor(Color.WHITE)
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.tvHora.setTextColor(Color.parseColor("#1565C0"))
        }

        holder.card.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                indexSeleccionado = pos
                notifyDataSetChanged()
                alSeleccionar(horaStr)
            }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarDatos(nuevaLista: List<String>) {
        lista = nuevaLista
        indexSeleccionado = -1
        notifyDataSetChanged()
    }
}