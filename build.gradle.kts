// Sin allprojects {} ni subprojects {}: cada modulo aplica explicitamente la
// convencion que necesita, y la raiz solo suma lo que es del proyecto entero.
plugins {
    id("printscript.coverage-aggregation")
    id("printscript.git-hooks")
}
