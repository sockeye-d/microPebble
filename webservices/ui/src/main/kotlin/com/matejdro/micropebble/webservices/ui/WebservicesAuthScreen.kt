package com.matejdro.micropebble.webservices.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matejdro.micropebble.navigation.keys.WebservicesAuthScreenKey
import com.matejdro.micropebble.webservices.ui.common.NotAuthorizedDisplay
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@Stable
@InjectNavigationScreen
@ContributesScreenBinding
class WebservicesAuthScreen(
   private val viewModel: WebservicesAuthViewModel,
) : Screen<WebservicesAuthScreenKey>() {
   @Composable
   override fun Content(key: WebservicesAuthScreenKey) {
      val token = viewModel.authToken.collectAsStateWithLifecycle(null).value
      if (token == null) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NotAuthorizedDisplay {
               Row(Modifier.align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(onClick = {}) {
                     Text(stringResource(R.string.authorize_rebble))
                  }
                  Button(onClick = {}) {
                     Text(stringResource(R.string.manual_setup))
                  }
               }
            }
         }
         return
      }
   }
}
