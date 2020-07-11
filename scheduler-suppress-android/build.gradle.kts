plugins {
    id("com.android.library")
    id("maven")
    id("com.github.dcendents.android-maven")
}

group = "com.huya.rxjava2"

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(29)

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
    android.defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    androidTestImplementation("androidx.test:core:1.2.0")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("junit:junit:4.12")
    androidTestImplementation("org.hamcrest:hamcrest-library:1.3")
    androidTestImplementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    androidTestImplementation("io.reactivex.rxjava2:rxjava:2.2.19")
}

tasks {
    val sourcesJar = create<Jar>("sourcesJar") {
        dependsOn("classes")
        classifier = "sources"
        from(android.sourceSets.named("main").get().java.sourceFiles)
    }

    artifacts {
        archives(sourcesJar)
    }
}