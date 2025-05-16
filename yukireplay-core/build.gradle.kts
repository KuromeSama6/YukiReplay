plugins {
    id("java")
    id("io.freefair.lombok") version "8.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = parent!!.group.toString()
version = parent!!.version.toString()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}

repositories {
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")

    compileOnly("joda-time:joda-time:2.12.5")
    compileOnly("org.jcommander:jcommander:2.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("org.apache.ant:ant:1.10.15")

    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")

}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named("build") {
    finalizedBy("publishToMavenLocal")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }
}
