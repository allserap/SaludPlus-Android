package com.citas.medicas.utils

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.Locale

// --- Extensiones de Spinner ---
fun Spinner.configurarConHint(opciones: Array<String>, hint: String) {
    val listaConHint = arrayOf(hint) + opciones
    val adapter = object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listaConHint) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            (view as TextView).setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
            return view
        }

        override fun isEnabled(position: Int): Boolean = position != 0

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            (view as TextView).setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
            return view
        }
    }
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    this.adapter = adapter
}

// --- Extensiones de View ---
fun View.cambiarColor(colorRes: Int) {
    this.setBackgroundColor(ContextCompat.getColor(context, colorRes))
}

fun View.setVisibilidad(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

// --- Extensiones de EditText (Máscaras) ---
fun EditText.aplicarMascaraTelefono() {
    this.addTextChangedListener(object : TextWatcher {
        private var isUpdating = false
        override fun afterTextChanged(s: Editable?) {
            if (isUpdating) return
            isUpdating = true
            val original = s.toString().replace("-", "")
            val formatted = StringBuilder()
            for (i in original.indices) {
                formatted.append(original[i])
                if (i == 3 && i < original.length - 1) formatted.append("-")
            }
            s?.replace(0, s.length, formatted.toString())
            isUpdating = false
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun EditText.aplicarMascaraDUI() {
    // Definimos el límite máximo de caracteres permitidos (8 dígitos + 1 guion + 1 dígito = 10)
    this.filters = arrayOf(android.text.InputFilter.LengthFilter(10))

    this.addTextChangedListener(object : android.text.TextWatcher {
        private var isUpdating = false

        override fun afterTextChanged(s: android.text.Editable?) {
            if (isUpdating) return
            isUpdating = true

            val original = s.toString().replace("-", "")
            val formatted = StringBuilder()

            for (i in original.indices) {
                formatted.append(original[i])
                // Si llega al índice 7 y hay al menos 8 caracteres, se agrega el guion
                if (i == 7 && original.length >= 8) {
                    formatted.append("-")
                }
            }

            s?.replace(0, s.length, formatted.toString())
            isUpdating = false
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

// --- Utilidades Globales ---
fun limpiarCampos(vararg editTexts: EditText) {
    editTexts.forEach { it.text?.clear() }
}

fun showDatePickerDialog(context: Context, editText: EditText) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(context, { _, year, month, day ->
        val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
        editText.setText(formattedDate)
        editText.error = null
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}
