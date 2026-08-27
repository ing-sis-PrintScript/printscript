package org.printscript.formatter.config

data class FormatterConfig(
    val spaceBeforeColon: Spacing = Spacing.NONE,
    val spaceAfterColon: Spacing = Spacing.SINGLE,
    val spaceAroundAssignment: Spacing = Spacing.SINGLE,
    val blankLinesBeforePrintln: BlankLines = BlankLines.NONE,
)
