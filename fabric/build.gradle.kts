import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom-no-remap")
    kotlin("jvm")
}

val modId: String by project
val modName: String by project
val modVersion: String by project
val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricLanguageKotlinVersion: String by project
val architecturyApiVersion: String by project
val smartBrainLibVersion: String by project
val geckoLibVersion: String by project
val yaclVersion: String by project

sourceSets {
    named("main") {
        java.srcDir(rootProject.file("src/main/java"))
        java.include("galacticwars/clonewars/client/gui/GalacticWarsConfigScreen.java")
        kotlin.srcDir(rootProject.file("src/main/java"))
        kotlin.include(
            "**/*.kt",
            "galacticwars/clonewars/client/gui/GalacticWarsConfigScreen.java",
        )
    }
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(rootProject.file("src/main/resources/galacticwars.accesswidener"))
    mods {
        maybeCreate("main").apply {
            sourceSet("main")
            sourceSet(
                "main",
                project.dependencies.project(mapOf("path" to ":common")) as ProjectDependency,
            )
        }
    }
}

val commonCode by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val commonBundle by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations.named("compileClasspath") { extendsFrom(commonCode) }
configurations.named("developmentFabric") { extendsFrom(commonCode) }

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlinVersion")
    implementation("dev.architectury:architectury-fabric:$architecturyApiVersion")
    implementation("net.tslat:smartbrainlib-fabric-26.2:$smartBrainLibVersion")
    implementation("com.geckolib:geckolib-fabric-26.2:$geckoLibVersion")
    implementation("dev.isxander:yet-another-config-lib:$yaclVersion-fabric")

    add(commonCode.name, project(path = ":common", configuration = "apiElements")) {
        isTransitive = false
    }
    add(commonBundle.name, project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }
}

tasks.named<ProcessResources>("processResources") {
    val replacements = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to modVersion,
        "minecraft_version" to minecraftVersion,
        "fabric_loader_version" to fabricLoaderVersion,
        "fabric_api_version" to fabricApiVersion,
        "fabric_language_kotlin_version" to fabricLanguageKotlinVersion,
        "architectury_api_version" to architecturyApiVersion,
        "geckolib_version" to geckoLibVersion,
        "smartbrainlib_version" to smartBrainLibVersion,
        "yacl_version" to yaclVersion,
    )
    inputs.properties(replacements)
    filesMatching("fabric.mod.json") { expand(replacements) }
}

tasks.named<Jar>("jar") {
    dependsOn(project(":common").tasks.named("transformProductionFabric"))
    archiveBaseName.set("galacticwars-fabric")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({ commonBundle.map { if (it.isDirectory) it else zipTree(it) } })
    exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
}

// Loom adds the common development JAR to the launch classpath as well as the
// common class directories. Keep that JAR current so iterative runs cannot
// execute stale common bytecode ahead of freshly compiled classes.
tasks.matching { it.name == "runClient" || it.name == "runServer" }.configureEach {
    dependsOn(project(":common").tasks.named("jar"))
}
