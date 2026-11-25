import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.mpazpro"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.mpazpro"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val ruta = providers.environmentVariable("MPAZ_FIRMA_ARCHIVO").orNull
        val claveAlmacen = providers.environmentVariable("MPAZ_FIRMA_ALMACEN").orNull
        val alias = providers.environmentVariable("MPAZ_FIRMA_ALIAS").orNull
        val claveLlave = providers.environmentVariable("MPAZ_FIRMA_CLAVE").orNull
        val valoresFirma = listOf(ruta, claveAlmacen, alias, claveLlave)
        if (valoresFirma.all { !it.isNullOrBlank() }) {
            create("firmaExterna") {
                storeFile = file(requireNotNull(ruta))
                storePassword = claveAlmacen
                keyAlias = alias
                keyPassword = claveLlave
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "URL_SERVIDOR", "\"http://127.0.0.1:8080/\"")
        }
        release {
            val urlServidor = providers.gradleProperty("mpazServidorUrl")
                .orElse("https://servidor-no-configurado.invalid/").get()
            val uriServidor = URI(urlServidor)
            require(uriServidor.scheme == "https" && !uriServidor.host.isNullOrBlank() &&
                uriServidor.userInfo == null && uriServidor.query == null && uriServidor.fragment == null &&
                urlServidor.endsWith("/") && urlServidor.none { it == '"' || it == '\\' }) {
                "mpazServidorUrl debe ser una dirección HTTPS válida terminada en /, sin credenciales ni parámetros."
            }
            buildConfigField("String", "URL_SERVIDOR", "\"$urlServidor\"")
            signingConfig = signingConfigs.findByName("firmaExterna") ?: signingConfigs.getByName("debug")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.material3.navigation.suite)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
}

// GUARDAR UNA DESCRIPCION PARA FUTURAS MIGRACIONES
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
