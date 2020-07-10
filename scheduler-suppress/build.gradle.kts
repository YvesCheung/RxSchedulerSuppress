plugins {
    id("java")
}

dependencies {
    compileOnly("io.reactivex.rxjava3:rxjava:3.0.4")
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-library:1.3")
    testImplementation("io.reactivex.rxjava3:rxjava:3.0.4")
}