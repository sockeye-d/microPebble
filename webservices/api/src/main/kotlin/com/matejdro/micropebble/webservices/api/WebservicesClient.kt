package com.matejdro.micropebble.webservices.api

import kotlinx.coroutines.flow.Flow

sealed class WebserviceResult<out T> {
   data class Success<T>(val data: T) : WebserviceResult<T>()
   data class Failure(val error: Throwable) : WebserviceResult<Nothing>()
   object NotAuthenticated : WebserviceResult<Nothing>()
}

interface WebservicesClient {
   val token: Flow<WebservicesToken?>
   suspend fun authenticate(token: WebservicesToken)
   suspend fun fetchLocker(): WebserviceResult<WebserviceLocker>
}
