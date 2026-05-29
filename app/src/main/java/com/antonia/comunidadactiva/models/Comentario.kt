package com.antonia.comunidadactiva.models

data class Comentario(
    var id: String = "",
    var eventoId: String = "",
    var usuarioId: String = "",
    var nombreUsuario: String = "",
    var texto: String = "",
    var calificacion: Float = 0f
)