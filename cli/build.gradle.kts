plugins {
    id("printscript.kotlin-common-conventions")
    id("printscript.static-analysis-conventions")
    id("printscript.coverage-conventions")
    application
}

dependencies {
    implementation(project(":runner"))
    implementation(project(":common"))
    implementation(project(":lexer"))
    implementation(project(":formatter"))
    implementation(project(":analyzer"))
    implementation(libs.clikt)
}

application {
    mainClass.set("org.printscript.cli.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
