package com.citas.medicas.ui.base

import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Locale

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    // Funciones visuales
    protected fun aplicarMascaraTelefono(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
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

    protected fun aplicarMascaraDUI(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                val original = s.toString().replace("-", "")
                val formatted = StringBuilder()

                for (i in original.indices) {
                    formatted.append(original[i])
                    if (i == 7 && original.length > 8) {
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

    // Funciones de utilidad común
        fun limpiarCampos(vararg editTexts: EditText) {
            editTexts.forEach { it.text?.clear() }
        }

        // Control de visibilidad
        fun setVisibilidad(view: View, visible: Boolean) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }

        fun validarRequerido(editText: EditText, mensaje: String): Boolean {
            return if (editText.text.toString().trim().isEmpty()) {
                editText.error = mensaje
                false
            } else {
                editText.error = null
                true
            }
        }

        // Mostrar y formatear fecha
        fun showDatePickerDialog(editText: EditText) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val realMonth = selectedMonth + 1
                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, realMonth, selectedDay)
                editText.setText(formattedDate)
                editText.error = null
            }, year, month, day)

            datePicker.show()
        }

        // metodo para resetear UI
        abstract fun resetearInterfaz()
}