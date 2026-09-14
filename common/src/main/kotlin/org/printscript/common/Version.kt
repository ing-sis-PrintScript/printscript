package org.printscript.common

enum class Version(val id: String) {
    V10("1.0"),
    V11("1.1"),
    ;

    companion object {
        fun of(id: String): Version? = entries.firstOrNull { it.id == id }
    }
}
