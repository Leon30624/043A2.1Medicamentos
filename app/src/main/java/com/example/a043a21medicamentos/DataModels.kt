package com.example.a043a21medicamentos

import androidx.compose.runtime.mutableStateListOf

// Modelo para Medicamentos / Botiquín (HU02 y HU04)
data class MedicamentoBotiquin(
    val id: Int,
    val nombre: String,
    val categoria: String,
    var unidadesDisponibles: Int,
    val limiteMinimo: Int = 5
)

// Modelo para Alarmas (HU01 y HU05)
data class AlarmaMedicamento(
    val id: Int,
    val nombreMedicamento: String,
    val dosis: String,
    val horaToma: String
)

// Modelo para Alergias (HU05)
data class Alergia(
    val descripcion: String
)

// Modelo para Historial de Tomas (HU03)
data class RegistroToma(
    val id: Int,
    val nombreMedicamento: String,
    val fechaHora: String,
    val estado: String // "Tomada" u "Omitida"
)

// Estado Global de la App (Un único usuario)
object EstadoApp {
    val listaBotiquin = mutableStateListOf<MedicamentoBotiquin>()
    val listaAlarmas = mutableStateListOf<AlarmaMedicamento>()
    val listaAlergias = mutableStateListOf<Alergia>()
    val listaHistorial = mutableStateListOf<RegistroToma>()
}