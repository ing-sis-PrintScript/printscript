package org.printscript.formatter.config

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
