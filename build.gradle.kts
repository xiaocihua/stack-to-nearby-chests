plugins {
    alias(libs.plugins.loom)
}

val archivesBaseName = "stack-to-nearby-chests"

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
