package com.matejdro.micropebble.webservices.api

data class WebservicesToken(
   val apiUrl: String = "https://appstore-api.rebble.io/",
   val token: String,
)
