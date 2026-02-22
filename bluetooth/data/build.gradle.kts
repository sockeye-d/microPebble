plugins {
   androidLibraryModule
   di
}

android {
   namespace = "com.matejdro.micropebble.bluetooth"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.bluetooth.api)
   api(projects.commonAndroid)
   api(libs.dispatch)
   implementation(projects.sharedResources)
   implementation(libs.androidx.core)
   implementation(libs.libpebble3)
   implementation(libs.kotlin.coroutines)
}
