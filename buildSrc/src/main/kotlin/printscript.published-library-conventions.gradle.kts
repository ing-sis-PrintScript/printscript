plugins {
    id("printscript.kotlin-library-conventions")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(
                    "Modulo ${project.name} de PrintScript, un lenguaje de tipado estatico " +
                        "interpretado, escrito en Kotlin.",
                )
                url.set("https://github.com/ing-sis-PrintScript/printscript")
                scm {
                    url.set("https://github.com/ing-sis-PrintScript/printscript")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ing-sis-PrintScript/printscript")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
