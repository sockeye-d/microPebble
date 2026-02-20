package com.matejdro.micropebble.webservices.api

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Stable
@Serializable
data class ParsedWebservicesToken(
   val sourceId: Uuid? = null,
   val bootUrl: String? = null,
   val token: String? = null,
)

sealed class WebserviceResult<out T> {
   data class Success<T>(val data: T) : WebserviceResult<T>()
   data class Failure(val error: Throwable) : WebserviceResult<Nothing>()
   object NotAuthenticated : WebserviceResult<Nothing>()
}

interface WebservicesClient {
   val tokens: Flow<Map<Uuid, WebservicesToken>>
   suspend fun checkToken(token: WebservicesToken): Boolean
   suspend fun authenticate(token: WebservicesToken): Boolean
   suspend fun deauthenticate(token: WebservicesToken)
   suspend fun parseTokenUri(uri: String?): ParsedWebservicesToken
   suspend fun fetchLocker(sourceId: Uuid): WebserviceResult<WebserviceLocker>
}
