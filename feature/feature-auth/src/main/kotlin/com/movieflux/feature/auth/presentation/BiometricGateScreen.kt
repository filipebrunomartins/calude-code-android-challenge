package com.movieflux.feature.auth.presentation

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.movieflux.core.ui.components.LoadingState

@Composable
fun BiometricGateScreen(
    onAuthenticated: () -> Unit,
    onFallbackToLogin: () -> Unit,
    viewModel: BiometricGateViewModel = hiltViewModel(),
) {
    val activity = LocalContext.current as FragmentActivity
    val executor = remember(activity) { ContextCompat.getMainExecutor(activity) }

    LaunchedEffect(Unit) {
        val allowedAuthenticators = viewModel.allowedAuthenticators()
        if (allowedAuthenticators == null) {
            onFallbackToLogin()
            return@LaunchedEffect
        }

        val callback =
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthenticated()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    onFallbackToLogin()
                }
            }

        val promptInfoBuilder =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle("Entrar no MovieFlux")
                .setSubtitle("Use sua biometria para continuar")
                .setAllowedAuthenticators(allowedAuthenticators)

        // DEVICE_CREDENTIAL e setNegativeButtonText são mutuamente exclusivos na API do
        // AndroidX Biometric — só usamos o botão negativo quando o fallback de PIN/padrão
        // não está incluído (aparelhos com sensor apenas Class 2/BIOMETRIC_WEAK).
        if (allowedAuthenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL == 0) {
            promptInfoBuilder.setNegativeButtonText("Usar senha")
        }

        BiometricPrompt(activity, executor, callback).authenticate(promptInfoBuilder.build())
    }

    LoadingState()
}
