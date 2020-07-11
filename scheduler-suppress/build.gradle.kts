plugins {
    id("java")
    id("maven")
}

group = "com.huya.rxjava2"

dependencies {
    compileOnly("io.reactivex.rxjava2:rxjava:2.2.19")
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-library:1.3")
    testImplementation("io.reactivex.rxjava2:rxjava:2.2.19")
}

tasks {
    val sourcesJar = create<Jar>("sourcesJar") {
        dependsOn("classes")
        classifier = "sources"
        from(sourceSets.named("main").get().allSource)
    }

    val javaDocJar = create<Jar>("javadocJar") {
        dependsOn("javadoc")
        classifier = "javadoc"
        from(javadoc.get().destinationDir)
    }

    artifacts {
        archives(sourcesJar)
        archives(javaDocJar)
    }
}
