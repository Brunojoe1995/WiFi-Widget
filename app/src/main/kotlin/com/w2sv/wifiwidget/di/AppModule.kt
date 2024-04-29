package com.w2sv.wifiwidget.di

import android.content.Context
import android.location.LocationManager
import androidx.compose.material3.SnackbarVisuals
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SnackbarVisualsFlow

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WidgetPinSuccessFlow

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @SnackbarVisualsFlow
    @Provides
    @Singleton
    fun mutableSnackbarVisualsFlow(): MutableSharedFlow<(Context) -> SnackbarVisuals> =
        MutableSharedFlow()

    @WidgetPinSuccessFlow
    @Provides
    @Singleton
    fun mutableWidgetPinSuccessFlow(): MutableSharedFlow<Unit> =
        MutableSharedFlow()

    @Provides
    @Singleton
    fun locationManager(@ApplicationContext context: Context): LocationManager =
        context.getSystemService(LocationManager::class.java)
}