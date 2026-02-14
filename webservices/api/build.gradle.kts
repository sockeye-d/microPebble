plugins {
   pureKotlinModule
}

dependencies {
   api(projects.common)
   api(libs.kotlin.coroutines)
   api(libs.kotlinova.core)

   implementation(libs.kotlin.serialization.core)
   implementation(projects.appstore.api)
}
