package com.example.a043a21medicamentos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PantallaAlarmas() {
    val context = LocalContext.current

    var nombreMed by remember { mutableStateOf("") }
    var dosisNumero by remember { mutableStateOf("") }
    var horaSeleccionadaText by remember { mutableStateOf("") }

    var horaSeleccionada by remember { mutableStateOf(-1) }
    var minutoSeleccionado by remember { mutableStateOf(-1) }

    var nuevaAlergia by remember { mutableStateOf("") }
    var mostrarAdvertenciaAlergia by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            horaSeleccionada = hourOfDay
            minutoSeleccionado = minute
            val amPm = if (hourOfDay >= 12) "PM" else "AM"
            val hora12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            val minStr = if (minute < 10) "0$minute" else "$minute"
            horaSeleccionadaText = "$hora12:$minStr $amPm"
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    fun mostrarNotificacionBarra(nombreMedicamento: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "canal_alarmas_v3"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Tomas",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💊 Recordatorio de Medicamento")
            .setContentText("Es hora de tomar tu medicamento: $nombreMedicamento")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // Programar la notificación y registrar de forma automática en el Historial cuando suene
    fun programarNotificacion(nombreMedicamento: String, dosis: String, hora: Int, minuto: Int) {
        val ahora = Calendar.getInstance()
        val objetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            if (before(ahora)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val tiempoDiferenciaMillis = objetivo.timeInMillis - ahora.timeInMillis

        Handler(Looper.getMainLooper()).postDelayed({
            // 1. Mostrar notificación visual
            mostrarNotificacionBarra(nombreMedicamento)

            // 2. REGISTRO AUTOMÁTICO EN EL HISTORIAL
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaHoraActual = sdf.format(Date())
            val nuevoIdHistorial = (EstadoApp.listaHistorial.maxOfOrNull { it.id } ?: 0) + 1

            EstadoApp.listaHistorial.add(
                0, // Insertar al inicio de la lista
                RegistroToma(
                    id = nuevoIdHistorial,
                    nombreMedicamento = "$nombreMedicamento ($dosis mg)",
                    fechaHora = fechaHoraActual,
                    estado = "Tomada"
                )
            )
        }, tiempoDiferenciaMillis)
    }

    fun guardarAlarmaExitosamente() {
        val nuevoId = (EstadoApp.listaAlarmas.maxOfOrNull { it.id } ?: 0) + 1
        val dosisFinal = "$dosisNumero mg"

        programarNotificacion(nombreMed.trim(), dosisNumero.trim(), horaSeleccionada, minutoSeleccionado)

        EstadoApp.listaAlarmas.add(
            AlarmaMedicamento(
                id = nuevoId,
                nombreMedicamento = nombreMed.trim(),
                dosis = dosisFinal,
                horaToma = horaSeleccionadaText
            )
        )

        Toast.makeText(context, "Recordatorio guardado exitosamente para las $horaSeleccionadaText", Toast.LENGTH_SHORT).show()

        nombreMed = ""
        dosisNumero = ""
        horaSeleccionadaText = ""
        horaSeleccionada = -1
        minutoSeleccionado = -1
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configurar Alarma de Medicamento",
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
                        value = dosisNumero,
                        onValueChange = { dosisNumero = it },
                        label = { Text("Dosis *") },
                        suffix = { Text("mg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // SECCIÓN DE HORA REDISEÑADA EN COLUMNA PARA EVITAR DESCUADRES
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Hora de la toma *", fontWeight = FontWeight.Bold)

                        OutlinedButton(
                            onClick = { timePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (horaSeleccionadaText.isEmpty()) "Seleccionar Hora" else "Hora: $horaSeleccionadaText"
                            )
                        }

                        if (horaSeleccionadaText.isEmpty()) {
                            Text(
                                text = "Debe seleccionar la hora de la toma",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Validación: Nombre, Dosis y Hora obligatorios
                            if (nombreMed.isBlank() || dosisNumero.isBlank() || horaSeleccionadaText.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Error: El nombre, la dosis y la hora son campos obligatorios",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            val esAlergico = EstadoApp.listaAlergias.any {
                                it.descripcion.equals(nombreMed.trim(), ignoreCase = true)
                            }

                            if (esAlergico) {
                                mostrarAdvertenciaAlergia = true
                            } else {
                                guardarAlarmaExitosamente()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Recordatorio")
                    }
                }
            }
        }

        item {
            Text(
                text = "Alarmas y Recordatorios Guardados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (EstadoApp.listaAlarmas.isEmpty()) {
            item {
                Text(
                    text = "No tienes alarmas configuradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            items(EstadoApp.listaAlarmas) { alarma ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = alarma.nombreMedicamento, fontWeight = FontWeight.Bold)
                            Text(text = "Dosis: ${alarma.dosis} | Hora: ${alarma.horaToma}")
                        }

                        TextButton(onClick = {
                            EstadoApp.listaAlarmas.remove(alarma)
                            Toast.makeText(context, "Alarma eliminada", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Eliminar", color = Color.Red)
                        }
                    }
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alergias y Condiciones Médicas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevaAlergia,
                            onValueChange = { nuevaAlergia = it },
                            label = { Text("Nueva Alergia (ej. Penicilina)") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (nuevaAlergia.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Error: El campo de alergia es obligatorio",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                EstadoApp.listaAlergias.add(Alergia(nuevaAlergia.trim()))
                                Toast.makeText(context, "Alergia registrada", Toast.LENGTH_SHORT).show()
                                nuevaAlergia = ""
                            }
                        }) {
                            Text("Agregar")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    EstadoApp.listaAlergias.forEach { alergia ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "• ${alergia.descripcion}")
                            TextButton(onClick = {
                                EstadoApp.listaAlergias.remove(alergia)
                                Toast.makeText(context, "Alergia eliminada", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Eliminar", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarAdvertenciaAlergia) {
        AlertDialog(
            onDismissRequest = { mostrarAdvertenciaAlergia = false },
            title = { Text("⚠️ ADVERTENCIA DE CONTRAINDICACIÓN") },
            text = { Text("El medicamento '$nombreMed' coincide con una alergia registrada en tu perfil. Su ingesta está contraindicada.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarAdvertenciaAlergia = false
                    guardarAlarmaExitosamente()
                }) {
                    Text("Continuar de todos modos")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarAdvertenciaAlergia = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}