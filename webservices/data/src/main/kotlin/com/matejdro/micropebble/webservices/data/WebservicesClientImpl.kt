package com.matejdro.micropebble.webservices.data

import androidx.datastore.core.DataStore
import com.matejdro.micropebble.webservices.api.WebservicesToken
import com.matejdro.micropebble.webservices.api.WebserviceResult
import com.matejdro.micropebble.webservices.api.WebserviceResult.NotAuthenticated
import com.matejdro.micropebble.webservices.api.WebservicesClient
import com.matejdro.micropebble.webservices.api.WebserviceLocker
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@ContributesBinding(AppScope::class)
@Inject
class WebservicesClientImpl(
   private val appInstallSourcesStore: DataStore<WebservicesToken?>,
) : WebservicesClient {
   val json: Json = Json {
      isLenient = true
      ignoreUnknownKeys = true
   }

   private var client: HttpClient? = null

   @Suppress("InjectDispatcher") // withIO doesn't work because the IO dispatcher isn't injected somehow
   private suspend fun getHttp() = client ?: withContext(Dispatchers.IO) {
      HttpClient {
         install(ContentNegotiation) {
            json(json)
         }
      }
   }.also { client = it }

   override val token
      get() = appInstallSourcesStore.data

   override suspend fun authenticate(token: WebservicesToken) {
      appInstallSourcesStore.updateData { token }
   }

   override suspend fun fetchLocker(): WebserviceResult<WebserviceLocker> {
      val token = token.first() ?: return NotAuthenticated
      val client = getHttp()
      return try {
         WebserviceResult.Success(
            client.get("${token.apiUrl}/api/v1/locker") { headers["Authorization"] = "Bearer ${token.token}" }.body(),
         )
      } catch (e: SerializationException) {
         WebserviceResult.Failure(e)
      }
   }
}
