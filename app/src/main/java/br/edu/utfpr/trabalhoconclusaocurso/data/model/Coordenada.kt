package br.edu.utfpr.trabalhoconclusaocurso.data.model

data class Coordenada(
    val id: Int = 0, // Autoincrement no SQLite
    val idAtividade: String,
    val latitude: Double,
    val longitude: Double
)