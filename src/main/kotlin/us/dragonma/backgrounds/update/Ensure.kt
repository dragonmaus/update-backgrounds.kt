package us.dragonma.backgrounds.update

import java.nio.file.Files
import java.nio.file.Path

internal fun Path.ensureDirectory(): Path {
    val file = this.toFile()

    if (!file.isDirectory) {
        if (file.exists()) {
            throw EnsureException("'$this' exists but is not a directory")
        }
        Files.createDirectories(this)
    }
    return this
}

internal fun Path.ensureFile(): Path {
    val file = this.toFile()

    if (!file.isFile) {
        if (file.exists()) {
            throw EnsureException("'$this' exists but is not a file")
        }
        Files.createFile(this)
    }
    return this
}
