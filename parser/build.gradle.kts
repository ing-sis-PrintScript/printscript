plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    api(project(":ast"))
    api(project(":common"))
    api(project(":token"))
}
