package com.example.listapp.models

/**
 *Define un modelo de datos mutable para representar un Gundam con ID, nombre, tipo y estado
 * de favorito, con valores por defecto vacíos/false.
 *
 * (Clase de datos que estructura la información de cada elemento del RecyclerView,
 * preparada para ser modificada).
 */
data class Gundam (
    var id: String = "",
    var nombre: String = "",
    var tipo: String= "",
    var esFavorito: Boolean = false
)

