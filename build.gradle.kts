@file:Suppress("GradlePackageVersionRange")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val transitiveInclude: Configuration by configurations.creating {
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "com.mojang")
}

plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
}

val archivesBaseName = property("archives_base_name")
group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.bymartrixx.me") {}
    maven("https://jitpack.io") {}
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")

    modImplementation("me.bymartrixx.player-events:api:${properties["player_events_api_version"]}")

    transitiveInclude(implementation("com.github.saibotk:JMAW:0.3.1")!!)
    transitiveInclude(implementation("ch.vorburger.mariaDB4j:mariaDB4j:2.5.3")!!)
    transitiveInclude(implementation("org.mariadb.jdbc:mariadb-java-client:3.0.5")!!)
    transitiveInclude(implementation("org.ktorm:ktorm-core:3.5.0")!!)
    transitiveInclude(implementation("org.ktorm:ktorm-support-mysql:3.5.0")!!)
    transitiveInclude(implementation("net.lingala.zip4j:zip4j:2.11.1")!!)
    transitiveInclude(implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")!!)

    transitiveInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }

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
        from("LICENSE") {
            rename { "${it}_${archivesBaseName}" }
        }
    }

    named<Test>("test") {
        useJUnitPlatform()
    }

    val copyJarToTestServer = register("copyJarToTestServer"){
        println("copy to server")
        copyFile("build/libs/TinyEconomyRenewed-1.0-SNAPSHOT.jar", project.property("testServerModsFolder") as String)
    }

    named<DefaultTask>("remapJar"){
        doLast{
            copyJarToTestServer.get()
        }
    }

}

fun copyFile(src: String, dest: String) {
    copy {
        from(src)
        into(dest)
    }
}