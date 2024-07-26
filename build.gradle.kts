import io.papermc.paperweight.util.constants.PAPERCLIP_CONFIG

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("io.papermc.paperweight.patcher") version "1.5.0"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/") {
        content {
            onlyForConfigurations(PAPERCLIP_CONFIG)
        }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.6:fat")
    decompiler("net.minecraftforge:forgeflower:1.5.605.7")
    paperclip("io.papermc:paperclip:3.0.3")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    repositories.mavenCentral()

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

    publishing.repositories.maven("https://repo.racci.dev/snapshots") {
        name = "RacciRepo"
        credentials(PasswordCredentials::class)
    }
}

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        maven(paperMavenPublicUrl)
        maven("https://jitpack.io")
    }
}

repositories {
    maven(paperMavenPublicUrl) {
        content {
            onlyForConfigurations(configurations.paperclip.name)
        }
    }
}

paperweight {
    serverProject.set(project(":tentacles-server"))

    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)

    useStandardUpstream("purpur") {
        url.set(github("PurpurMC", "Purpur"))
        ref.set(providers.gradleProperty("purpurCommit"))

        withStandardPatcher {
            apiSourceDirPath.set("Purpur-API")
            serverSourceDirPath.set("Purpur-Server")

            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("Tentacles-API"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("Tentacles-Server"))
        }
    }
}

tasks.generateDevelopmentBundle {
    apiCoordinates.set("dev.racci.tentacles:tentacles-api")
    mojangApiCoordinates.set("io.papermc.paper:paper-mojangapi")
    libraryRepositories.set(
        listOf(
            "https://repo.maven.apache.org/maven2/",
            paperMavenPublicUrl,
            "https://repo.purpurmc.org/snapshots"
        )
    )
}

publishing {
    publications.create<MavenPublication>("devBundle") {
        artifact(tasks.generateDevelopmentBundle) {
            artifactId = "dev-bundle"
        }
    }
}
