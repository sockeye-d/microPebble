package com.matejdro.micropebble.webservices.ui

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.navigation.keys.WebservicesAuthScreenKey
import com.matejdro.micropebble.webservices.api.WebservicesClient
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class WebservicesAuthViewModel(
   private val client: WebservicesClient,
   private val resources: CoroutineResourceManager,
) : SingleScreenViewModel<WebservicesAuthScreenKey>(resources.scope) {
   val authToken = client.token
}
