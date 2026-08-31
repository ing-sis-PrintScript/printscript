// Como se compila y testea un modulo de PrintScript. Nada de analisis estatico
// ni de coverage: eso vive en sus propias convenciones.
plugins {
    kotlin("jvm")
}

group = "printscript"
version = providers.gradleProperty("releaseVersion").getOrElse("1.0.0-SNAPSHOT")

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
