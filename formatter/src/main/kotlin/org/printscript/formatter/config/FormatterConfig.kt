package org.printscript.formatter.config

// null es "esa clave no vino en el config", y significa NO TOCAR ese espacio.
// Es el tercer estado que necesita un formatter incremental: no alcanza con
// "ninguno" y "uno", hace falta "el que ya estaba".
data class FormatterConfig(
    val spaceBeforeColon: Spacing? = null,
    val spaceAfterColon: Spacing? = null,
    val spaceAroundAssignment: Spacing? = null,
    val blankLinesBeforePrintln: BlankLines? = null,
)

enum class Spacing(private val spaces: Int) {
    NONE(0),
    SINGLE(1),
    ;

    fun render(): String = " ".repeat(spaces)
}

enum class BlankLines(private val count: Int) {
    NONE(0),
    ONE(1),
    TWO(2),
    ;

    fun render(): String = "\n".repeat(count)

    companion object {
        val ALLOWED: IntRange = 0..2

        fun of(count: Int): BlankLines? = entries.firstOrNull { it.count == count }
    }
}
