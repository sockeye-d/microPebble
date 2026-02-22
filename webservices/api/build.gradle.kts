plugins {
   pureKotlinModule
   alias(libs.plugins.serialization)
}

dependencies {
   api(libs.kotlin.coroutines)
   api(projects.appstore.api)

   implementation(libs.kotlin.serialization.core)

   compileOnly(libs.androidx.compose.runtime.annotation)
}
