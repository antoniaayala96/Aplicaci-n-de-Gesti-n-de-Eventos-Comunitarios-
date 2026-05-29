package com.antonia.comunidadactiva.models

data class Evento(
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fecha: String = "",
    var hora: String = "",
    var ubicacion: String = "",
    var categoria: String = "",
    var imagenUrl: String = "",
    var estado: String = "Activo",
    var creadorId: String = ""
)