plugins {
   androidLibraryModule
   compose
   di
   navigation
   parcelize
   showkase
}

android {
    namespace = "com.matejdro.micropebble.webservices.ui"

    buildFeatures {
        androidResources = true
    }
}

dependencies {
    api(projects.webservices.api)
   api(projects.common)
   api(libs.kotlinova.navigation)
   api(libs.kotlin.coroutines)

   implementation(projects.commonCompose)
   implementation(projects.commonNavigation)
   implementation(projects.sharedResources)

    testImplementation(projects.common.test)
}
