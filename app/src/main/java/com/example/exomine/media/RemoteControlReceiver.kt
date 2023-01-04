package com.example.exomine.media


import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver
import com.example.exomine.parcelable


class RemoteControlReceiver : MediaButtonReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
            val event = intent.parcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)

//            when (event?.keyCode) {
//
//                KeyEvent.KEYCODE_MEDIA_PLAY -> MediaPlayerService.sendIntent(
//                    context,
//                    MediaPlayerService.ACTION_PLAY_PAUSE,
//                    -1
//                )
//                KeyEvent.KEYCODE_MEDIA_PAUSE -> MediaPlayerService.sendIntent(
//                    context,
//                    MediaPlayerService.ACTION_PAUSE,
//                    -1
//                )
//                KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> MediaPlayerService.sendIntent(
//                    context,
//                    MediaPlayerService.ACTION_SEEK_FORWARD,
//                    -1
//                )
//                KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> MediaPlayerService.sendIntent(
//                    context,
//                    MediaPlayerService.ACTION_SEEK_BACKWARD,
//                    -1
//                )
//            }
        }
    }
}