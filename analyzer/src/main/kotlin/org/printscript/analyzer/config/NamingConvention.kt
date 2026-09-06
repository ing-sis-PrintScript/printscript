package org.printscript.analyzer.config

/**
 * Si un nombre respeta la convención elegida. Un colaborador que se le
 * inyecta a IdentifierNamingRule, no un if adentro de ella — la regla no
 * sabe distinguir camelCase de snake_case, solo sabe preguntarle a su
 * NamingConvention. Agregar PascalCase el día de mañana es un object nuevo
 * acá, no tocar la regla que lo usa.
 */
fun interface NamingConvention {
    fun matches(name: String): Boolean
}

object CamelCase : NamingConvention {
    private val PATTERN = Regex("^[a-z][a-zA-Z0-9]*$")

    override fun matches(name: String): Boolean = PATTERN.matches(name)
}

object SnakeCase : NamingConvention {
    private val PATTERN = Regex("^[a-z][a-z0-9_]*$")

    override fun matches(name: String): Boolean = PATTERN.matches(name)
}
