package com.example.a043a21medicamentos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var pantallaActual by remember { mutableStateOf("ALARMAS") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestor de Medicamentos") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pantallaActual == "ALARMAS",
                    onClick = { pantallaActual = "ALARMAS" },
                    icon = { Text("⏰") },
                    label = { Text("Alarmas") }
                )
                NavigationBarItem(
                    selected = pantallaActual == "BOTIQUIN",
                    onClick = { pantallaActual = "BOTIQUIN" },
                    icon = { Text("📦") },
                    label = { Text("Mi Botiquín") }
                )
                NavigationBarItem(
                    selected = pantallaActual == "HISTORIAL",
                    onClick = { pantallaActual = "HISTORIAL" },
                    icon = { Text("📋") },
                    label = { Text("Historial") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (pantallaActual) {
                "ALARMAS" -> PantallaAlarmas()
                "BOTIQUIN" -> PantallaBotiquin()
                "HISTORIAL" -> PantallaHistorial()
            }
        }
    }
}