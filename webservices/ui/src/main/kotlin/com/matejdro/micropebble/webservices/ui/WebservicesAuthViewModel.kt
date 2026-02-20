package com.matejdro.micropebble.webservices.ui

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.WebservicesAuthScreenKey
import com.matejdro.micropebble.webservices.api.ParsedWebservicesToken
import com.matejdro.micropebble.webservices.api.WebservicesClient
import com.matejdro.micropebble.webservices.api.WebservicesToken
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import kotlin.uuid.Uuid

class InvalidTokenException : CauseException(message = "Token is invalid", isProgrammersFault = false)

@Stable
@Inject
@ContributesScopedService
class WebservicesAuthViewModel(
   private val client: WebservicesClient,
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val sourceService: AppstoreSourceService,
) : SingleScreenViewModel<WebservicesAuthScreenKey>(resources.scope) {
   private val _currentAuthToken: MutableStateFlow<Outcome<Map<Uuid, WebservicesToken?>>> = MutableStateFlow(Outcome.Progress())
   val authToken = client.tokens
   val sources = sourceService.sources
   val authErrors = MutableStateFlow<CauseException?>(null)

   private val _startAuthToken = MutableStateFlow<Outcome<ParsedWebservicesToken?>>(Outcome.Progress())
   val startAuthToken: StateFlow<Outcome<ParsedWebservicesToken?>> = _startAuthToken

   fun authenticate(token: WebservicesToken) = resources.launchResourceControlTask(_currentAuthToken) {
      actionLogger.logAction { "WebservicesAuthViewModel.authenticate($token)" }
      if (!client.authenticate(token)) {
         authErrors.value = InvalidTokenException()
      }
   }

   suspend fun canAuthenticate(token: WebservicesToken): Boolean {
      actionLogger.logAction { "WebservicesAuthViewModel.canAuthenticate($token)" }
      return client.checkToken(token)
   }

   fun loadFromBootUrl() {
      actionLogger.logAction { "WebservicesAuthViewModel.loadFromBootUrl()" }
      val intentBootUri = key.bootUrl
      if (intentBootUri == null) {
         _startAuthToken.value = Outcome.Success(null)
         return
      }
      resources.launchResourceControlTask(_startAuthToken) {
         emit(Outcome.Success(client.parseTokenUri(intentBootUri)))
      }
   }
}
