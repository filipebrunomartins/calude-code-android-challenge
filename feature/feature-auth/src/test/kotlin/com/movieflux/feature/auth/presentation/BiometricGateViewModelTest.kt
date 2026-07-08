package com.movieflux.feature.auth.presentation

import androidx.biometric.BiometricManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BiometricGateViewModelTest {
    private val biometricCapabilityChecker: BiometricCapabilityChecker = mockk()

    @Test
    fun `given checker retorna STRONG mais credencial, when allowedAuthenticators, then repassa o mesmo valor`() {
        // given
        every { biometricCapabilityChecker.allowedAuthenticators() } returns
            (BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        val viewModel = BiometricGateViewModel(biometricCapabilityChecker)

        // when
        val result = viewModel.allowedAuthenticators()

        // then
        assertEquals(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL, result)
    }

    @Test
    fun `given checker retorna null, when allowedAuthenticators, then repassa null`() {
        // given
        every { biometricCapabilityChecker.allowedAuthenticators() } returns null
        val viewModel = BiometricGateViewModel(biometricCapabilityChecker)

        // when
        val result = viewModel.allowedAuthenticators()

        // then
        assertNull(result)
    }
}
