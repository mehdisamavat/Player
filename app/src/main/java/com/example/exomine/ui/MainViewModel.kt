package com.example.exomine.ui

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import com.example.exomine.*
import com.example.exomine.media.EMPTY_PLAYBACK_STATE
import com.example.exomine.media.MusicServiceConnection
import com.example.exomine.media.NOTHING_PLAYING
import com.example.exomine.ui.detailmusic.DetailMusicViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
open  class MainViewModel   @Inject constructor( private val musicServiceConnection: MusicServiceConnection):ViewModel() {

    val data = arrayListOf(
        Post(
            id = "1",
            title = "Life Time Episode 02",
            artist = "Dj SH.Y",
            source = "https://fs.vusic.ir/posts/33690/YCDARMDOnrX47phvR6nk3dBHYVL4jIfP2vfZkhYt.mp3",
            image = "https://fs.vusic.ir/posts/33690/nJCUGnaGDXkye3Jpmp4uSwAZbECXp5SeU8mWPhRc.jpg",
        ),
        Post(
            id = "2",
            title = "Iceverb Episode 04",
            artist = "Dj Crazy",
            source = "https://fs.vusic.ir/posts/33184/DdFLqa7x45B18kMOEtnkJnIQtqxOKwv7hzwMOPwv.mp3",
            image = "https://fs.vusic.ir/posts/33184/ShNVD96q36LP1xXDZ3r9MJx7KattnwaCP6lzQADl.jpg",

            ),
        Post(
            id = "3",
            title = "Midnight EP 03",
            artist = "Dj Romi",
            source = "https://fs.vusic.ir/posts/9339/jxsCzeBmUPo7Z66L2cXyEHMsJBT936WKa0TcLhD4.mp3",
            image = "https://fs.vusic.ir/posts/9339/6gkEzV2Pgs9rfR4FYiEgBlhmrxIYivNjuH8pQiSD.jpg",
        ),
    )

    private val _mediaItems = MutableLiveData<List<Post>>()
    val mediaItems: LiveData<List<Post>> = _mediaItems
    val mediaButtonRes = MutableLiveData<Int>().apply { postValue(R.drawable.ic_album_black_24dp) }
    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    val mediaPosition = MutableLiveData<Long>().apply { postValue(0L) }
    var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())
    val nowPlayingMetaDataCompat = MutableLiveData<MediaMetadataCompat>()


    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: List<MediaBrowserCompat.MediaItem>
        ) {
            val itemsList = children.map { child ->
                val subtitle = child.description.subtitle ?: ""
                Post(
                    id = child.mediaId!!,
                    title = child.description.title.toString(),
                    artist = subtitle.toString(),
                    source = child.description.mediaUri.toString(),
                    image = child.description.iconUri.toString(),
                    playbackRes = getResourceForMediaId(child.mediaId!!)
                )
            }
            _mediaItems.postValue(itemsList)

        }
    }
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
         playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
            viewModelScope.launch {
                delay(100)
                _mediaItems.postValue(updateState(playbackState, metadata))
            }
        }
    }
    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        val playbackState = musicServiceConnection.playbackState.value ?: EMPTY_PLAYBACK_STATE
        val metadata = it ?: NOTHING_PLAYING
        nowPlayingMetaDataCompat.postValue(it)
        if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
            viewModelScope.launch {
                delay(100)
                _mediaItems.postValue(updateState(playbackState, metadata))
            }
        }
    }
    private val connectedConnectionObserver = Observer<Boolean> {
        if (it){
            musicServiceConnection.subscribe(musicServiceConnection.rootMediaId, subscriptionCallback)
            musicServiceConnection.transportControls.playFromMediaId("none", bundleOf().apply { putParcelableArrayList("list",data) })
        }
    }
    val networkError = Transformations.map(musicServiceConnection.networkFailure) { it }
    init {
        musicServiceConnection.playbackState.observeForever(playbackStateObserver)
        musicServiceConnection.nowPlaying.observeForever(mediaMetadataObserver)
        musicServiceConnection.isConnected.observeForever(connectedConnectionObserver)
    }


    fun playMedia(mediaItemId:String,playList:ArrayList<Post>?= data) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItemId == nowPlaying?.id) {
            musicServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying ->  transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(TAG, "Playable item clicked but neither play nor pause are enabled!" + " (mediaId=${mediaItemId})")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaItemId, bundleOf().apply { putParcelableArrayList("list",playList) })
        }
    }
    fun seekto(  progress:Int,fromUser :Boolean){
        if(fromUser)
        musicServiceConnection.transportControls.seekTo(progress.toLong())

    }

    fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val currPosition = playbackState.currentPlayBackPosition
        if (mediaPosition.value != currPosition)
            mediaPosition.postValue(currPosition)
        if (updatePosition)
            checkPlaybackPosition()
    }, POSITION_UPDATE_INTERVAL_MILLIS)



    private fun getResourceForMediaId(mediaId: String): Int {
        val isActive = mediaId == musicServiceConnection.nowPlaying.value?.id
        val isPlaying = musicServiceConnection.playbackState.value?.isPlaying ?: false
        return when {
            !isActive -> NO_RES
            isPlaying -> R.drawable.ic_pause_black_24dp
            else -> R.drawable.ic_play_arrow_black_24dp
        }
    }
    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat
    ): List<Post> {

        if (mediaMetadata.duration != 0L && mediaMetadata.id == musicServiceConnection.nowPlaying.value?.id) {
            this.nowPlayingMetaDataCompat.postValue(mediaMetadata)
        }

        // Update the media button resource ID
        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.ic_pause_black_24dp
                else -> R.drawable.ic_play_arrow_black_24dp
            }
        )

        return mediaItems.value?.map { val useResId = if (it.id == mediaMetadata.id) mediaButtonRes.value!! else NO_RES
            it.copy(playbackRes = useResId)
        } ?: emptyList()
    }



    override fun onCleared() {
        super.onCleared()
        updatePosition = false
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)
        musicServiceConnection.unsubscribe("root", subscriptionCallback)
    }

}
private const val NO_RES = 0
private const val POSITION_UPDATE_INTERVAL_MILLIS = 100L
private const val TAG = "MainActivitytVM"
