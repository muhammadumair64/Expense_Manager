plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.iobits.budgetexpensemanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.budgetplanner.expensetracker.managermoney"
        minSdk = 23
        targetSdk = 34
        versionCode = 18
        versionName = "2.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Applovin Mediation
            resValue("string", "APP_LOVIN_MEDIUM_NATIVE", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_SMALL_NATIVE", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_INTERSTITIAL", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_BANNER", "YOUR_AD_UNIT_ID")

            //admob app id
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "ADMOB_BANNER_V2", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "ADMOB_OPEN_AD", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "ADMOB_INTERSTITIAL_V2", "ca-app-pub-3940256099942544/1033173712")
            resValue(
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "ADMOB_NATIVE_WITH_MEDIA_V2",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue("string", "ADMOB_REWARD_VIDEO", "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "ADMOB_REWARD_INTER", "ca-app-pub-3940256099942544/5354046379")
            resValue("string", "ADMOB_BANNER_COLLAPSIBLE", "ca-app-pub-3940256099942544/2014213617")
        }

        release {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Applovin Mediation
            resValue("string", "APP_LOVIN_MEDIUM_NATIVE", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_SMALL_NATIVE", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_INTERSTITIAL", "")
            resValue("string", "APP_LOVIN_BANNER", "YOUR_AD_UNIT_ID")

            //admob app id
            resValue("string", "admob_app_id", "ca-app-pub-8481475782807886~7748826951")

            resValue("string", "ADMOB_BANNER_V2", "ca-app-pub-8481475782807886/3821707152")
            resValue("string", "ADMOB_OPEN_AD", "ca-app-pub-8481475782807886/3809581948")
            resValue("string", "ADMOB_INTERSTITIAL_V2", "ca-app-pub-8481475782807886/6787099072")
            resValue("string", "ADMOB_NATIVE_WITHOUT_MEDIA_V2", "ca-app-pub-8481475782807886/6582456963")
            resValue("string", "ADMOB_NATIVE_WITH_MEDIA_V2", "")
            resValue("string", "ADMOB_REWARD_VIDEO", "")
            resValue("string", "ADMOB_REWARD_INTER", "")
            resValue("string", "ADMOB_BANNER_COLLAPSIBLE", "")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    kapt {
        generateStubs = true
        correctErrorTypes = true
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.junit.jupiter)

    //    Firebase
    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))
    implementation(libs.play.services.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)

    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-perf")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // for minify issue
    implementation(libs.infer.annotation)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //shimmer effect
    implementation(libs.shimmer)

    //shimmer effect
    implementation(libs.shimmer)

    //Hilt Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.android.compiler)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Lifecycles only (without ViewModel or LiveData)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    kapt(libs.androidx.hilt.compiler)

    // viewModelScope:
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.sdp.android)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.navigation.fragment)

    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation(libs.androidx.lifecycle.common.java8)

    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Kotlin
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Feature module Support
   // implementation(libs.androidx.navigation.dynamic.features.fragment)

    implementation(libs.androidx.fragment.ktx)
    // Feature module Support
  //  implementation(libs.androidx.navigation.dynamic.features.fragment)
    // Testing Navigation
    androidTestImplementation(libs.androidx.navigation.testing)

    // Jetpack Compose Integration
    implementation(libs.androidx.navigation.compose)

    //Glide
    implementation(libs.glide)

    implementation(libs.androidx.recyclerview)

    // BILLING LIBRARY
    implementation(libs.billing)

    // Applovin
//    implementation(libs.applovin.sdk)
    implementation(libs.google.adapter)
    implementation(libs.facebook.adapter)

    implementation(libs.play.services.ads.identifier)
    implementation(libs.play.services.base)
    implementation(libs.picasso)

    // admob ads
    implementation(libs.play.services.ads)
    // ad consent
    implementation(libs.user.messaging.platform)

    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    annotationProcessor(libs.androidx.lifecycle.compiler)

    implementation(libs.lottie)

    // google common library
    implementation(libs.listenablefuture)
    implementation(libs.guava)
    implementation(libs.drag.drop.swipe.recyclerview)
    //GSON of Retrofit
    implementation(libs.converter.gson)

    // ViewModel ,Room and LiveData
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    kapt (libs.androidx.room.compiler)
    // kapt ("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")

    // Math calculation
    implementation ("org.mariuszgromada.math:MathParser.org-mXparser:4.4.2")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("com.github.RupinSahu:CardDrawer:-SNAPSHOT")

    //implementation ("com.github.nguyenhoanglam:ImagePicker:1.6.3")

    // for bidding
    implementation  ("com.google.ads.mediation:applovin:13.0.0.1")
    implementation  ("com.google.ads.mediation:facebook:6.18.0.0")
    implementation  ("com.google.ads.mediation:mintegral:16.8.61.0")
    implementation ("com.google.ads.mediation:vungle:7.4.1.0")
    implementation ("com.vungle:vungle-ads:7.4.1")

    implementation ("com.github.ome450901:SimpleRatingBar:1.5.1")
}
