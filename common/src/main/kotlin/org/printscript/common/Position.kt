package org.printscript.common

data class Position(val line: Int, val column: Int) {
    override fun toString() = "$line:$column"
}

data class Range(val start: Position, val end: Position) {
    override fun toString() = "$start-$end"
}
