package com.citas.medicas.utils

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat

fun Spinner.configurarConHint(opciones: Array<String>, hint: String) {
        val context = this.context
        val listaConHint = arrayOf(hint) + opciones

        val adapter = object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listaConHint) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val tv = view as TextView
                // texto seleccionado gris si es el hint
                tv.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                tv.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.adapter = adapter
}

fun View.cambiarColor(colorRes: Int) {
    this.setBackgroundColor(ContextCompat.getColor(context, colorRes))
}
