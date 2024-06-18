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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.github.mukeshsolanki.android-otpview-pinview:otpview:3.1.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.pgreze:android-reactions:1.6")
    implementation("io.github.amrdeveloper:reactbutton:2.1.0")
    implementation ("com.github.3llomi:CircularStatusView:V1.0.3")
    implementation ("com.github.OMARIHAMZA:StoryView:1.0.2-alpha")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.firebase:firebase-auth")
    annotationProcessor(libs.room.compiler)
//    implementation ("com.github.sharish:ShimmerRecyclerView:v1.3")
}