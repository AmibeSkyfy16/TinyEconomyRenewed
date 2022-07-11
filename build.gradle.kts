/*
 *
 * build.gradle.kts is based on this : https://github.com/StckOverflw/TwitchControlsMinecraft/blob/4bf406893544c3edf52371fa6e7a6cc7ae80dc05/build.gradle.kts
 *
 * This is one of the ways I tried to include no-mod libraries.
 *
 * This build file is the build im using right now, and it seems to work
 *
 * To use this file as build file, have a look at the settings.gradle.kts file
 *
 */

@file:Suppress("GradlePackageVersionRange")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
}

val transitiveInclude: Configuration by configurations.creating

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
    include(":MariaDBServerFabricMC-2.0+1.19")?.let { modImplementation(it) }

//    transitiveInclude(implementation("ch.vorburger.mariaDB4j:mariaDB4j:2.5.3")!!)
    transitiveInclude(implementation("org.mariadb.jdbc:mariadb-java-client:3.0.5")!!)
    transitiveInclude(implementation("org.ktorm:ktorm-core:3.5.0")!!)
    transitiveInclude(implementation("org.ktorm:ktorm-support-mysql:3.5.0")!!)
    transitiveInclude(implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")!!)

    handleIncludes(project, transitiveInclude)

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
        from("LICENSE") {
            rename { "${it}_${archivesBaseName}" }
        }
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

//    val copyJarToTestServer = register("copyJarToTestServer"){
//        println("copy to server")
//        copyFile("build/libs/TinyEconomyRenewed-1.0-SNAPSHOT.jar", project.property("testServerModsFolder") as String)
//    }
//
//    named<DefaultTask>("remapJar"){
//        doLast{
//            copyJarToTestServer.get()
//        }
//    }

}

fun copyFile(src: String, dest: String) {
    copy {
        from(src)
        into(dest)
    }
}

fun DependencyHandlerScope.includeTransitive(
    root: ResolvedDependency?,
    dependencies: Set<ResolvedDependency>,
    fabricLanguageKotlinDependency: ResolvedDependency,
    checkedDependencies: MutableSet<ResolvedDependency> = HashSet()
) {
    dependencies.forEach {
        if (checkedDependencies.contains(it) || (it.moduleGroup == "org.jetbrains.kotlin" && it.moduleName.startsWith("kotlin-stdlib")) || (it.moduleGroup == "org.slf4j" && it.moduleName == "slf4j-api"))
            return@forEach

        if (fabricLanguageKotlinDependency.children.any { kotlinDep -> kotlinDep.name == it.name }) {
            println("Skipping -> ${it.name} (already in fabric-language-kotlin)")
        } else {
            include(it.name)
            println("Including -> ${it.name} from ${root?.name}")
        }
        checkedDependencies += it

        includeTransitive(root ?: it, it.children, fabricLanguageKotlinDependency, checkedDependencies)
    }
}

// from : https://github.com/StckOverflw/TwitchControlsMinecraft/blob/4bf406893544c3edf52371fa6e7a6cc7ae80dc05/build.gradle.kts
fun DependencyHandlerScope.handleIncludes(project: Project, configuration: Configuration) {
    includeTransitive(
        null,
        configuration.resolvedConfiguration.firstLevelModuleDependencies,
        project.configurations.getByName("modImplementation").resolvedConfiguration.firstLevelModuleDependencies
            .first { it.moduleGroup == "net.fabricmc" && it.moduleName == "fabric-language-kotlin" }
    )
}