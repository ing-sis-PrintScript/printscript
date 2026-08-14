// buildSrc es un build aparte: tiene sus propios repos y su propia copia del
// catalogo de versiones. Sin este bloque el accessor `libs` no existe aca.
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
