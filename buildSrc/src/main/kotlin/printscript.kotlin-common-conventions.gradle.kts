plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

group = "printscript"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Los precompiled script plugins no tienen el accessor `libs`; hay que pedir el
// catalogo del proyecto que aplica el plugin.
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(libs.findVersion("jvm").get().requiredVersion.toInt())
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
