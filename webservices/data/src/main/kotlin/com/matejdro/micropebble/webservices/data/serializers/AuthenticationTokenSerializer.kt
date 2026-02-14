package com.matejdro.micropebble.webservices.data.serializers

import android.util.Log
import androidx.datastore.core.Serializer
import com.matejdro.micropebble.webservices.api.WebservicesToken
import dispatch.core.withIO
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object AuthenticationTokenSerializer : Serializer<WebservicesToken?> {
   private val json = Json { ignoreUnknownKeys = true }
   override val defaultValue = null as WebservicesToken?

   override suspend fun readFrom(input: InputStream) =
      try {
         json.decodeFromString<WebservicesToken?>(input.readBytes().decodeToString())
      } catch (e: SerializationException) {
         Log.e("AppstoreSourcesSerializer.readFrom", "Failed to load appstore sources", e)
         defaultValue
      }

   override suspend fun writeTo(t: WebservicesToken?, output: OutputStream) = withIO {
      output.write(json.encodeToString(t).encodeToByteArray())
   }
}
