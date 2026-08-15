package com.universidad.reta2.ui.screens.splash

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.ui.navigation.Screen

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
    val networkState by viewModel.networkState.collectAsState()
    val context = LocalContext.current

    // Navegar cuando hay sesión verificada
    LaunchedEffect(isUserLoggedIn) {
        when (isUserLoggedIn) {
            true -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            false -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            null -> Unit
        }
    }

    // Dialog de sin conexión
    if (networkState is SplashViewModel.NetworkState.Disconnected) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Sin conexión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "No se detectó conexión a internet. Puedes continuar en modo offline " +
                           "y tu progreso se sincronizará automáticamente cuando tengas conexión.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.proceedOffline() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Continuar sin conexión")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        // Cerrar la app
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("Salir")
                }
            }
        )
    }

    // UI de carga
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reta2",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = when (networkState) {
                    is SplashViewModel.NetworkState.Checking -> "Verificando conexión..."
                    is SplashViewModel.NetworkState.Connected -> "Sincronizando..."
                    is SplashViewModel.NetworkState.Disconnected -> ""
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}