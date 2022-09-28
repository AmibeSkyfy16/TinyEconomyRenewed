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

import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val transitiveInclude: Configuration by configurations.creating

plugins {
    id("fabric-loom") version "1.0-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.10"
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
    maven("https://maven.bymartrixx.me")
    maven("https://jitpack.io")
    maven("https://maven.nucleoid.xyz")
    maven("https://repo.repsy.io/mvn/amibeskyfy16/repo") // Use for my JsonConfig lib
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")
    modImplementation("net.silkmc:silk-game:${properties["silk_version"]}")

    transitiveInclude(implementation("org.mariadb.jdbc:mariadb-java-client:3.0.7")!!)
    transitiveInclude(implementation("org.ktorm:ktorm-core:3.5.0")!!)
    transitiveInclude(implementation("org.ktorm:ktorm-support-mysql:3.5.0")!!)
    transitiveInclude(implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")!!)
    transitiveInclude(implementation("net.lingala.zip4j:zip4j:2.11.2")!!)
    transitiveInclude(implementation("ch.skyfy.jsonconfiglib:json-config-lib:3.0.9")!!)

    handleIncludes(project, transitiveInclude)

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}

tasks {

    val javaVersion = JavaVersion.VERSION_17

    val copyJarToTestServer = register("copyJarToTestServer") {
        println("copying jar to test server")
//        copyFile("build/libs/${archivesName}-$version.jar", project.property("testServerModsFolder") as String)
//        copyFile("build/libs/${archivesName}-$version.jar", project.property("testClientModsFolder") as String)
    }

    loom {

        runs {

            this.getByName("client"){
                runDir = "testClient"

                // Copy some default files to the test client
                copy {
                    from("dev/prepared_client/.")
                    into("testClient")
                    include("options.txt") // options.txt with my favorite settings
                }
            }

            this.getByName("server"){
                runDir = "testServer"

                // Copy some default files to the test server
                copy {
                    from("dev/prepared_server/.")
                    into("testServer")
                    include("eula.txt") // Accepted eula
                    include("server.properties") // server.properties configured with usefully settings
                    include("world/**") // A flat world, fast to load
                }
            }
        }

    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
            vendor.set(JvmVendorSpec.BELLSOFT)
        }

        withSourcesJar()
        withJavadocJar()
    }

    named<Wrapper>("wrapper") {
        gradleVersion = "7.5.1"
        distributionType = Wrapper.DistributionType.BIN
    }

    @Suppress("UnstableApiUsage")
    named<ProcessResources>("processResources") {
        inputs.property("version", project.version)
        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    named<Javadoc>("javadoc") {
        options.quiet() // Ignore javadoc error message
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = javaVersion.toString()
        kotlinOptions.freeCompilerArgs += "-Xskip-prerelease-check"
    }

    named<JavaCompile>("compileJava") {
        options.encoding = "UTF-8"
        options.release.set(javaVersion.toString().toInt())
    }

    named<Jar>("jar") {
        from("LICENSE") {
            rename { "${it}_${archivesName}" }
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

    build {
        doLast {
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