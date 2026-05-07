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
            tvEspecialidad.text = "Especialidad ID: ${medico.especialidadId}"
            tvUnidadMedica.text = "Unidad ID: ${medico.unidadMedicaId}"
            tvRolBadge.text = if (medico.rolId == 2) "Médico" else "Usuario"
            tvEstado.text = if (medico.activo) "Activo" else "Inactivo"

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