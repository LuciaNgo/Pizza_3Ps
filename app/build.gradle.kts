plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.pizza3ps"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pizza3ps"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(enforcedPlatform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-identity:18.0.1")
    implementation("com.facebook.android:facebook-android-sdk:latest.release")
    implementation("com.facebook.android:facebook-login:latest.release")
//    implementation(libs.firebase.auth)
//    implementation(libs.firebase.firestore)
//    implementation(libs.play.services.auth)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.counterfab)
    implementation(libs.glide)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.play.services.location)
    annotationProcessor(libs.glide.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.cardview)
    implementation(libs.material.v1100)
    implementation(libs.androidx.viewpager2)
    implementation(libs.konfetti)
    implementation(libs.lottie)
    implementation(libs.stepview)
    implementation(libs.firebase.messaging)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.mpandroidchart)
    implementation("com.github.momo-wallet:mobile-sdk:1.0.7") {
        exclude(group = "com.android.support")
    }
    //implementation ("com.braintreepayments.api:drop-in:6.16.0")
//    implementation ("com.paypal.android:card-payments:CURRENT-VERSION-SNAPSHOT")
//    implementation ("com.paypal.sdk:paypal-android-sdk:2.16.0")
    implementation ("com.braintreepayments.api:drop-in:6.16.0")
    implementation ("com.braintreepayments.api:paypal:5.8.0")


}