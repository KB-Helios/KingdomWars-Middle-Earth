plugins {
    id("architectury-plugin")
    id("dev.architectury.loom-no-remap")
    kotlin("jvm")
}

val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val architecturyApiVersion: String by project
val smartBrainLibVersion: String by project
val geckoLibVersion: String by project
val coroutinesVersion: String by project

architectury {
    common("fabric", "neoforge")
}

loom {
    accessWidenerPath.set(rootProject.file("src/main/resources/galacticwars.accesswidener"))
}

val neoForgeOnlySources = listOf(
    "galacticwars/clonewars/client/gui/GalacticWarsConfigScreen.java",
    "galacticwars/clonewars/client/render/BlasterClientExtensions.java",
    "galacticwars/clonewars/client/render/LightsaberClientExtensions.java",
    "galacticwars/clonewars/gametest/**",
)

sourceSets {
    named("main") {
        java.setSrcDirs(listOf(rootProject.file("src/main/java")))
        // Kotlin 2.4 no longer discovers Java roots relocated outside the module
        // automatically. Supplying the same tree gives kotlinc Java symbols for
        // mixed-source analysis; javac remains the only compiler that emits them.
        kotlin.setSrcDirs(listOf(
            rootProject.file("src/main/kotlin"),
            rootProject.file("src/main/java"),
        ))
        resources.setSrcDirs(listOf(
            rootProject.file("src/main/resources"),
            rootProject.file("src/generated/resources"),
        ))
        java.exclude(neoForgeOnlySources)
        kotlin.exclude(neoForgeOnlySources)
        resources.exclude(
            "**/*.bbmodel",
            "**/.cache/**",
            "data/galacticwars/neoforge/**",
            "META-INF/neoforge.mods.toml",
            "fabric.mod.json",
        )
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")

    compileOnly("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    api("dev.architectury:architectury:$architecturyApiVersion")
    compileOnly("net.tslat:smartbrainlib-common-26.2:$smartBrainLibVersion")
    compileOnly("com.geckolib:geckolib-common-26.2:$geckoLibVersion")
    compileOnly(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")
}

// Architectury 3.5 does not declare the production transforms' input-JAR
// dependency under Gradle 9.5. Without this edge, a clean parallel build can
// transform a not-yet-created archive and publish a 166-byte empty artifact.
tasks.matching { it.name.startsWith("transformProduction") }.configureEach {
    dependsOn(tasks.named("jar"))
}
