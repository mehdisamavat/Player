package com.example.exomine.media

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.exomine.*
import com.example.exomine.R
import com.example.exomine.util.getPendingIntentFlag
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.Util.constrainValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob

class MusicService : MediaBrowserServiceCompat() {



    private lateinit var notificationManager: UampNotificationManager

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


    protected lateinit var mediaSession: MediaSessionCompat
    protected lateinit var mediaSessionConnector: MediaSessionConnector
    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private var currentMediaItemIndex: Int = 0
    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C. AUDIO_CONTENT_TYPE_MUSIC )
        .setUsage(C.USAGE_MEDIA)
        .build()
    private val playerListener = PlayerEventListener()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
    }

    private inner class UampQueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            if (  currentPlaylistItems.size>windowIndex) {
                return currentPlaylistItems[windowIndex].description
            }
            return MediaDescriptionCompat.Builder().build()
        }
    }
    private inner class UampPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE or
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            TODO("Not yet implemented")
        }
        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            currentPlaylistItems=extras?.parcelableArrayList<Post>("list")?.map { it->MediaMetadataCompat.Builder().from(it).build() }!!
            notifyChildrenChanged("root")
            preparePlaylist(currentPlaylistItems, mediaId,playWhenReady,0)

        }


        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {

//            mediaSource.whenReady {
//                val metadataList = mediaSource.search(query, extras ?: Bundle.EMPTY)
//                if (metadataList.isNotEmpty()) {
//                    preparePlaylist(
//                        metadataList,
//                        metadataList[0],
//                        playWhenReady,
//                        playbackStartPositionMs = C.TIME_UNSET
//                    )
//                }
//            }
        }

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ): Boolean {
//            if (command == "set") {
//                val list: ArrayList<Post> = extras?.getParcelableArrayList("list")!!
//                currentPlaylistItems = list.map {
//                    val jsonImageUri = Uri.parse(it.image)
//                    val imageUri = mapUri(jsonImageUri)
//                    MediaMetadataCompat.Builder()
//                        .from(it)
//                        .apply {
//                            displayIconUri =
//                                imageUri.toString() // Used by ExoPlayer and Notification
//                            albumArtUri = imageUri.toString()
//                            // Keep the original artwork URI for being included in Cast metadata object.
//                            putString(ORIGINAL_ARTWORK_URI_KEY, jsonImageUri.toString())
//                        }
//                        .build()
//                }
//            }
//            notifyChildrenChanged(MY_MEDIA_ROOT_ID)

            return true
        }

    }
    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing ) {
                ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, this@MusicService.javaClass))
                startForeground(notificationId, notification)
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            stopSelf()
        }
    }
    private inner class PlayerEventListener : Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
//            var actions = MEDIA_SESSION_ACTIONS
//            var playbackStateCompatInt:Int=PlaybackStateCompat.STATE_NONE
//
//            when (playbackState) {
//                STATE_BUFFERING -> {
//                    playbackStateCompatInt=PlaybackStateCompat.STATE_BUFFERING
//                    actions = actions or PlaybackStateCompat.ACTION_PAUSE }
//                STATE_ENDED -> {
//                    Log.i("mehdi", "STATE_ENDED")
//                    playbackStateCompatInt=PlaybackStateCompat.STATE_STOPPED
//                    actions = actions or PlaybackStateCompat.ACTION_PLAY }
//                STATE_IDLE -> {
//                    Log.i("mehdi", "STATE_IDLE")
//                    playbackStateCompatInt=PlaybackStateCompat.STATE_NONE
//                    actions = actions or PlaybackStateCompat.ACTION_PLAY
//                }
//                STATE_READY -> {
//                    Log.i("mehdi", "STATE_READY")
//                }
//            }

//            mediaSession.setPlaybackState(
//                PlaybackStateCompat.Builder()
//                    .setState(playbackStateCompatInt, 0, 1.0f)
//                    .setActions(actions)
//                    .build())
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
//            var actions = MEDIA_SESSION_ACTIONS
//            var playbackStateCompatInt:Int=PlaybackStateCompat.STATE_NONE
//
//            if (playWhenReady){
//                Log.i("mehdi", "ReadyChanged  $playWhenReady")
//                playbackStateCompatInt=PlaybackStateCompat.STATE_PLAYING
//                actions = actions or PlaybackStateCompat.ACTION_PAUSE
//            }else{
//                playbackStateCompatInt=PlaybackStateCompat.STATE_PAUSED
//                Log.i("mehdi", "ReadyChanged  $playWhenReady")
//                actions = actions or PlaybackStateCompat.ACTION_PLAY
//            }

//            mediaSession.setPlaybackState(
//                PlaybackStateCompat.Builder()
//                    .setState(playbackStateCompatInt, 0, 1.0f)
//                    .setActions(actions)
//                    .build())

        }


        override fun onEvents(player: Player, events: Events) {
//            Log.i("mehdi","PlayerEventListener   onEvents()  $events")
//            if (events.contains(EVENT_POSITION_DISCONTINUITY)
//                || events.contains(EVENT_MEDIA_ITEM_TRANSITION)
//                || events.contains(EVENT_PLAY_WHEN_READY_CHANGED)
//            ) {
//                currentMediaItemIndex = if (currentPlaylistItems.isNotEmpty()) {
//                    constrainValue(player.currentMediaItemIndex, 0,currentPlaylistItems.size - 1)
//                } else 0
//            }
        }

        override fun onPlayerError(error: PlaybackException) {
            var message = R.string.generic_error;
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
                || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            ) {
                message = R.string.error_media_not_found;
            }
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
    private fun preparePlaylist(playList: List<MediaMetadataCompat>, itemToPlay: String, playWhenReady: Boolean, playbackStartPositionMs: Long) {

        val initialWindowIndex = if (itemToPlay.isNotEmpty())  currentPlaylistItems.indexOfFirst { it.id==itemToPlay } else -1

        exoPlayer.setMediaItems(currentPlaylistItems.map { it.toMediaItem() }, initialWindowIndex, playbackStartPositionMs)
        exoPlayer.prepare()
        if (initialWindowIndex>=0){
            exoPlayer.playWhenReady=true
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(this, 0, sessionIntent, getPendingIntentFlag())
            }

        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }
        mediaSession.setCallback(object :MediaSessionCompat.Callback(){
            override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            }
            override fun onPlay() {

                super.onPlay()
            }
            override fun onPause() {

                super.onPause()
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
            }

            override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            }
        })
        sessionToken = mediaSession.sessionToken


        notificationManager = UampNotificationManager(this, mediaSession.sessionToken, PlayerNotificationListener())


        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(UampPlaybackPreparer())
        mediaSessionConnector.setQueueNavigator(UampQueueNavigator(mediaSession))
        mediaSessionConnector.setPlayer(exoPlayer)

        notificationManager.showNotificationForPlayer(exoPlayer)

    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {

        return BrowserRoot("root", null)
    }


    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        return result.sendResult(currentPlaylistItems.map {
            MediaBrowserCompat.MediaItem(
                it.description,
                it.flag
            )
        })
    }



}


private const val MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_FAST_FORWARD or
        PlaybackStateCompat.ACTION_REWIND or
        PlaybackStateCompat.ACTION_SEEK_TO or
        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
        PlaybackStateCompat.ACTION_FAST_FORWARD
/*
 * (Media) Session events
 */
const val NETWORK_FAILURE = "com.example.android.uamp.media.session.NETWORK_FAILURE"

