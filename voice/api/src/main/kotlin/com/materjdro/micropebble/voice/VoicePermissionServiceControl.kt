package com.materjdro.micropebble.voice

import kotlinx.coroutines.flow.Flow

interface VoicePermissionServiceControl {
   val voiceServiceActive: Flow<Boolean>
   fun start()
}
