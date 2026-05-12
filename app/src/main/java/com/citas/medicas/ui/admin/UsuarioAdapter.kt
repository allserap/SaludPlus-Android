package com.citas.medicas.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.citas.medicas.R
import com.citas.medicas.databinding.ItemUsuarioBinding
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.Usuario

class UsuariosAdapter(private var medicos: List<MedicoResponse>) :
    RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(val binding: ItemUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val medico = medicos[position]
        val context = holder.itemView.context

        with(holder.binding) {
            // Unir nombre y apellido del médico
            val nombreCompleto = "${medico.nombre} ${medico.apellido}"
            tvNombre.text = nombreCompleto
            tvEspecialidad.text = "${medico.especialidadNombre}"
            tvUnidadMedica.text = "${medico.unidadMedicaNombre}"
            tvRolBadge.text = "${medico.rolNombre}"

            // --- LÓGICA DE ESTADO (ELIMINACIÓN LÓGICA) ---
            if (medico.activo) {
                tvEstado.text = "Activo"
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.status_completed))
                root.alpha = 1.0f // card visible
            } else {
                tvEstado.text = "Inactivo"
                // Texto Rojo Fuerte
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.status_canceled))
                // Fondo Rojo Claro
                tvEstado.backgroundTintList = ContextCompat.getColorStateList(context, R.color.status_canceled_light)
                tvIniciales.setTextColor(ContextCompat.getColor(context, R.color.status_canceled))
                tvIniciales.backgroundTintList = ContextCompat.getColorStateList(context, R.color.status_canceled_light)

                // card atenuado
                root.alpha = 0.6f
            }

            // Lógica de iniciales
            val iniciales = "${medico.nombre.take(1)}${medico.apellido.take(1)}".uppercase()
            tvIniciales.text = iniciales

            // Estilo visual del Badge (Azul para médicos)
            tvRolBadge.backgroundTintList = ContextCompat.getColorStateList(context, R.color.citas_primary)
            tvRolBadge.setTextColor(ContextCompat.getColor(context, R.color.citas_white))
        }
    }

    override fun getItemCount() = medicos.size

    fun updateList(newList: List<MedicoResponse>) {
        this.medicos = newList
        notifyDataSetChanged()
    }
}