plugins {
    id("io.freefair.lombok") version "9.2.0" // https://plugins.gradle.org/plugin/io.freefair.lombok
    `java-library`
    `maven-publish`
}

description = "World1-6Elevators"
group = "com.andrew121410.mc"
version = "1.0-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_25
java.targetCompatibility = JavaVersion.VERSION_25

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    jar {
        archiveFileName.set("World1-6Elevators.jar")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://repo.opencollab.dev/maven-releases/")
    }

    maven {
        url = uri("https://repo.opencollab.dev/maven-snapshots/")
    }

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.World1-6.World1-6Utils:World1-6Utils-Plugin:cbfa5ab713")
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}