package com.example.exomine.di

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import com.example.exomine.media.MusicService
import com.example.exomine.media.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMediaBrowserServiceConnection(@ApplicationContext appContext: Context): MusicServiceConnection {
        return MusicServiceConnection(appContext, ComponentName(appContext, MusicService::class.java))
    }
}
