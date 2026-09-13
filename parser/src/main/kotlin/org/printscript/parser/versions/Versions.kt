package org.printscript.parser.versions

import org.printscript.common.Version
import org.printscript.parser.Parser

fun parserFor(version: Version): Parser =
    when (version) {
        Version.V10 -> PrintScript10.parser()
        Version.V11 -> PrintScript11.parser()
    }
