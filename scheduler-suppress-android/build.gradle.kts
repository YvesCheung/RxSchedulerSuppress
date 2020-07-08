plugins {
    id("com.android.library")
}

android {
    compileSdkVersion(28)
    buildToolsVersion("28.0.3")

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(28)

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        multiDexEnabled = true
    }
}

dependencies {
    api(project(":scheduler-suppress"))

    compileOnly("io.reactivex.rxjava2:rxandroid:2.1.1")
    compileOnly("io.reactivex.rxjava2:rxjava:2.2.19")
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-library:1.3")
    testImplementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    testImplementation("io.reactivex.rxjava2:rxjava:2.2.19")
}