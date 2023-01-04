package com.example.exomine.ui.detailmusic

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.exomine.*
import com.example.exomine.media.EMPTY_PLAYBACK_STATE
import com.example.exomine.media.MusicServiceConnection
import com.example.exomine.media.NOTHING_PLAYING
import com.example.exomine.ui.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
class DetailMusicViewModel @Inject constructor( private val musicServiceConnection: MusicServiceConnection)  : MainViewModel(musicServiceConnection) {

    init {
        checkPlaybackPosition()
    }

    override fun onCleared() {
        super.onCleared()
        updatePosition = false
    }

}






