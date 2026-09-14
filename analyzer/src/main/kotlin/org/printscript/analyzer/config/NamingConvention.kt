package org.printscript.analyzer.config

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
