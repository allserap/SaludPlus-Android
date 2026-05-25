package com.citas.medicas.ui.paciente.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.models.UnidadMedicaMapa
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MapaAdapter(private var listaUnidades: List<UnidadMedicaMapa>) :
    RecyclerView.Adapter<MapaAdapter.MapaViewHolder>() {

    class MapaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreItemMapa)
        val tvDireccion: TextView = view.findViewById(R.id.tvDireccionItemMapa)

        val llCabecera: LinearLayout = view.findViewById(R.id.llCabeceraUnidad)
        val ivFlecha: ImageView = view.findViewById(R.id.ivFlechaExpandir)
        val llDetalles: LinearLayout = view.findViewById(R.id.llDetallesExpandibles)

        // Botones
        val btnLlamar: MaterialButton = view.findViewById(R.id.btnLlamarHospital)
        val btnLlegar: MaterialButton = view.findViewById(R.id.btnComoLlegar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unidad_mapa, parent, false) // Asegúrate de que este sea el nombre de tu nuevo XML
        return MapaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MapaViewHolder, position: Int) {
        val unidad = listaUnidades[position]

        holder.tvNombre.text = unidad.nombre
        holder.tvDireccion.text = unidad.direccion
        holder.btnLlamar.text = unidad.telefono ?: "No disponible"

        holder.llCabecera.setOnClickListener {
            if (holder.llDetalles.visibility == View.GONE) {
                // Si está oculto, lo mostramos
                holder.llDetalles.visibility = View.VISIBLE
                holder.ivFlecha.rotation = 180f // Rotamos la flechita hacia arriba
            } else {
                // Si está visible, lo ocultamos
                holder.llDetalles.visibility = View.GONE
                holder.ivFlecha.rotation = 0f // Flechita hacia abajo
            }
        }

        holder.btnLlegar.setOnClickListener {
            if (unidad.latitud != null && unidad.longitud != null) {
                val uri = "geo:${unidad.latitud},${unidad.longitud}?q=${unidad.latitud},${unidad.longitud}(${unidad.nombre})"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                holder.itemView.context.startActivity(intent)
            }
        }

        // 4. (Opcional) Botón "Llamar"
        holder.btnLlamar.setOnClickListener {
            if (!unidad.telefono.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${unidad.telefono}"))
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = listaUnidades.size

    fun actualizarDatos(nuevaLista: List<UnidadMedicaMapa>) {
        listaUnidades = nuevaLista
        notifyDataSetChanged()
    }
}