package com.matejdro.micropebble.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.matejdro.micropebble.R
import com.matejdro.micropebble.common.notifications.NotificationKeys
import dev.zacsweers.metro.Inject
import com.matejdro.micropebble.sharedresources.R as sharedR

@Inject
class NotificationChannelManager(context: Context) {
   init {
      val notificationManager = context.getSystemService<NotificationManager>()!!

      notificationManager.createNotificationChannel(
         NotificationChannel(
            NotificationKeys.CHANNEL_CONNECTING,
            context.getString(sharedR.string.connecting),
            NotificationManager.IMPORTANCE_LOW
         )
      )

      notificationManager.createNotificationChannel(
         NotificationChannel(
            NotificationKeys.CHANNEL_NO_VOICE,
            context.getString(R.string.voice_issues),
            NotificationManager.IMPORTANCE_DEFAULT
         )
      )
   }
}
