# Demo

Archivos `.ps` de ejemplo para probar el CLI a mano. Cada uno aísla **un** caso.
Las salidas de abajo están verificadas corriendo los archivos, no son teóricas.

## Cómo correrlos

```bash
./gradlew :cli:installDist
CLI=cli/build/install/cli/bin/cli

$CLI execution  demo/validos/10-hola-mundo.ps --version 1.0
$CLI validation demo/errores-parser/falta-el-valor.ps --version 1.0
$CLI analyzing  demo/linter/mal-camel-case.ps --config demo/linter/camel-case.json --version 1.1
$CLI formatting demo/formatter/desprolijo.ps --config demo/formatter/basico.json --version 1.0
```

El CLI imprime un contador de progreso (`Parseando... N sentencias`) mientras trabaja;
en los ejemplos de abajo está omitido.

---

## `validos/` — lo que tiene que andar

| Archivo | Versión | Salida |
|---|---|---|
| `10-hola-mundo.ps` | 1.0 | `Joe Doe` |
| `10-operaciones.ps` | 1.0 | `16` `8` `48` `3` `Resultado: 3` `3.75` |
| `10-reasignacion.ps` | 1.0 | `10` `42` — incluye `let x: number;` sin valor, asignado después |
| `11-const-y-boolean.ps` | 1.1 | `Joe` `true` `false` |
| `11-if-else.ps` | 1.1 | `entro al then` `sigo despues del if` |
| `11-if-anidado-y-scope.ps` | 1.1 | `99` `si` `2` — `if` adentro de `if`, y una asignación a una variable de afuera que sobrevive al bloque |
| `11-read.ps` | 1.1 | necesita stdin y `BEST_FOOTBALL_CLUB` seteada |

`11-read.ps` se corre así:

```bash
BEST_FOOTBALL_CLUB="San Lorenzo" $CLI execution demo/validos/11-read.ps --version 1.1
```

---

## `errores-lexer/` — falla antes de armar tokens

| Archivo | Error |
|---|---|
| `caracter-invalido.ps` | `2:19  Caracter inesperado '@'` |
| `string-sin-cerrar.ps` | `1:22  String sin cerrar` |

## `errores-parser/` — hay tokens, pero no forman un statement

| Archivo | Error |
|---|---|
| `falta-punto-y-coma.ps` | `2:1  Se esperaba ';' al final de la declaración` |
| `falta-el-tipo.ps` | `1:7  Se esperaba ':' antes del tipo` |
| `falta-el-valor.ps` | `1:17  Se esperaba un valor, un identificador o '('` |
| `const-sin-valor.ps` | `1:7  Una constante tiene que declararse con un valor` (1.1) |
| `falta-llave-de-cierre.ps` | `2:24  No se esperaba el fin del archivo acá` (1.1) |
| `funcion-desconocida.ps` | `1:8  Se esperaba '=' en la asignación` |
| `println-como-valor.ps` | `1:17  Se esperaba un valor, un identificador o '('` |

Los dos últimos son los interesantes:

- **`funcion-desconocida.ps`** (`saludar("hola");`) no da *"No existe la función"* como uno
  esperaría. `CallParser.canHandle` solo acepta el token `PRINTLN`, así que `saludar` se lee
  como un identificador suelto, `AssignmentParser` lo agarra creyendo que es una asignación,
  y se queja del `=` que falta. El error de ejecución *"No existe la función"* existe en
  `ExpressionEvaluator` pero hoy es inalcanzable desde el parser.
- **`println-como-valor.ps`** es el mismo fenómeno: `println` no está en los tokens que el
  parser acepta en posición de valor, así que falla al parsear y nunca llega al
  *"'println' no devuelve un valor."* del intérprete.

## `errores-interprete/` — parsea bien, revienta al ejecutar

| Archivo | Error |
|---|---|
| `variable-no-declarada.ps` | `1:9  La variable 'noExiste' no ha sido declarada.` |
| `variable-ya-declarada.ps` | `2:1  La variable 'x' ya fue declarada.` |
| `variable-sin-inicializar.ps` | `2:9  La variable 'x' no ha sido inicializada.` |
| `const-reasignada.ps` | `2:1  La constante 'nombre' no se puede reasignar.` (1.1) |
| `tipo-incorrecto.ps` | `1:1  Se esperaba un tipo 'NUMBER' pero se obtuvo un valor distinto.` |
| `division-por-cero.ps` | `3:9  División por cero.` |
| `operacion-invalida.ps` | `3:9  Operación inválida entre 'hola' y '5'.` |
| `if-condicion-no-boolean.ps` | `1:4  La condición de un if tiene que ser un boolean.` (1.1) |
| `fuera-del-scope-del-bloque.ps` | imprime `5`, después `6:9  La variable 'interna' no ha sido declarada.` (1.1) |
| `readenv-no-definida.ps` | `1:23  La variable de entorno 'NO_EXISTE_ESTA_VARIABLE' no está definida.` (1.1) |
| `readinput-con-numero.ps` | `1:19  'readInput' necesita un string y recibió '5'.` (1.1) |
| `readinput-no-convierte.ps` | `1:1  Se esperaba un tipo 'NUMBER' pero se obtuvo un valor distinto.` (1.1) |

Dos que vale la pena mirar:

- **`fuera-del-scope-del-bloque.ps`** imprime `5` (desde adentro del bloque) y recién después
  falla: es `Environment.endScope` descartando la variable al salir del `if`.
- **`readinput-no-convierte.ps`** muestra que `readInput` siempre devuelve un string y no hay
  conversión automática: meterlo en un `number` falla por tipo.

## `version/` — el mismo archivo, distinto resultado según `--version`

| Archivo | 1.1 | 1.0 |
|---|---|---|
| `11-corrido-como-10.ps` | `true` | `1:7  Se esperaba '=' en la asignación` |
| `if-corrido-como-10.ps` | `hola` | `1:3  Se esperaba '=' en la asignación` |
| `read-corrido-como-10.ps` | lee de stdin | `1:26  Se esperaba ';' al final de la declaración` |

No hay ningún `if (version == ...)` en el código: en 1.0 las palabras `const`, `if` y
`readInput` no están en el mapa de keywords, salen como `IDENTIFIER`, y el parser se
confunde solo.

---

## `linter/`

Configs disponibles: `camel-case.json`, `snake-case.json`, `sin-reglas.json`,
`regla-desconocida.json`.

| Archivo + config | Resultado |
|---|---|
| `ok-camel-case.ps` + `camel-case.json` | sin problemas |
| `mal-camel-case.ps` + `camel-case.json` | 3 problemas: `mi_variable`, `Otra_Cosa`, `_guionBajo` |
| `mal-snake-case.ps` + `snake-case.json` | 2 problemas: `miVariable`, `OtraCosa` |
| `mal-camel-case.ps` + `sin-reglas.json` | sin problemas — la regla apagada no se instancia |
| `println-con-expresion.ps` + `camel-case.json` | 2 errores `println-argument` (líneas 4 y 5); las líneas con identificador o literal pasan |
| `readinput-con-expresion.ps` + `camel-case.json` | 1 error `read-input-argument` (1.1) |
| `con-error-de-sintaxis.ps` + `camel-case.json` | 2 problemas: uno `syntax` y uno `identifier-naming` |
| cualquiera + `regla-desconocida.json` | `config: Regla de analisis desconocida: 'esta-regla-no-existe'` |

`con-error-de-sintaxis.ps` es el caso que muestra que el linter **sigue analizando después de
un error de sintaxis**: reporta el error de la línea 1 y el problema de estilo de la línea 2,
los dos por el mismo canal.

## `formatter/`

Configs: `basico.json`, `println-dos-lineas.json`, `if-llave-misma-linea.json`,
`if-llave-abajo.json`, `config-invalida.json`.

**`desprolijo.ps` + `basico.json`**

```
let    nombre: string = "Joe";
let edad: number = 30;
println(nombre + " tiene " + edad);
println("fin");
```

Los 4 espacios después de `let` quedan: `basico.json` no prende
`mandatory-single-space-separation`, y sin una regla que opine, el formatter respeta el
espaciado original.

**`if-desprolijo.ps` + `if-llave-misma-linea.json`** (1.1)

```
const activo: boolean = true;
if(activo) {
    println("adentro");
    let x: number = 1 + 2;
    println(x);
}else {
    println("else");
}
```

**`if-desprolijo.ps` + `if-llave-abajo.json`** (1.1)

```
const activo: boolean = true;
if(activo)
{
  println("adentro");
  let x: number = 1 + 2;
  println(x);
}else
{
  println("else");
}
```

En los dos casos el `}else` queda pegado: no hay ninguna regla de espaciado alrededor del
`else`. Es una limitación real de la implementación de hoy, no un error de la config.

**`config-invalida.json`** → `config: Rule 'line-breaks-after-println' expects a value within 0..2, got 7`
