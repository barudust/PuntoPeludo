package com.example.puntopeludo

// Modelos simples para los Spinners
data class Marca(val id: Int, val nombre: String) {
    override fun toString(): String = nombre // Para que el Spinner muestre el nombre
}

data class Categoria(val id: Int, val nombre: String) {
    override fun toString(): String = nombre
}
data class Subcategoria(val id: Int, val nombre: String) {
    override fun toString(): String = nombre
}
data class Especie(val id: Int, val nombre: String) {
    override fun toString(): String = nombre
}

data class Etapa(val id: Int, val nombre: String) {
    override fun toString(): String = nombre
}

data class Linea(val id: Int, val nombre: String) {
    override fun toString(): String = nombre
}