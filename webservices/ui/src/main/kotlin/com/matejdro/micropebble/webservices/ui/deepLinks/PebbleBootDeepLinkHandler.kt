package com.matejdro.micropebble.webservices.ui.deepLinks

import android.net.Uri
import com.matejdro.micropebble.navigation.keys.WebservicesAuthScreenKey
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.deeplink.matchDeepLink
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.OpenScreenOrMoveToTop
import java.net.URLDecoder
import kotlin.uuid.Uuid

@ContributesIntoSet(OuterNavigationScope::class)
@Inject
class PebbleBootDeepLinkHandler : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean) = uri.matchDeepLink("pebble://custom-boot-config-url/{args}") {
      val key = WebservicesAuthScreenKey(URLDecoder.decode(it["args"], Charsets.UTF_8.name())!!, Uuid.random())
      return OpenScreenOrMoveToTop(key)
   }
}
