plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    api(project(":ast"))
    implementation(project(":token"))
}
