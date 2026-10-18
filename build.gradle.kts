plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.modPublish)
}

val archivesBaseName = "stack-to-nearby-chests"
version = "0.5.20"
val minecraftVersionRangeStart = "26.3"
val minecraftVersionRangeEnd = "26.3"
val supportedJavaVersions = listOf(JavaVersion.VERSION_25, JavaVersion.VERSION_26)

base {
    archivesName = "${archivesBaseName}-mc${libs.versions.minecraft.get()}"
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/auserguide/declaring_repositories.html
    // for more information about repositories.

    maven {
        name = "AlexIIL"
        url = uri("https://staging.alexiil.uk/maven/")
    }
    maven {
        url = uri("https://maven.terraformersmc.com/releases/")
    }
    maven {
        url = uri("https://maven.shedaniel.me/")
    }
}

loom {
    runs["client"].run {
        runDirectory = file("run${libs.versions.minecraft.get()}")
        jvmArguments = listOf("-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=fatjar")
        programArguments = listOf("--width=1280", "--height=720")
    }
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.loader)
    implementation(libs.api)

    implementation(libs.libGui)
    include(libs.libGui)

    compileOnly(libs.modmenu)
    localRuntime(libs.modmenu)

//    compileOnly("dev.emi:emi-fabric:${emi_version}:api")
//    localRuntime("dev.emi:emi-fabric:${emi_version}")

    compileOnly(libs.cloth.basicMath)
    compileOnly(libs.rei.api)
    compileOnly(libs.rei.defaultPlugin)
//    localRuntime(libs.rei)
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    val projectName = project.name
    inputs.property("projectName", projectName)

    from("LICENSE") {
        rename { "${it}_$projectName" }
    }
}

publishMods {
    changelog.set(file("CHANGELOG.md").readText())
    type.set(STABLE)
    file.set(tasks.jar.get().archiveFile)
    modLoaders.add("fabric")
    displayName = "${version.get()} for Minecraft $minecraftVersionRangeStart" +
            if (minecraftVersionRangeStart != minecraftVersionRangeEnd)
                " - $minecraftVersionRangeEnd" else ""

    github {
        accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
        repository.set("xiaocihua/stack-to-nearby-chests")
        commitish.set("master")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")
        projectId = "HtGckJVc"
        environment = CLIENT_ONLY
        minecraftVersionRange {
            start = minecraftVersionRangeStart
            end = minecraftVersionRangeEnd
        }

        requires("fabric-api")
        optional("modmenu")
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = "693995"
        client = true
        server = false
        minecraftVersionRange {
            start = minecraftVersionRangeStart
            end = minecraftVersionRangeEnd
        }

        requires("fabric-api")
        optional("modmenu")
        incompatible("optifabric")
        embeds("libgui")
    }

    dryRun = providers.environmentVariable("GITHUB_TOKEN").getOrNull() == null
            || providers.environmentVariable("MODRINTH_API_KEY").getOrNull() == null
            || providers.environmentVariable("CURSEFORGE_API_KEY").getOrNull() == null
}
