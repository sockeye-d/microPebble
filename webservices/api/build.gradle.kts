plugins {
   pureKotlinModule
   alias(libs.plugins.serialization)
}

dependencies {
   api(projects.common)
   api(libs.kotlin.coroutines)
   api(libs.kotlinova.core)

   implementation(libs.kotlin.serialization.core)
   implementation(projects.appstore.api)

   compileOnly(libs.androidx.compose.runtime.annotation)
}
