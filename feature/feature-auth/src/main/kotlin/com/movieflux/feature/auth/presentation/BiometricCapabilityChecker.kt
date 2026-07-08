package com.movieflux.feature.auth.presentation

import android.content.Context
import androidx.biometric.BiometricManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface BiometricCapabilityChecker {
    /**
     * Retorna a combinação de [BiometricManager.Authenticators] que o dispositivo consegue
     * satisfazer agora (para usar em [androidx.biometric.BiometricPrompt.PromptInfo]), ou `null`
     * se nenhuma biometria estiver disponível.
     */
    fun allowedAuthenticators(): Int?
}

class AndroidBiometricCapabilityChecker
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : BiometricCapabilityChecker {
        override fun allowedAuthenticators(): Int? {
            val manager = BiometricManager.from(context)
            val strongWithCredential =
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

            return when {
                // Sensores certificados Class 3, com fallback nativo de PIN/padrão.
                manager.canAuthenticate(strongWithCredential) == BiometricManager.BIOMETRIC_SUCCESS -> strongWithCredential
                // Aparelhos mais antigos (ex. Android 9) cujo sensor de digital não é certificado
                // como Class 3 pelo framework, mas tem biometria real cadastrada e funcional.
                // BIOMETRIC_WEAK não pode ser combinado com DEVICE_CREDENTIAL na mesma prompt.
                manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS ->
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                else -> null
            }
        }
    }
