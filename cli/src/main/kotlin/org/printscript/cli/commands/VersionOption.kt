package org.printscript.cli.commands

import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import org.printscript.common.Version

internal fun ParameterHolder.versionOption() =
    option("--version", help = "Version de PrintScript: 1.0 o 1.1")
        .convert { Version.of(it) ?: fail("version desconocida: '$it'. Las validas son 1.0 y 1.1") }
        .default(Version.V10)
