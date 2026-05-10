package com.citas.medicas.utils

object Validation {
    fun isValidPassword(password: String): Boolean {
        // 8 caracteres, una mayúscula y un símbolo
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$")
        return password.matches(passwordRegex)
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidDUI(dui: String): Boolean {
        val regex = Regex("^\\d{8}-\\d$")
        return dui.matches(regex)
    }

    fun isValidPhone(phone: String): Boolean {
        val regex = Regex("^\\d{4}-\\d{4}$")
        return phone.matches(regex)
    }
}