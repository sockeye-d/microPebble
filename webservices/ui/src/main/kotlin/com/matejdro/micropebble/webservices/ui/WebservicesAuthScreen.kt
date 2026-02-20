package com.matejdro.micropebble.webservices.ui

import android.R.attr.name
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.navigation.keys.WebservicesAuthScreenKey
import com.matejdro.micropebble.ui.components.BasicExposedDropdownMenuBox
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.webservices.api.ParsedWebservicesToken
import com.matejdro.micropebble.webservices.api.WebservicesToken
import com.matejdro.micropebble.webservices.ui.common.NotAuthorizedDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.transformLatest
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid
import com.matejdro.micropebble.sharedresources.R as sharedR

@Stable
@InjectNavigationScreen
@ContributesScreenBinding
class WebservicesAuthScreen(
   private val viewModel: WebservicesAuthViewModel,
) : Screen<WebservicesAuthScreenKey>() {
   @Composable
   override fun Content(key: WebservicesAuthScreenKey) {
      val tokens = viewModel.authToken.collectAsStateWithLifecycle(emptyMap()).value
      val initialToken = viewModel.startAuthToken.collectAsStateWithLifecycleAndBlinkingPrevention().value ?: Outcome.Progress()

      LaunchedEffect(key.bootUuid) {
         viewModel.loadFromBootUrl()
      }

      WebservicesAuthScreenContent(
         tokens,
         initialToken,
         sources = viewModel.sources.collectAsStateWithLifecycle(null).value,
         authenticate = { viewModel.authenticate(it) },
         checkToken = { viewModel.canAuthenticate(it) }
      )
   }
}

@Composable
private fun WebservicesAuthScreenContent(
   tokens: Map<Uuid, WebservicesToken>,
   initialToken: Outcome<ParsedWebservicesToken?>,
   sources: List<AppstoreSource>?,
   authenticate: (WebservicesToken) -> Unit,
   checkToken: suspend (WebservicesToken) -> Boolean,
) {
   var setupDialogData: ParsedWebservicesToken? by remember { mutableStateOf(null) }
   var setupDialogShown by remember { mutableStateOf(false) }
   ProgressErrorSuccessScaffold(initialToken) { initialTokenData ->
      LaunchedEffect(initialTokenData) {
         if (initialTokenData != null) {
            setupDialogShown = true
            setupDialogData = initialTokenData
         }
      }

      if (tokens.isEmpty()) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NotAuthorizedDisplay {
               Row(Modifier.align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  val uriHandler = LocalUriHandler.current
                  Button(onClick = { uriHandler.openUri("https://boot.rebble.io") }) {
                     Text(stringResource(R.string.authorize_rebble))
                  }
                  Button(onClick = { setupDialogShown = true }) {
                     Text(stringResource(R.string.setup_dialog))
                  }
               }
            }
         }
      }

      if (setupDialogShown && sources != null) {
         ManualSetupDialog(
            setupDialogData,
            sources,
            onDismissed = { setupDialogShown = false },
            onSubmitted = {
               setupDialogShown = false
               authenticate(it)
            },
            {
               checkToken(it)
            },
         )
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ManualSetupDialog(
   initialValues: ParsedWebservicesToken?,
   sources: List<AppstoreSource>,
   onDismissed: () -> Unit,
   onSubmitted: (WebservicesToken) -> Unit,
   checkToken: suspend (WebservicesToken) -> Boolean,
   modifier: Modifier = Modifier,
) {
   var appstoreSource by remember { mutableStateOf(initialValues?.sourceId?.let { source -> sources.find { it.id == source } }) }
   var bootUrl by remember { mutableStateOf(initialValues?.bootUrl ?: "") }
   var apiToken by remember {
      mutableStateOf(
         initialValues?.token ?: ""
      )
   }
   val token by remember {
      snapshotFlow { Triple(appstoreSource, bootUrl, apiToken) }.transformLatest { (source, bootUrl, apiToken) ->
         emit(Outcome.Progress())
         if (source != null) {
            delay(100.milliseconds)
            emit(Outcome.Success(WebservicesToken(source.id, bootUrl, apiToken).takeIf { checkToken(it) }))
         } else {
            emit(Outcome.Success(null))
         }
      }
   }.collectAsStateWithLifecycle(Outcome.Progress())
   AlertDialog(
      onDismissRequest = onDismissed,
      title = {
         Text(stringResource(R.string.setup_dialog, name))
      },
      confirmButton = {
         if (token is Outcome.Progress) {
            CircularProgressIndicator()
         } else {
            TextButton(
               onClick = { token.data?.let { onSubmitted(it) } },
               enabled = token.data != null,
            ) {
               Text(stringResource(sharedR.string.ok))
            }
         }
      },
      dismissButton = {
         TextButton(onClick = onDismissed) {
            Text(stringResource(sharedR.string.cancel))
         }
      },
      text = {
         Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicExposedDropdownMenuBox(
               textFieldValue = appstoreSource?.name ?: "",
               modifier = Modifier.fillMaxWidth(), // textFieldLeadingIcon = {
               //    Icon(
               //       painterResource(com.matejdro.micropebble.appstore.ui.R.drawable.ic_appstore_source),
               //       contentDescription = null
               //    )
               // },
               textFieldLabel = { Text(stringResource(R.string.appstore_source)) },
            ) {
               for (source in sources.filter { it.enabled }) {
                  DropdownMenuItem(
                     text = { Text(source.name) },
                     onClick = { appstoreSource = source },
                     contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                  )
               }
            }
            OutlinedTextField(
               bootUrl,
               onValueChange = { bootUrl = it },
               label = { Text(stringResource(R.string.api_boot_url)) },
               modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
               apiToken,
               onValueChange = { apiToken = it },
               label = { Text(stringResource(R.string.api_token)) },
               modifier = Modifier.fillMaxWidth()
            )
         }
      },
      modifier = modifier,
   )
}
