import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version("1.8.10") apply true
}

group = "octest.project"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation ("org.ktorm:ktorm-support-sqlite:3.6.0")
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation ("org.apache.logging.log4j:log4j-api:2.22.0")
    implementation ("org.apache.logging.log4j:log4j-core:2.22.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.22.0")
    implementation ("io.github.microutils:kotlin-logging-jvm:2.0.6")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.2.1")
    implementation("br.com.devsrsouza.compose.icons:tabler-icons:1.1.0")
//    // Enables FileKit without Compose dependencies
//    implementation("io.github.vinceglb:filekit-core:0.6.3")
//
//    // Enables FileKit with Composable utilities
//    implementation("io.github.vinceglb:filekit-compose:0.6.3")

    // Enables FileKit without Compose dependencies
    implementation("io.github.vinceglb:filekit-core:0.8.0")

    // Enables FileKit with Composable utilities
    implementation("io.github.vinceglb:filekit-compose:0.8.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "UDTBrowser"
            packageVersion = "1.0.0"

            linux {
                modules("jdk.security.auth")
            }
        }
    }
}
