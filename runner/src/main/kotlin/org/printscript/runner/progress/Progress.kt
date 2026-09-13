package org.printscript.runner.progress

fun interface Progress {
    fun parsed()

    fun done() {}

    companion object {
        val NONE = Progress { }
    }
}
