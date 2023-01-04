package com.example.exomine.media

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.exomine.util.getPendingIntentFlag
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager.*


 class CustomActionMusic(val context: Context) : CustomActionReceiver {
    override fun createCustomActions(context: Context, instanceId: Int): Map<String, NotificationCompat.Action> {

//        val piContentIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntentHelper.getPendingIntentFlag())
        val piPrev = PendingIntent.getService(context, 0, Intent(context, MusicService::class.java).setAction(ACTION_PREVIOUS), getPendingIntentFlag())
        val piNext = PendingIntent.getService(context, 0, Intent(context, MusicService::class.java).setAction(ACTION_NEXT),getPendingIntentFlag())
        val piPause = PendingIntent.getService(context, 0, Intent(context, MusicService::class.java).setAction(ACTION_PAUSE), getPendingIntentFlag()) //check play and pause intent
        val piPlay = PendingIntent.getService(context, 0, Intent(context, MusicService::class.java).setAction(ACTION_PLAY), getPendingIntentFlag()) //check play and pause intent
//        val piStop = PendingIntent.getService(context, 0, Intent(context, MediaPlayerService::class.java).setAction(MediaPlayerService.ACTION_STOP_SERVICE), getPendingIntentFlag())
//        val piBuffering = PendingIntent.getService(context, 0, Intent(context, MediaPlayerService::class.java).setAction(MediaPlayerService.ACTION_NOTHING_SERVICE), PendingIntentHelper

//        val intent = Intent("Favourite").setPackage(context.packageName)
//        val pendingIntent = PendingIntent.getBroadcast(context, instanceId, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val prev: NotificationCompat.Action = NotificationCompat.Action(com.google.android.exoplayer2.ui.R.drawable.exo_icon_previous, ACTION_PREVIOUS, null)
        val play: NotificationCompat.Action = NotificationCompat.Action(com.google.android.exoplayer2.ui.R.drawable.exo_icon_play, ACTION_PLAY, null)
        val pause: NotificationCompat.Action = NotificationCompat.Action(com.google.android.exoplayer2.ui.R.drawable.exo_icon_pause, ACTION_PAUSE,null)
        val next: NotificationCompat.Action = NotificationCompat.Action(com.google.android.exoplayer2.ui.R.drawable.exo_icon_next, ACTION_NEXT, null)

        val actionMap: MutableMap<String, NotificationCompat.Action> = HashMap()
        actionMap[ACTION_PREVIOUS] = prev
        actionMap[ACTION_PLAY] = play
        actionMap[ACTION_PAUSE] = pause
        actionMap[ACTION_NEXT] = next

        return actionMap
    }

    override fun getCustomActions(player: Player): List<String> {
        val customActions: MutableList<String> = ArrayList()
        customActions.add(ACTION_PREVIOUS)
        if (player.playWhenReady) {
            customActions.add(ACTION_PAUSE)
        } else {
            customActions.add(ACTION_PLAY)
        }
        customActions.add(ACTION_NEXT)

        return customActions
    }

    override fun onCustomAction(player: Player, action: String, intent: Intent) {
        context.startService(intent)
//        when (action) {
//            ACTION_PLAY -> {
//                Log.d("test tag", "play")
//
//                controlDispatcher.dispatchSetPlayWhenReady(player, action == ACTION_PLAY)
//            }
//            ACTION_PAUSE -> {
//                Log.d("test tag", "pause")
//                controlDispatcher.dispatchSetPlayWhenReady(player, action == ACTION_PLAY)
//            }
//            ACTION_NEXT -> {
//                Log.d("test tag", "next")
//                val nextWindowIndex = player.nextWindowIndex
//                if (nextWindowIndex != C.INDEX_UNSET) {
//                    controlDispatcher.dispatchSeekTo(player, nextWindowIndex, C.TIME_UNSET)
//                }
//            }
//            ACTION_PREVIOUS -> {
//                Log.d("test tag", "prev")
//                player.currentTimeline.getWindow(player.currentWindowIndex, SystemColor.window)
//                val previousWindowIndex = player.previousWindowIndex
//                if (previousWindowIndex != C.INDEX_UNSET && SystemColor.window.isDynamic && !SystemColor.window.isSeekable) {
//                    controlDispatcher.dispatchSeekTo(player, previousWindowIndex, C.TIME_UNSET)
//                }
//            }
//        }
    }
}