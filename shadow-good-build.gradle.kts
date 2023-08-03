/*
 *
 * shadow-good-build.gradle.kts is based on this : https://github.com/QuiltServerTools/Ledger/blob/master/build.gradle.kts
 *
 * This is one of the ways I tried to include no-mod libraries.
 *
 * This build file is the build im using right now, and it seems to work
 *
 * To use this file as build file, have a look at the settings.gradle.kts file
 *
 */

@file:Suppress("GradlePackageVersionRange")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("fabric-loom") version "1.3-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    idea
}

base {
    archivesName.set(properties["archives_name"].toString())
    group = property("maven_group")!!
    version = property("mod_version")!!
}

repositories {
    mavenCentral()
    mavenLocal()
//    maven("https://maven.bymartrixx.me")
    maven("https://jitpack.io")
    maven("https://maven.nucleoid.xyz")
    maven("https://repo.repsy.io/mvn/amibeskyfy16/repo") // Use for my json5Config lib
    maven("https://cursemaven.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")
    modImplementation("net.silkmc:silk-game:${properties["silk_version"]}")

//    include("curse.maven:project-835038:4427154")?.let { modRuntimeOnly(it) } // My mod MariaDBServerFabricMC-0.0.1+1.19.3 is required
//    modLocalRuntime("curse.maven:project-835038:4427154")

//    transitiveInclude(implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")!!)
//    transitiveInclude(implementation("org.ktorm:ktorm-core:3.6.0")!!)
//    transitiveInclude(implementation("org.ktorm:ktorm-support-mysql:3.6.0")!!)
//    transitiveInclude(implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")!!)
//    transitiveInclude(implementation("net.lingala.zip4j:zip4j:2.11.2")!!)
//    transitiveInclude(implementation("ch.skyfy.jsonconfiglib:json-config-lib:3.0.14")!!)
//    transitiveInclude(implementation("com.jayway.jsonpath:json-path:2.7.0")!!)
//    transitiveInclude(implementation("io.github.binance:binance-connector-java:2.0.0rc2")!!)

//    handleIncludes(project, transitiveInclude)

    shadow(implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")!!)
    shadow("org.ktorm:ktorm-core:3.6.0")
    shadow("org.ktorm:ktorm-support-mysql:3.6.0")
    shadow("net.lingala.zip4j:zip4j:2.11.2")
    shadow("ch.skyfy.jsonconfiglib:json-config-lib:3.0.14")
    shadow("com.jayway.jsonpath:json-path:2.8.0")
    shadow("io.github.binance:binance-connector-java:2.0.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.22")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

configurations.implementation.get().extendsFrom(configurations.shadow.get())

tasks {

    val javaVersion = JavaVersion.VERSION_17

    processResources {
        inputs.property("version", project.version)
        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }

    loom {
        runs {
            this.getByName("client") {
                runDir = "testclient"

                val file = File("preconfiguration/doneclient.txt")
                if (!file.exists()) {
                    println("copying to client")
                    file.createNewFile()

                    // Copy some default files to the test client
                    copy {
                        from("preconfiguration/prepared_client/.")
                        into("testclient")
                        include("options.txt") // options.txt with my favorite settings
                    }

                    // Copying the world to use
                    copy {
                        from("preconfiguration/worlds/.")
                        include("testworld#1/**")
                        into("testclient/saves")
                    }

                    // Copying useful mods
                    copy {
                        from("preconfiguration/mods/client/.", "preconfiguration/mods/both/.")
                        include("*.jar")
                        into("testclient/mods")
                    }

                }
            }
            this.getByName("server") {
                runDir = "testserver"

                val file = File("preconfiguration/doneserver.txt")
                if (!file.exists()) {
                    file.createNewFile()
                    println("copying to server")

                    // Copy some default files to the test server
                    copy {
                        from("preconfiguration/prepared_server/.")
                        include("server.properties") // server.properties configured with usefully settings
                        include("eula.txt") // Accepted eula
                        into("testserver")
                    }

                    // Copying the world to use
                    copy {
                        from("preconfiguration/worlds/.")
                        include("testworld#1/**")
                        into("testserver")
                    }

                    // Copying useful mods
                    copy {
                        from("preconfiguration/mods/server/.", "preconfiguration/mods/both/.")
                        include("*.jar")
                        into("testserver/mods")
                    }
                }
            }
        }
    }

    java {
        toolchain {
//            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
//            vendor.set(JvmVendorSpec.BELLSOFT)
        }
        withSourcesJar()
        withJavadocJar()
    }

    named<Wrapper>("wrapper") {
        gradleVersion = "8.2.1"
        distributionType = Wrapper.DistributionType.BIN
    }

    named<Javadoc>("javadoc") {
        options {
            (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }

    named<Jar>("jar") {
        from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } }
    }

    /**
     * thanks to
     * - https://github.com/QuiltServerTools/Ledger/blob/master/build.gradle.kts
     * - https://github.com/LuckPerms/LuckPerms/blob/4068c71d5a1ff9f70c59ea22a4abff68604183a3/fabric/build.gradle#L4
     */
    named<ShadowJar>("shadowJar"){
        from("LICENSE")

        archiveFileName.set("${project.properties["archives_name"]}-${project.properties["mod_version"]}-shadowed.jar")

        configurations = listOf(project.configurations.shadow.get())

        exclude("kotlin/**", "kotlinx/**", "javax/**", "META-INF")
        exclude("org/checkerframework/**", "org/intellij/**", "org/jetbrains/annotations/**")
        exclude("com/google/gson/**")
        exclude("net/kyori/**")
        exclude("org/slf4j/**")

        relocate("org.mariadb.jdbc", "ch.skyfy.tinyeconomyrenewed.libs.jdbc")
        relocate("org.ktorm", "ch.skyfy.tinyeconomyrenewed.libs.ktorm")
        relocate("org.objectweb", "ch.skyfy.tinyeconomyrenewed.libs.objectweb")
        relocate("org.apache.commons", "ch.skyfy.tinyeconomyrenewed.libs.org.apache.commons")
        relocate("org.json", "ch.skyfy.tinyeconomyrenewed.libs.org.json")
        relocate("net.lingala.zip4j", "ch.skyfy.tinyeconomyrenewed.libs.net.lingala.zip4j")
        relocate("net.minidev", "ch.skyfy.tinyeconomyrenewed.libs.net.minidev")
        relocate("com.jayway.jsonpath", "ch.skyfy.tinyeconomyrenewed.libs.com.jayway.jsonpath")
        relocate("com.binance", "ch.skyfy.tinyeconomyrenewed.libs.com.binance")
        relocate("com.sun.jna", "ch.skyfy.tinyeconomyrenewed.libs.com.sun.jna")
        relocate("com.google", "ch.skyfy.tinyeconomyrenewed.libs.com.google")
        relocate("com.github", "ch.skyfy.tinyeconomyrenewed.libs.com.github")
        relocate("mu", "ch.skyfy.tinyeconomyrenewed.libs.mu")
        relocate("waffle", "ch.skyfy.tinyeconomyrenewed.libs.waffle")
        relocate("okio", "ch.skyfy.tinyeconomyrenewed.libs.okio")
        relocate("okhttp3", "ch.skyfy.tinyeconomyrenewed.libs.okhttp3")
    }

    named<RemapJarTask>("remapJar"){
        dependsOn(shadowJar.get())
        inputFile.set(shadowJar.get().archiveFile)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = javaVersion.toString()
        kotlinOptions.freeCompilerArgs += "-Xskip-prerelease-check" // Required by others project like SilkMC. Also add this to intellij setting under Compiler -> Kotlin Compiler -> Additional ...
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(javaVersion.toString().toInt())
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

    val copyJarToTestServer = register("copyJarToTestServer") {
        println("copying jar to server")
//        copyFile("build/libs/${project.properties["archives_name"]}-${project.properties["mod_version"]}.jar", project.property("testServerModsFolder") as String)
//        copyFile("build/libs/${project.properties["archives_name"]}-${project.properties["mod_version"]}.jar", project.property("testClientModsFolder") as String)
    }

    build { doLast { copyJarToTestServer.get() } }

}

fun copyFile(src: String, dest: String) = copy { from(src); into(dest) }