package com.movieflux.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.loginSucceeded, uiState.shouldOfferBiometric) {
        if (uiState.loginSucceeded && !uiState.shouldOfferBiometric) {
            onNavigateToHome()
        }
    }

    if (uiState.shouldOfferBiometric) {
        BiometricOptInDialog(
            onConfirm = { viewModel.onBiometricPreferenceSelected(true) },
            onDismiss = { viewModel.onBiometricPreferenceSelected(false) },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "MovieFlux", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.username,
            onValueChange = viewModel::onUsernameChanged,
            label = { Text("Usuário") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        uiState.errorMessage?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = viewModel::login,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                Text("Entrar")
            }
        }
    }
}

@Composable
private fun BiometricOptInDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ativar biometria?") },
        text = { Text("Use sua digital ou reconhecimento facial para entrar mais rápido da próxima vez.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Ativar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Agora não") } },
    )
}
