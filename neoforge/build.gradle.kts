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
val modLicense: String by project
val modVersion: String by project
val minecraftVersion: String by project
val neoForgeVersion: String by project
val kotlinForForgeVersion: String by project
val architecturyApiVersion: String by project
val smartBrainLibVersion: String by project
val geckoLibVersion: String by project
val yaclVersion: String by project

architectury {
    platformSetupLoomIde()
    neoForge()
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
    runs {
        named("client") {
            systemProperties.put("neoforge.enabledGameTestNamespaces", modId)
        }
        named("server") {
            programArguments.add("--nogui")
            systemProperties.put("neoforge.enabledGameTestNamespaces", modId)
        }
        create("gameTestServer") {
            server()
            forgeTemplate.set("gameTestServer")
            systemProperties.put("neoforge.enabledGameTestNamespaces", modId)
        }
    }
}

val commonCode by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val commonRuntime by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val commonBundle by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations.named("compileClasspath") { extendsFrom(commonCode) }
configurations.named("testCompileClasspath") { extendsFrom(commonCode) }
configurations.named("testRuntimeClasspath") { extendsFrom(commonRuntime) }

val rootNeoForgeSources = listOf(
    "galacticwars/clonewars/client/gui/GalacticWarsConfigScreen.java",
    "galacticwars/clonewars/client/render/BlasterClientExtensions.java",
    "galacticwars/clonewars/client/render/LightsaberClientExtensions.java",
    "galacticwars/clonewars/gametest/**",
)

sourceSets {
    named("main") {
        java.srcDir(rootProject.file("src/main/java"))
        java.include(rootNeoForgeSources)
        kotlin.srcDir(rootProject.file("src/main/java"))
        resources.srcDir(rootProject.file("src/main/resources"))
        resources.include(
            "META-INF/neoforge.mods.toml",
            "data/galacticwars/neoforge/**",
        )
    }
    named("test") {
        java.srcDir(rootProject.file("src/test/java"))
        resources.srcDir(rootProject.file("src/test/resources"))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    neoForge("net.neoforged:neoforge:$neoForgeVersion")
    implementation("dev.architectury:architectury-neoforge:$architecturyApiVersion")
    implementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForForgeVersion")
    implementation("net.tslat:smartbrainlib-neoforge-26.2:$smartBrainLibVersion")
    implementation("com.geckolib:geckolib-neoforge-26.2:$geckoLibVersion")
    implementation("dev.isxander:yet-another-config-lib:$yaclVersion-neoforge")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    add(commonCode.name, project(path = ":common", configuration = "apiElements")) {
        isTransitive = false
    }
    add(commonRuntime.name, project(path = ":common", configuration = "runtimeElements")) {
        isTransitive = false
    }
    add(commonBundle.name, project(path = ":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }
}

tasks.named<ProcessResources>("processResources") {
    val replacements = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to modLicense,
        "mod_version" to modVersion,
        "minecraft_version" to minecraftVersion,
        "neoforge_version" to neoForgeVersion,
        "kotlin_for_forge_version" to kotlinForForgeVersion,
        "architectury_api_version" to architecturyApiVersion,
        "geckolib_version" to geckoLibVersion,
        "smartbrainlib_version" to smartBrainLibVersion,
        "yacl_version" to yaclVersion,
    )
    inputs.properties(replacements)
    filesMatching("META-INF/neoforge.mods.toml") { expand(replacements) }
}

tasks.named<Jar>("jar") {
    dependsOn(project(":common").tasks.named("transformProductionNeoForge"))
    archiveBaseName.set("galacticwars-neoforge")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({ commonBundle.map { if (it.isDirectory) it else zipTree(it) } })
    exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
}

// Loom adds the common development JAR to the launch classpath as well as the
// common class directories. Keep that JAR current so iterative runs cannot
// execute stale common bytecode ahead of freshly compiled classes.
tasks.matching {
    it.name == "runClient" || it.name == "runServer" || it.name == "runGameTestServer"
}.configureEach {
    dependsOn(project(":common").tasks.named("jar"))
}

tasks.matching { it.name == "runGameTestServer" }.configureEach {
    doFirst {
        // SavedData is part of the behavior under test. A reused GameTest world makes an
        // otherwise clean run inherit conquest, progression and replay state from older runs.
        delete(layout.projectDirectory.dir("run/gametestserver/gametestworld"))
    }
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
    useJUnitPlatform()
    workingDir = rootProject.projectDir
}

val executableHarnessClasses = fileTree(rootProject.file("src/test/java")) {
    include("**/*Test.java")
}.files.map { sourceFile ->
    rootProject.file("src/test/java").toPath()
        .relativize(sourceFile.toPath())
        .toString()
        .replace('\\', '.')
        .replace('/', '.')
        .removeSuffix(".java")
}.sorted()

val executableHarnessTasks = executableHarnessClasses.map { className ->
    val suffix = className.split('.').joinToString("") { segment ->
        segment.replaceFirstChar { character -> character.uppercase() }
    }
    tasks.register<JavaExec>("run$suffix") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Runs executable verification harness $className."
        dependsOn(tasks.named("testClasses"), tasks.named("jar"))
        classpath = sourceSets.test.get().runtimeClasspath
        mainClass.set(className)
        workingDir = rootProject.projectDir
    }
}

val runHarnesses by tasks.registering {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs all ${executableHarnessClasses.size} executable harnesses and any Kotlin/JUnit tests."
    dependsOn(executableHarnessTasks)
    dependsOn(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(runHarnesses)
}
