package com.example.a043a21medicamentos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PantallaHistorial() {
    var filtroMedicamento by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("TODOS") }

    val historialFiltrado = EstadoApp.listaHistorial.filter { registro ->
        val coincideNombre = registro.nombreMedicamento.contains(filtroMedicamento.trim(), ignoreCase = true)
        val coincideEstado = if (filtroEstado == "TODOS") true else registro.estado.equals(filtroEstado, ignoreCase = true)
        coincideNombre && coincideEstado
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- FILTROS DE BÚSQUEDA ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Historial de Tomas Realizadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = filtroMedicamento,
                    onValueChange = { filtroMedicamento = it },
                    label = { Text("🔍 Filtrar por nombre de medicamento...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroEstado == "TODOS",
                        onClick = { filtroEstado = "TODOS" },
                        label = { Text("Todos") }
                    )
                    FilterChip(
                        selected = filtroEstado == "Tomada",
                        onClick = { filtroEstado = "Tomada" },
                        label = { Text("Tomadas") }
                    )
                    FilterChip(
                        selected = filtroEstado == "Omitida",
                        onClick = { filtroEstado = "Omitida" },
                        label = { Text("Omitidas") }
                    )
                }
            }
        }

        // --- LISTADO AUTOMÁTICO DE HISTORIAL ---
        if (historialFiltrado.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay registros de tomas en este periodo.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(historialFiltrado) { registro ->
                val esTomada = registro.estado.equals("Tomada", ignoreCase = true)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esTomada) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = registro.nombreMedicamento,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Fecha y hora: ${registro.fechaHora}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Surface(
                            color = if (esTomada) Color(0xFF2E7D32) else Color(0xFFC62828),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = registro.estado.uppercase(),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}