
plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.saif.mywhatsapp"
    compileSdk = 34

    buildFeatures {
        viewBinding=true
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/license.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
        resources.excludes.add("META-INF/notice.txt")
    }


//    buildscript {
//        repositories {
//            google()
//            mavenCentral()
//        }
//        dependencies {
//            classpath("com.google.gms:google-services:4.4.2") // Update version if needed
//        }
//    }

    defaultConfig {
        applicationId = "com.saif.mywhatsapp"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime)
    implementation(libs.lifecycle.process)
    implementation(libs.firebase.messaging)
    implementation(libs.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.otpview)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(platform(libs.firebase.bom))
    implementation (libs.glide)
    implementation (libs.circleimageview)
    implementation (libs.android.reactions)
    implementation(libs.reactbutton)
    implementation (libs.circularstatusview)
    implementation (libs.storyview)
    implementation (libs.shimmer)
    implementation(libs.google.firebase.auth)
    implementation(libs.okhttp)
    implementation(libs.android.image.cropper)
    implementation (libs.ucrop)
    implementation (libs.recordview)
    implementation (libs.java.jwt)
    implementation (libs.audiowave.progressbar)
    implementation(libs.amplituda)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation ("com.github.chrisbanes:PhotoView:2.0.0"){
        exclude (group="com.android.support")
        exclude (group="androidx.versionedparcelable")
    }

//    implementation("com.github.JagarYousef:ChatVoicePlayer:1.1.0") {
//        exclude(group = "com.android.support")
//        exclude(group = "androidx.versionedparcelable")
//    }

    implementation (libs.google.auth.library.oauth2.http)
    annotationProcessor(libs.room.compiler)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
//    implementation("io.github.jan-tennert.supabase:bom:3.0.3")
//    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.3")
//    implementation("io.ktor:ktor-client-android:3.0.3")
//    implementation(libs.supabase.kt)


//    implementation ("com.github.sharish:ShimmerRecyclerView:v1.3")
}