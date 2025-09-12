package br.edu.utfpr.trabalhoconclusaocurso.data.model

import java.io.Serializable

data class Usuario(
    var id: String? = null,
    var nome: String? = null,
    var username: String? = null,
    var cpf: String? = null,
    var idade: Int? = null,
    var altura: Double? = null,
    var peso: Double? = null,
    var distanciaPreferida: Double? = null,
    var usuarioSenha: String? = null
) : Serializable