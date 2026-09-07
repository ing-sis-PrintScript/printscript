plugins {
    id("printscript.published-library-conventions")
}

dependencies {
    api(project(":common"))
    api(project(":lexer"))
    api(project(":parser"))
    api(project(":interpreter"))
    api(project(":formatter"))
    api(project(":analyzer"))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.yaml)
}
