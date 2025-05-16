plugins {
    `java-library`
}

group = "moe.ku6"
version = "0.0.1"
description = "Parent project for YukiReplay"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18)) // Adjust if needed
    }
}

tasks.named("build") {
    finalizedBy(":yukireplay-core:build")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
