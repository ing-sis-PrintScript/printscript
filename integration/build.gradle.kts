plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    testImplementation(project(":common"))
    testImplementation(project(":token"))
    testImplementation(project(":ast"))
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
    testImplementation(project(":interpreter"))
}
