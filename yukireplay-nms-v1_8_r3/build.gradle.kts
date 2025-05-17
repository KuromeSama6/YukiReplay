plugins {
    id("java")
    id("io.freefair.lombok") version "8.4"
}

group = project.parent?.group as String;
version = project.parent?.version as String;

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}

repositories {
    mavenLocal()
    maven("https://libraries.minecraft.net")
    mavenCentral()
}

dependencies {
    implementation(project(":yukireplay-api"))

    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}