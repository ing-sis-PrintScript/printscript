plugins {
    id("printscript.kotlin-common-conventions")
    id("printscript.static-analysis-conventions")
    id("printscript.coverage-conventions")
    application
}

dependencies {
    implementation(project(":common"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    implementation(project(":formatter"))
    implementation(libs.clikt)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.yaml)
}

application {
    mainClass.set("org.printscript.cli.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
