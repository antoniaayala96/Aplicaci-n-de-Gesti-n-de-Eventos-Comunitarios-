package com.antonia.comunidadactiva.utils

import android.util.Patterns

object Validaciones {

    fun correoValido(correo: String): Boolean {
        return correo.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }

    fun passwordValida(password: String): Boolean {
        return password.length >= 6
    }

    fun campoVacio(texto: String): Boolean {
        return texto.trim().isEmpty()
    }

    fun urlValida(url: String): Boolean {
        return url.isEmpty() || Patterns.WEB_URL.matcher(url).matches()
    }
}