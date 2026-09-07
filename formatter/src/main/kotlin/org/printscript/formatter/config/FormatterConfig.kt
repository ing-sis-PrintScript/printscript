package org.printscript.formatter.config

data class FormatterConfig(
    val spaceBeforeColon: Spacing = Spacing.NONE,
    val spaceAfterColon: Spacing = Spacing.SINGLE,
    val spaceAroundAssignment: Spacing = Spacing.SINGLE,
    val blankLinesBeforePrintln: BlankLines = BlankLines.NONE,
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
