package com.citas.medicas.ui.base

import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Locale

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {
        // Control de visibilidad
        fun setVisibilidad(view: View, visible: Boolean) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }

        fun ocultarTeclado() {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }

        // metodo para resetear UI
        abstract fun resetearInterfaz()
}