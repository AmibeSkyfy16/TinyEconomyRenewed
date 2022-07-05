/*
 *
 * build_01.gradle.kts is based on this : https://github.com/homchom/recode/blob/b2d2c2b4bb0126f1d4c0711cae6cb54473e75113/build.gradle.kts
 *
 * This is one of the ways I tried to include no-mod libraries.
 *
 * This build file currently not working (getting some error after copied the jar on my server and started the server)
 *
 * To use this file as build file, have a look at the settings.gradle.kts file
 *
 */


@file:Suppress("GradlePackageVersionRange")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val shade: Configuration by configurations.creating {
    isCanBeResolved = true
    exclude(group = "org.slf4j")
}

val archivesBaseName = property("archives_base_name")
group = property("maven_group")!!
version = property("mod_version")!!

base{
    archivesName.set(properties["archives_base_name"].toString())
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.bymartrixx.me") {}
    maven("https://jitpack.io") {}
    maven("https://maven.nucleoid.xyz"){}
    maven("https://api.modrinth.com/maven"){
        name = "Modrinth"
        content { includeGroup("maven.modrinth") }
    }
    flatDir {
        dirs("libs")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")

    modImplementation("me.bymartrixx.player-events:api:${properties["player_events_api_version"]}")
    include("eu.pb4:sidebar-api:${properties["sidebar-api_version"]}")?.let { modImplementation(it) }
//    include("maven.modrinth:mariadbserverfabricmc:1.0")?.let { modImplementation(it) }

    // Local jar
    include(":MariaDBServerFabricMC-1.0+1.19")?.let { modImplementation(it) }

    shadeImpl("ch.vorburger.mariaDB4j:mariaDB4j:2.5.3")
    shadeImpl("org.mariadb.jdbc:mariadb-java-client:3.0.5")
    shadeImpl("org.ktorm:ktorm-core:3.5.0")
    shadeImpl("org.ktorm:ktorm-support-mysql:3.5.0")
    shadeImpl("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.junit.platform:junit-platform-runner:1.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks {

    val javaVersion = JavaVersion.VERSION_17

    processResources {
        inputs.property("version", project.version)
        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    java {
        withSourcesJar()
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = javaVersion.toString()
    }

    named<JavaCompile>("compileJava") {
        options.encoding = "UTF-8"
        options.release.set(javaVersion.toString().toInt())
    }

    named<Jar>("jar") {
        enabled = false
        from("LICENSE") {
            rename { "${it}_${archivesBaseName}" }
        }
    }

    val relocate by registering(com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation::class) {
        target = shadowJar.get()
        prefix = "${project.properties["maven_group"]}.tinyeconomyrenewed.shaded"
    }

    shadowJar {
        dependsOn(relocate)
        configurations = listOf(shade)
        destinationDirectory.set(file("build/devlibs"))
        archiveClassifier.set("dev")

        from("LICENSE")
    }

    remapJar {
        inputFile.value(shadowJar.get().archiveFile)
    }


    named<Test>("test") { // https://stackoverflow.com/questions/40954017/gradle-how-to-get-output-from-test-stderr-stdout-into-console
        useJUnitPlatform()

        testLogging {
            outputs.upToDateWhen { false } // When the build task is executed, stderr-stdout of test classes will be show
            showStandardStreams = true
        }

        doLast {
            println("Displaying some default values:")
            println("\tshowStackTraces: ${testLogging.showStackTraces}")
            println("\tshowExceptions: ${testLogging.showExceptions}")
            println("\tshowCauses: ${testLogging.showCauses}")
            println("\tshowStandardStreams: ${testLogging.showStandardStreams}")
        }
    }

}

typealias DependencyConfig = Action<ExternalModuleDependency>

fun DependencyHandlerScope.shadeImpl(notation: Any) {
    implementation(notation)
    shade(notation)
}

fun DependencyHandlerScope.shadeApi(notation: Any) {
    api(notation)
    shade(notation)
}

fun DependencyHandlerScope.includeImpl(notation: Any) {
    implementation(notation)
    include(notation)
}

fun DependencyHandlerScope.includeApi(notation: Any) {
    api(notation)
    include(notation)
}

fun DependencyHandlerScope.includeModImpl(notation: Any) {
    modImplementation(notation)
    include(notation)
}

fun DependencyHandlerScope.includeModApi(notation: Any) {
    modApi(notation)
    include(notation)
}
