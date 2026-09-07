package org.printscript.runner

import org.printscript.lexer.source.SourceReader

// Como abrir la fuente, no la fuente ya abierta.
//
// La diferencia es de memoria. Un SourceReader es el primer eslabon de una cadena y
// cada eslabon apunta al siguiente: quien lo guarda en una variable mantiene viva la
// cadena entera, o sea el archivo completo. Una fabrica no referencia ningun eslabon
// --sabe fabricar uno-- asi que ya no hay forma de sostener el primero sin querer.
//
// Antes esto era un comentario en StreamSourceReader pidiendo que los comandos lo
// crearan en linea. Un comentario no lo puede garantizar: ExecuteRunner lo incumplia.
fun interface SourceFactory {
    fun open(): SourceReader
}
