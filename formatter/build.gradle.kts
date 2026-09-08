plugins {
    id("printscript.published-library-conventions")
}

dependencies {
    api(project(":common"))
    api(project(":token"))

    // El formatter sobre tokens se prueba de punta a punta: fuente -> lexer -> texto.
    testImplementation(project(":lexer"))
}
