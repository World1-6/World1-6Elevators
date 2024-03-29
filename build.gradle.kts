plugins {
    id("io.freefair.lombok") version "8.4"
    `java-library`
    `maven-publish`
}

description = "World1-6Elevators"
group = "com.andrew121410.mc"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        archiveFileName.set("World1-6Elevators.jar")
    }
}

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
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
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.github.World1-6.World1-6Utils:World1-6Utils-Plugin:a1c6ad2df7")
    compileOnly("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}