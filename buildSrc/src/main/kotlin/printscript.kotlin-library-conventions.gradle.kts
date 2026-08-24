// Una libreria de PrintScript: se compila y testea como todas, pasa por el
// analisis estatico, mide coverage, y expone API (habilita `api(...)`).
plugins {
    id("printscript.kotlin-common-conventions")
    id("printscript.static-analysis-conventions")
    id("printscript.coverage-conventions")
    `java-library`
}
