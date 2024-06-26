
plugins {

    id("com.android.application") version "8.3.0"
    id("org.jetbrains.kotlin.android") version "1.8.10"
    id("com.ncorti.ktfmt.gradle") version "0.17.0"
    id("org.sonarqube") version "4.4.1.3373"

    // ensure correct Kotlin plugin
    id("jacoco")
    id("com.google.gms.google-services")

    // chats
    id ("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
}



java {
    // Set source and target compatibility
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


android {
    namespace = "com.android.PetPamper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.PetPamper"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    testCoverage {
        jacocoVersion = "0.8.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }

    compileOptions {
        //sourceCompatibility = JavaVersion.VERSION_1_8
        //targetCompatibility = JavaVersion.VERSION_1_8

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        //jvmTarget = "1.8"
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // Robolectric needs to be run only in debug. But its tests are placed in the shared source set (test)
    // The next lines transfers the src/test/* from shared to the testDebug one
    //
    // This prevent errors from occurring during unit tests
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")

        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }

    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }
}

sonar {
    properties {
        // property("sonar.gradle.skipCompile", "true")
        property("sonar.projectKey", "PetPamper_PetPamper")
        property("sonar.projectName", "PetPamper")
        property("sonar.organization", "petpamper")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.token", System.getenv("SONAR_TOKEN"))
        // Comma-separated paths to the various directories containing the *.xml JUnit report files. Each path may be absolute or relative to the project base directory.
        property("sonar.junit.reportPaths", "${project.layout.buildDirectory.get()}/test-results/testDebugunitTest/")
        // Paths to xml files with Android Lint issues. If the main flavor is changed, this file will have to be changed too.
        property("sonar.androidLint.reportPaths", "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml")
        // Paths to JaCoCo XML coverage report files.
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

// When a library is used both by robolectric and connected tests, use this function
fun DependencyHandlerScope.globalTestImplementation(dep: Any) {
    androidTestImplementation(dep)
    testImplementation(dep)
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(platform(libs.compose.bom))
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.test.junit)
    globalTestImplementation(libs.androidx.junit)
    globalTestImplementation(libs.androidx.espresso.core)

    // ------------- Jetpack Compose ------------------

    implementation(libs.androidx.ui)
    implementation("androidx.compose.ui:ui:1.6.5")
    implementation ("androidx.compose.material3:material3:1.2.1")
    implementation ("androidx.navigation:navigation-compose:2.7.0-rc01")
    implementation ("androidx.compose.material:material:1.3.0")

    implementation("io.getstream:stream-chat-android-compose:6.0.8")
    implementation("io.getstream:stream-chat-android-offline:6.0.8")

    implementation ("io.getstream:stream-chat-android-client:6.0.8")
    implementation("androidx.compose.material:material-icons-extended:1.6.0-alpha08")

    implementation("androidx.compose.material:material-icons-extended:1.6.0-alpha08")



    implementation("com.google.accompanist:accompanist-insets:0.24.1-alpha")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0"){
        exclude(module = "protobuf-lite")
    }
    androidTestImplementation ("androidx.test:runner:1.4.0")
    androidTestImplementation ("androidx.test:rules:1.4.0")

    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.maps.android:maps-compose-utils:4.3.0")


    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("io.coil-kt:coil-compose:1.4.0")

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    // Material Design 3
    // Integration with activities
    implementation(libs.compose.activity)
    // Integration with ViewModels
    implementation(libs.compose.viewmodel)
    // Android Studio Preview support
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    // UI Tests
    globalTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)

    // --------- Kaspresso test framework ----------
    globalTestImplementation(libs.kaspresso)
    globalTestImplementation(libs.kaspresso.compose)

    // ----------       Robolectric     ------------
    testImplementation(libs.robolectric)
    implementation(libs.google.services)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation (libs.firebase.ui.auth)

    implementation(libs.core.ktx)

    implementation (libs.play.services.auth)
    implementation (libs.accompanist.insets)
    implementation ("com.google.android.gms:play-services-auth:19.0.0")
    implementation ("com.google.accompanist:accompanist-flowlayout:0.23.1")


    implementation ("androidx.compose.material:material:1.6.5")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")


    // Use the latest version
    // slider
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation ("androidx.compose.ui:ui-util:1.6.5")


    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha13")
    // To use constraintlayout in compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha13")

    // chat
    implementation ("com.google.firebase:firebase-storage-ktx")

    implementation ("com.google.dagger:hilt-android:2.48")
    implementation ("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation ("com.google.firebase:firebase-firestore:24.6.0")
    kapt ("com.google.dagger:hilt-android-compiler:2.48")

    // implementation ("io.coil-kt:coil-compose:1.3.2")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    //notifications
    implementation ("androidx.core:core-ktx:1.6.0")
}

configurations {
    all {
        exclude(module = "protobuf-lite")
    }
}

tasks.withType<Test> {
    // Configure Jacoco for each tests
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}



tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )

    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.layout.projectDirectory}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}