package com.materjdro.micropebble.voice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.matejdro.micropebble.common.di.ServiceKey
import com.matejdro.micropebble.voice.data.R
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * A dummy service that does nothing but run in foreground.
 * This is required for having background microphone permission in Android 11+
 */
@ContributesIntoMap(AppScope::class)
@ServiceKey(VoicePermissionService::class)
@Inject
class VoicePermissionService : Service() {
   override fun onCreate() {
      super.onCreate()

      createChannel()

      startForeground(
         NOTIFICATION_ID,
         NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle(getString(R.string.voice_recognition))
            .setContentText(getString(R.string.voice_recognition_description))
            .build(),
         ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
      )
   }

   override fun onBind(intent: Intent?): IBinder? {
      return null
   }

   private fun createChannel() {
      val channel = NotificationChannel(
         CHANNEL_ID,
         getString(R.string.voice_recognition),
         NotificationManager.IMPORTANCE_DEFAULT
      )
      val manager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(channel)
   }
}

@Inject
@SingleIn(AppScope::class)
class VoicePermissionServiceControl(
   private val context: Context,
) {
   fun start() {
      context.startForegroundService(Intent(context, VoicePermissionService::class.java))
   }

   fun stop() {
      context.stopService(Intent(context, VoicePermissionService::class.java))
   }
}

private const val CHANNEL_ID = "voice_channel_id"
private const val NOTIFICATION_ID = 12378
