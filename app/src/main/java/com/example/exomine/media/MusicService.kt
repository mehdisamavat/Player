package com.example.exomine.media

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.*
import com.example.exomine.*
import com.example.exomine.R
import com.example.exomine.util.getPendingIntentFlag
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlin.math.max

open class MusicService : MediaBrowserServiceCompat() {



    private lateinit var notificationManager: MediaLibraryService

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


//    protected lateinit var mediaSession: MediaSessionCompat
    protected lateinit var mediaSession: MediaLibraryService.MediaLibrarySession

    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private var currentMediaItemIndex: Int = 0
    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()
    private val playerListener = PlayerEventListener()

    private val exoPlayer: Player by lazy {
        val player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
        player.addAnalyticsListener(EventLogger(null, "exoplayer-uamp"))
        player
    }

    private val replaceableForwardingPlayer: ReplaceableForwardingPlayer by lazy {
        ReplaceableForwardingPlayer(exoPlayer)
    }

    open fun getCallback(): MediaLibraryService.MediaLibrarySession.Callback {
        return MusicServiceCallback()
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

        }

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ): Boolean {
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
    private inner class PlayerEventListener : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)

        }


        override fun onEvents(player: Player, events: Events) {

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


    open inner class MusicServiceCallback: MediaLibraryService.MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(session: MediaLibraryService.MediaLibrarySession, browser: MediaSession.ControllerInfo, params: MediaLibraryService.LibraryParams?): ListenableFuture<LibraryResult<MediaItem>> {
            // By default, all known clients are permitted to search, but only tell unknown callers
            // about search if permitted by the [BrowseTree].
            val isKnownCaller = packageValidator.isKnownCaller(browser.packageName, browser.uid)
            val rootExtras = Bundle().apply {
                putBoolean(MEDIA_SEARCH_SUPPORTED, isKnownCaller || browseTree.searchableByUnknownCaller)
                putBoolean(CONTENT_STYLE_SUPPORTED, true)
                putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
                putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
            }
            val libraryParams = MediaLibraryService.LibraryParams.Builder().setExtras(rootExtras).build()
            val rootMediaItem = if (!isKnownCaller) {
                MediaItem.EMPTY
            } else if (params?.isRecent == true) {
                if (exoPlayer.currentTimeline.isEmpty) {
                    storage.loadRecentSong()?.let {
                        preparePlayerForResumption(it)
                    }
                }
                recentRootMediaItem
            } else {
                catalogueRootMediaItem
            }
            return Futures.immediateFuture(LibraryResult.ofItem(rootMediaItem, libraryParams))
        }

        override fun onGetChildren(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            if (parentId == recentRootMediaItem.mediaId) {
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        storage.loadRecentSong()?.let {
                                song -> listOf(song)
                        }!!,
                        MediaLibraryService.LibraryParams.Builder().build()
                    )
                )
            }
            return callWhenMusicSourceReady {
                LibraryResult.ofItemList(
                    browseTree[parentId] ?: ImmutableList.of(),
                    MediaLibraryService.LibraryParams.Builder().build()
                )
            }
        }

        override fun onGetItem(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return callWhenMusicSourceReady {
                LibraryResult.ofItem(
                    browseTree.getMediaItemByMediaId(mediaId) ?: MediaItem.EMPTY,
                    MediaLibraryService.LibraryParams.Builder().build())
            }
        }

        override fun onSearch(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return callWhenMusicSourceReady {
                val searchResult = musicSource.search(query, params?.extras ?: Bundle())
                mediaSession.notifySearchResultChanged(browser, query, searchResult.size, params)
                LibraryResult.ofVoid()
            }
        }

        override fun onGetSearchResult(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return callWhenMusicSourceReady {
                val searchResult = musicSource.search(query, params?.extras ?: Bundle())
                val fromIndex = max((page - 1) * pageSize, searchResult.size - 1)
                val toIndex = max(fromIndex + pageSize, searchResult.size)
                LibraryResult.ofItemList(searchResult.subList(fromIndex, toIndex), params)
            }
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            return callWhenMusicSourceReady {
                mediaItems.map { browseTree.getMediaItemByMediaId(it.mediaId)!! }.toMutableList()
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
        }
    }




    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(this, 0, sessionIntent, getPendingIntentFlag())
            }

        mediaSession = with(
            MediaLibraryService.MediaLibrarySession.Builder(this, replaceableForwardingPlayer, getCallback())) {
            setId(packageName)
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                setSessionActivity(
                    PendingIntent.getActivity(
                        /* context= */ this@MusicService,
                        /* requestCode= */ 0,
                        sessionIntent,
                        if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE
                        else PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }
            build()
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

