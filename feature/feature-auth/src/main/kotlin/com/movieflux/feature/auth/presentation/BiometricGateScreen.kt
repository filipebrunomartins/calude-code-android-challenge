package com.movieflux.feature.auth.presentation

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.movieflux.core.ui.components.LoadingState

@Composable
fun BiometricGateScreen(
    onAuthenticated: () -> Unit,
    onFallbackToLogin: () -> Unit,
) {
    val activity = LocalContext.current as FragmentActivity
    val executor = remember(activity) { ContextCompat.getMainExecutor(activity) }

    LaunchedEffect(Unit) {
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

        val promptInfo =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle("Entrar no MovieFlux")
                .setSubtitle("Use sua biometria para continuar")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                ).build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }

    LoadingState()
}
