package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.common.getOrNull
import org.printscript.formatter.config.FormatterConfig

internal fun program(vararg nodes: ASTNode): Sequence<Result<ASTNode, PrintScriptError>> =
    nodes.asSequence().map { Result.Success(it) }

internal fun formatToText(
    program: Sequence<Result<ASTNode, PrintScriptError>>,
    config: FormatterConfig,
): String =
    PrintScript10.formatter(config)
        .format(program)
        .mapNotNull { it.getOrNull() }
        .joinToString("") { it.text }
