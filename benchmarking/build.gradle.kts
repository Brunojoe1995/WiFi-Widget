import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.baselineprofile)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

val mvdName = "Pixel 6 API 33"

android {
    namespace = "com.w2sv.benchmarking"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 28
        targetSdk = libs.versions.compileSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions.managedDevices.devices {
        @Suppress("UnstableApiUsage")
        create<ManagedVirtualDevice>(mvdName) {
            device = "Pixel 6"
            apiLevel = 33
            systemImageSource = "aosp"
        }
    }

    targetProjectPath = ":app"
}

// Baseline profile configuration: https://developer.android.com/topic/performance/baselineprofiles/configure-baselineprofiles
baselineProfile {
    @Suppress("UnstableApiUsage")
    enableEmulatorDisplay = false
    useConnectedDevices = false
    managedDevices += mvdName
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.runner)
}