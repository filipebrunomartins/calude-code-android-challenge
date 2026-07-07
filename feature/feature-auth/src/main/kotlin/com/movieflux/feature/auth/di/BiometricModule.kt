package com.movieflux.feature.auth.di

import com.movieflux.feature.auth.presentation.AndroidBiometricCapabilityChecker
import com.movieflux.feature.auth.presentation.BiometricCapabilityChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BiometricModule {
    @Binds
    abstract fun bindBiometricCapabilityChecker(impl: AndroidBiometricCapabilityChecker): BiometricCapabilityChecker
}
