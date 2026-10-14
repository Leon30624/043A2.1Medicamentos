package com.example.a043a21medicamentos

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PantallaBotiquin() {
    val context = LocalContext.current

    var nombreMed by remember { mutableStateOf("") }
    var cantidadInicialText by remember { mutableStateOf("") }

    var filtroBusqueda by remember { mutableStateOf("") }

    val medicamentosFiltrados = EstadoApp.listaBotiquin.filter {
        it.nombre.contains(filtroBusqueda.trim(), ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- FORMULARIO DE REGISTRO DE INVENTARIO ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Registrar Medicamento en Botiquín",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nombreMed,
                        onValueChange = { nombreMed = it },
                        label = { Text("Nombre del medicamento *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cantidadInicialText,
                        onValueChange = { cantidadInicialText = it },
                        label = { Text("Número total de pastillas/unidades *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val cantidad = cantidadInicialText.toIntOrNull()

                            if (nombreMed.isBlank()) {
                                Toast.makeText(context, "Error: El nombre del medicamento es obligatorio", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (cantidad == null || cantidad <= 0) {
                                Toast.makeText(
                                    context,
                                    "Error: No se permite registrar una cantidad inicial menor o igual a cero",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            val nuevoId = (EstadoApp.listaBotiquin.maxOfOrNull { it.id } ?: 0) + 1

                            EstadoApp.listaBotiquin.add(
                                MedicamentoBotiquin(
                                    id = nuevoId,
                                    nombre = nombreMed.trim(),
                                    categoria = "General",
                                    unidadesDisponibles = cantidad
                                )
                            )

                            Toast.makeText(context, "Medicamento agregado a Mi Botiquín", Toast.LENGTH_SHORT).show()
                            nombreMed = ""
                            cantidadInicialText = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar al Botiquín")
                    }
                }
            }
        }

        // --- BUSCADOR POR NOMBRE ---
        item {
            OutlinedTextField(
                value = filtroBusqueda,
                onValueChange = { filtroBusqueda = it },
                label = { Text("🔍 Buscar por nombre en Mi Botiquín...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // --- LISTADO DE INVENTARIO ---
        item {
            Text(
                text = "Existencias en Mi Botiquín",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (medicamentosFiltrados.isEmpty()) {
            item {
                Text(
                    text = if (filtroBusqueda.isNotBlank()) "No se encontraron medicamentos con ese nombre." else "Tu botiquín está vacío.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            itemsIndexed(medicamentosFiltrados) { _, med ->
                val indexEnListaReal = EstadoApp.listaBotiquin.indexOf(med)
                val esAgotado = med.unidadesDisponibles <= 0
                val esStockBajo = med.unidadesDisponibles > 0 && med.unidadesDisponibles < med.limiteMinimo

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            esAgotado -> Color(0xFFFFCDD2)
                            esStockBajo -> Color(0xFFFFF9C4)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = med.nombre,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                if (esAgotado) {
                                    Text(
                                        text = "ESTADO: AGOTADO",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                } else {
                                    Text(
                                        text = "Unidades disponibles: ${med.unidadesDisponibles}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (esStockBajo) Color(0xFFE65100) else Color.Unspecified
                                    )
                                }
                            }

                            TextButton(onClick = {
                                if (indexEnListaReal != -1) {
                                    EstadoApp.listaBotiquin.removeAt(indexEnListaReal)
                                    Toast.makeText(context, "Medicamento eliminado del botiquín", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Eliminar", color = Color.Red)
                            }
                        }

                        if (esStockBajo) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFE65100),
                                shape = MaterialTheme.shapes.extraSmall,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ ALERTA: Quedan menos de 5 dosis (${med.unidadesDisponibles}). Comprar más pronto.",
                                    color = Color.White,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- ÚNICO BOTÓN: DESCONTAR TOMA ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (med.unidadesDisponibles > 0 && indexEnListaReal != -1) {
                                        EstadoApp.listaBotiquin[indexEnListaReal] = med.copy(
                                            unidadesDisponibles = med.unidadesDisponibles - 1
                                        )
                                        Toast.makeText(context, "Dosis descontada (-1)", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error: El stock disponible ya se agotó", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !esAgotado,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Descontar Toma (-1)")
                            }
                        }
                    }
                }
            }
        }
    }
}