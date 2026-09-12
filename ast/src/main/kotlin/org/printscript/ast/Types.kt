package org.printscript.ast

enum class DeclaredType { NUMBER, STRING, BOOLEAN }

// Con que palabra se declaro la variable. Un enum y no un booleano: en el lugar donde se
// usa, DeclarationKind.CONST se entiende solo y "false" no.
enum class DeclarationKind { LET, CONST }

enum class BinaryOperator { PLUS, MINUS, TIMES, DIVIDE }

enum class UnaryOperator { MINUS }
