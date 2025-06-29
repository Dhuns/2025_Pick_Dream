import java.util.Properties

fun getEnvValue(key: String): String {
    val props = Properties()
    val envFile = rootProject.file("env")
    if (envFile.exists()) {
        envFile.inputStream().use { props.load(it) }
    }
    return props.getProperty(key) ?: ""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("androidx.navigation.safeargs.kotlin") version "2.7.7"
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.pick_dream"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pick_dream"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders.put("GOOGLE_MAPS_API_KEY", getEnvValue("GOOGLE_MAPS_API_KEY"))
        manifestPlaceholders.put("OPENAI_API_KEY", getEnvValue("OPENAI_API_KEY"))
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Firebase BoM(Bill of Materials) 추가
    // BoM을 사용하면 Firebase 라이브러리의 버전을 명시하지 않아도 호환되는 버전이 자동으로 설정됩니다.
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.activity)
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation ("androidx.viewpager2:viewpager2:1.1.0")
    implementation ("com.prolificinteractive:material-calendarview:1.4.3")

    // BoM을 사용하므로 아래 Firebase 라이브러리들은 버전을 명시하지 않습니다.
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    implementation("com.squareup.picasso:picasso:2.71828")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}
