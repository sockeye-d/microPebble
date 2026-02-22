package com.materjdro.micropebble.voice

import android.app.Service
import android.companion.CompanionDeviceService
import android.os.Build
import androidx.annotation.RequiresApi
import com.matejdro.micropebble.common.di.ServiceKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.util.concurrent.atomic.AtomicInteger

@Suppress("DEPRECATION")
@ContributesIntoMap(AppScope::class, binding = binding<Service>())
@ServiceKey(CompanionDeviceListenerService::class)
@Inject
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class CompanionDeviceListenerService(
   private val voiceControlService: VoicePermissionServiceControl,
) : CompanionDeviceService() {
   val appearedDevices = AtomicInteger(0)

   @Deprecated("Deprecated in Java")
   override fun onDeviceAppeared(address: String) {
      if (appearedDevices.incrementAndGet() == 1) {
         voiceControlService.start()
      }
      super.onDeviceAppeared(address)
   }

   @Deprecated("Deprecated in Java")
   override fun onDeviceDisappeared(address: String) {
      if (appearedDevices.decrementAndGet() == 0) {
         voiceControlService.stop()
      }
      super.onDeviceDisappeared(address)
   }
}
