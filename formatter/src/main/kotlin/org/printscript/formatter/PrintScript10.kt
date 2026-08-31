package org.printscript.formatter

import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.engine.NodeDispatcher
import org.printscript.formatter.engine.PrintScriptFormatter
import org.printscript.formatter.expressions.PrintScript10ExpressionFormatter
import org.printscript.formatter.statements.DeclarationFormatter
import org.printscript.formatter.statements.PrintScript10StatementDispatcher
import org.printscript.formatter.syntax.StatementSeparator

object PrintScript10 {
    fun dispatcher(): NodeDispatcher {
        val expressions = PrintScript10ExpressionFormatter()
        return PrintScript10StatementDispatcher(DeclarationFormatter(expressions), expressions)
    }

    fun formatter(config: FormatterConfig): Formatter = PrintScriptFormatter(dispatcher(), StatementSeparator(), config)
}
