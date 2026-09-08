package org.printscript.formatter.config

// null es "esa clave no vino en el config", y significa NO TOCAR ese espacio.
// Es el tercer estado que necesita un formatter incremental: no alcanza con
// "ninguno" y "uno", hace falta "el que ya estaba".
//
// Las tres ultimas son interruptores porque el TCK las manda como booleanos y la
// consigna las marca como "no se configura": ausente es apagada, y apagada tampoco
// toca nada.
data class FormatterConfig(
    val spaceBeforeColon: Spacing? = null,
    val spaceAfterColon: Spacing? = null,
    val spaceAroundAssignment: Spacing? = null,
    val lineBreaksAfterPrintln: BlankLines? = null,
    val lineBreakAfterStatement: Boolean = false,
    val spaceSurroundingOperations: Boolean = false,
    val singleSpaceSeparation: Boolean = false,
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
