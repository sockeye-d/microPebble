package com.matejdro.micropebble.webservices.data

import android.net.Uri
import androidx.datastore.core.DataStore
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.webservices.api.ParsedWebservicesToken
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
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@Serializable
data class CobbleApi(
   val appstore: Appstore,
   val auth: Auth,
   val webviews: Webviews,
) {
   @Serializable
   data class Appstore(
      val base: String,
   )

   @Serializable
   data class Auth(
      @SerialName("authorize_url")
      val authorizeUrl: String,
      val base: String,
      @SerialName("client_id")
      val clientId: String,
      @SerialName("refresh_url")
      val refreshUrl: String,
   )

   @Serializable
   data class Webviews(
      val appstoreApplication: String,
      val appstoreWatchapps: String,
      val appstoreWatchfaces: String,
      val manageAccount: String,
   )
}

@ContributesBinding(AppScope::class)
@Inject
class WebservicesClientImpl(
   private val appInstallSourcesStore: DataStore<Map<Uuid, WebservicesToken>>,
   private val sources: AppstoreSourceService,
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

   override val tokens
      get() = appInstallSourcesStore.data

   override suspend fun checkToken(token: WebservicesToken) = token.httpGet(token.lockerUrl()).status == HttpStatusCode.OK

   override suspend fun authenticate(token: WebservicesToken): Boolean {
      if (!checkToken(token)) {
         return false
      }
      appInstallSourcesStore.updateData { it + (token.sourceId to token) }
      return true
   }

   override suspend fun deauthenticate(token: WebservicesToken) {
      appInstallSourcesStore.updateData { it - token.sourceId }
   }

   override suspend fun fetchLocker(sourceId: Uuid): WebserviceResult<WebserviceLocker> {
      val token = tokens.first()[sourceId] ?: return NotAuthenticated
      return try {
         WebserviceResult.Success(
            token.httpGet(token.lockerUrl()).body(),
         )
      } catch (e: SerializationException) {
         WebserviceResult.Failure(e)
      }
   }

   override suspend fun parseTokenUri(uri: String): ParsedWebservicesToken {
      val url = Uri.parse(uri)!!
      val bootUrl = "${url.scheme!!}://${url.authority!!}"
      val (appstore, _, _) = getHttp().get("$bootUrl/api/cobble").body<CobbleApi>()
      return ParsedWebservicesToken(
         sourceId = sources.find { it.url == appstore.base }?.id, token = url.getQueryParameter("access_token"), bootUrl = bootUrl
      )
   }

   private suspend inline fun WebservicesToken.httpGet(endpoint: String, block: HttpRequestBuilder.() -> Unit = {}) =
      getHttp().get(endpoint) {
         headers["Authorization"] = "Bearer $token"
         block()
      }

   private suspend fun WebservicesToken.lockerUrl(): String = "${source()}/v1/locker"
   private suspend fun WebservicesToken.source(): String = sources.find(sourceId)!!.url
}
