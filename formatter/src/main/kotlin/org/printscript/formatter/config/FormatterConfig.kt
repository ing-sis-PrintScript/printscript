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
    val ifBrace: BracePosition? = null,
    val indentInsideIf: Indent? = null,
)

// Donde va la "{" que abre el cuerpo de un if.
//
// Un enum y no dos booleanos porque if-brace-same-line y if-brace-below-line no son
// dos reglas: son una sola perilla con tres estados --arriba, abajo, y no tocar--.
// Con dos booleanos se puede escribir "las dos true", que no quiere decir nada.
// Es lo mismo que ya pasa con enforce-spacing-around-equals y su clave inversa:
// dos claves del TCK, un solo campo.
enum class BracePosition {
    SAME_LINE,
    BELOW_LINE,
    ;

    fun render(): String =
        when (this) {
            SAME_LINE -> " "
            BELOW_LINE -> "\n"
        }
}

// Cuantos espacios de sangria por nivel de bloque (indent-inside-if).
//
// No es un Int pelado por lo mismo que Spacing y BlankLines no lo son: el tipo sabe
// que valores son validos y sabe escribirse.
data class Indent(private val spaces: Int) {
    fun render(depth: Int): String = " ".repeat(spaces * depth)

    companion object {
        val ALLOWED: IntRange = 0..8

        fun of(spaces: Int): Indent? = if (spaces in ALLOWED) Indent(spaces) else null
    }
}

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
