# PrintScript

Intérprete, formatter y linter de PrintScript, un lenguaje de tipado estático
parecido a TypeScript. Escrito en Kotlin, repartido en diez módulos Gradle.

Soporta dos versiones del lenguaje: **1.0** y **1.1**. Cada comando elige con
qué versión leer el archivo.

---

## El lenguaje

### PrintScript 1.0

```javascript
let nombre: string = "Joe";
let edad: number = 25;
edad = edad + 1;
println("Hola " + nombre);
```

Tipos: `number`, `string`. Operadores: `+`, `-`, `*`, `/` y el menos unario.

### PrintScript 1.1

Suma constantes, booleanos, condicionales y lectura de entrada y de entorno.

```javascript
const limite: number = 10;
let activo: boolean = true;

if (activo) {
    println("adentro");
} else {
    println("afuera");
}

const nombre: string = readInput("Nombre:");
const club: string = readEnv("BEST_FOOTBALL_CLUB");
```

Lo que agrega: `const`, `boolean` con `true` / `false`, `if` / `else` con
bloques, `readInput(prompt)` y `readEnv(nombre)`.

Una constante tiene que declararse con un valor y no se puede reasignar. Las
variables declaradas dentro de un bloque dejan de existir al salir.

---

## Uso

```bash
./gradlew :cli:installDist
./cli/build/install/cli/bin/cli <comando> <archivo.ps> [opciones]
```

Cuatro comandos:

| comando | qué hace |
|---|---|
| `validation` | revisa que el archivo parsee y reporta los errores de sintaxis |
| `execution` | ejecuta el programa |
| `formatting` | reformatea el archivo según un config |
| `analyzing` | reporta violaciones de estilo según un config |

Todos aceptan `--version 1.0` o `--version 1.1` (por defecto `1.0`).
`formatting` y `analyzing` aceptan `--config` con un `.json`, `.yaml` o `.yml`
(en `analyzing` es obligatorio).

```bash
cli execution main.ps --version 1.1
cli formatting main.ps --config formato.json --version 1.1
cli analyzing main.ps --config estilo.yaml --version 1.1
```

---

## Configuración

### Formatter

```json
{
  "enforce-spacing-before-colon-in-declaration": false,
  "enforce-spacing-after-colon-in-declaration": true,
  "enforce-spacing-around-equals": true,
  "line-breaks-after-println": 1,
  "mandatory-line-break-after-statement": true,
  "mandatory-space-surrounding-operations": true,
  "mandatory-single-space-separation": true,

  "if-brace-same-line": true,
  "indent-inside-if": 4
}
```

`enforce-no-spacing-around-equals` es la clave inversa de
`enforce-spacing-around-equals`: gobiernan el mismo espacio. Lo mismo
`if-brace-below-line` con `if-brace-same-line`.

Las tres últimas son de 1.1. `line-breaks-after-println` admite 0, 1 o 2;
`indent-inside-if`, de 0 a 8.

**Una clave ausente significa "no toques ese espacio"**, no "ninguno". El
formatter preserva el espaciado del archivo y solo reescribe donde una clave se
lo pide.

### Linter

```json
{
  "identifier_format": "camel case",
  "mandatory-variable-or-literal-in-println": true,
  "mandatory-variable-or-literal-in-readInput": true
}
```

`identifier_format` acepta `"camel case"` o `"snake case"` (con el espacio). La
última clave es de 1.1.

---

## Estructura

```
common      Result, Position, Range, Version. Sin dependencias.
token       Token y TokenType: el vocabulario entre lexer y parser.
ast         Los nodos del arbol.
lexer       Texto  ->  tokens
parser      Tokens ->  statements
interpreter Statements -> efectos
formatter   Tokens ->  texto formateado
analyzer    AST    ->  diagnosticos de estilo
runner      Arma el pipeline. Es el unico que conoce a todos.
cli         Solo argumentos y salida por pantalla.
```

Cada módulo tiene un paquete `versions/` con `PrintScript10`, `PrintScript11` y
un `Versions.kt`. Las clases que hacen el trabajo no saben que existen
versiones: reciben las reglas ya armadas.

El `formatter` depende de `token`, **no** de `ast`: formatear es preservar el
espaciado del archivo, y el árbol no lo tiene.

Los diagramas de clases y de secuencia de cada módulo están en
[`source/`](source), en PlantUML, más un `end-to-end.puml` en la raíz de esa
carpeta.

---

## Desarrollo

```bash
./gradlew check                 # tests + ktlint + detekt + cobertura
./gradlew publishToMavenLocal   # para probar contra el TCK local
```

`check` exige 80% de cobertura de instrucciones sobre el proyecto entero.
El repo tiene un hook de pre-commit que corre lo mismo.

El TCK de la cátedra vive en un repo aparte y consume los artefactos publicados.

---

## Publicación

Los diez módulos se publican a GitHub Packages. La versión sale del tag: crear
un release `v1.1.0` dispara el workflow, que corre

```bash
./gradlew publish -PreleaseVersion=1.1.0
```
