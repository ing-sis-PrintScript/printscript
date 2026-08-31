package org.printscript.formatter.config

enum class Spacing(private val spaces: Int) {
    NONE(0),
    SINGLE(1),
    ;

    fun render(): String = " ".repeat(spaces)
}
