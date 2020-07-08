plugins {
    id("java")
}

dependencies {
    compileOnly("io.reactivex.rxjava2:rxjava:2.2.19")
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-library:1.3")
    testImplementation("io.reactivex.rxjava2:rxjava:2.2.19")
}