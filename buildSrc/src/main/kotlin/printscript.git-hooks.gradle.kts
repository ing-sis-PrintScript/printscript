// .git/hooks no se versiona, asi que el hook vive en .githooks/ y se copia.
// Enganchado a `check`, que se corre igual: la instalacion deja de depender de
// que cada uno se acuerde de hacerla en su maquina.
plugins {
    base
}

tasks.register<Copy>("installGitHooks") {
    from(rootDir.resolve(".githooks"))
    into(rootDir.resolve(".git/hooks"))
    // Sin permiso de ejecucion, Git ignora el hook en silencio.
    filePermissions { unix("0755") }
}

tasks.named("check") {
    dependsOn("installGitHooks")
}
