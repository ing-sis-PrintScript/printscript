plugins {
    id("printscript.published-library-conventions")
}

dependencies {
    api(project(":ast"))
    api(project(":common"))
    api(project(":token"))
}
