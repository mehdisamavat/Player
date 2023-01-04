package com.example.exomine.ui.musiclist

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import com.example.exomine.Post
import com.example.exomine.R
import com.example.exomine.id
import com.example.exomine.isPlaying
import com.example.exomine.media.EMPTY_PLAYBACK_STATE
import com.example.exomine.media.MusicServiceConnection
import com.example.exomine.media.NOTHING_PLAYING
import com.example.exomine.ui.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicListViewModel @Inject constructor(musicServiceConnection: MusicServiceConnection) : MainViewModel(musicServiceConnection) {

}

