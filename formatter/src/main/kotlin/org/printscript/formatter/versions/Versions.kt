package org.printscript.formatter.versions

import org.printscript.common.Version
import org.printscript.formatter.Formatter
import org.printscript.formatter.config.FormatterConfig

fun formatterFor(
    version: Version,
    config: FormatterConfig,
): Formatter =
    when (version) {
        Version.V10 -> PrintScript10.formatter(config)
        Version.V11 -> PrintScript11.formatter(config)
    }
