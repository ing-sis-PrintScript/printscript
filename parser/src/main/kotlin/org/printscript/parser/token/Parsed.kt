package org.printscript.parser.token


data class Parsed<out T>(val value: T, val rest: TokenStream)
