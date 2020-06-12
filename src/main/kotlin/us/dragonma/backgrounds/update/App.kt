package us.dragonma.backgrounds.update

import us.dragonma.getopt.GetOpt
import us.dragonma.getopt.Option
import java.awt.Desktop
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream
import kotlin.streams.toList
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        exitProcess(App().run(args))
    } catch (e: Exception) {
        System.err.println("${App().name}: ${e.message}")
        exitProcess(1)
    }
}

private class App {
    internal val name = "update-backgrounds"

    private val usage = "Usage: $name [-h]"
    private val help = """$usage
        |  -h   display this help
    """.trimMargin()

    fun run(args: Array<String>): Int {
        GetOpt(args, "gh").forEach {
            when (it) {
                Option('h') -> {
                    println(help)
                    return 0
                }
            }
        }

        val homePath = System.getProperty("user.home")
        val backgroundsDir = Paths.get(homePath, "Pictures", "Backgrounds").ensureDirectory()
        val downloadsDir = backgroundsDir.resolve("Archives").ensureDirectory()
        val blacklistPath = backgroundsDir.resolve("black.list").ensureFile()
        val whitelistPath = backgroundsDir.resolve("white.list").ensureFile()

        val blacklist = blacklistPath.toFile().readLines().toMutableSet()
        val whitelist = whitelistPath.toFile().readLines().toMutableSet()

        val credentials = Credentials("Digital Blasphemy Sign In")

        backgroundsDir.resolve("resolutions.list").toFile().readLines()
            .filterNot { it.startsWith('#') }
            .map { it.split(' ').map(String::trim).take(2) }
            .forEach { (resolution, tag) ->
                val targetDir = backgroundsDir
                    .resolve(Ratio(resolution).prettyPrint().replace(':', '_'))
                    .resolve(resolution)
                    .resolve("digitalblasphemy")
                val state = emptyMap<String, String>().toMutableMap()
                blacklist.forEach { state["$it$tag.jpg"] = "black" }
                whitelist.forEach { state["$it$tag.jpg"] = "white" }

                println(">> Updating $resolution.zip")
                val zipFile = fetchFile(
                    "$resolution.zip",
                    downloadsDir,
                    credentials
                )

                val zip = ZipInputStream(BufferedInputStream(zipFile.inputStream()))

                println(">> Extracting ${zipFile.name} into $targetDir")
                Files.createDirectories(targetDir)
                unzip@ while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.isDirectory) {
                        continue
                    }

                    val file = targetDir.resolve(entry.name).toFile()
                    if (!file.validates(entry.size, entry.crc)) {
                        Files.copy(zip, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        file.setLastModified(entry.time)
                        file.validate(entry.size, entry.crc)
                    }

                    if (!state.contains(entry.name)) {
                        state[entry.name] = "new"
                    }

                    if (!arrayOf("black", "white").contains(state[entry.name])) {
                        Desktop.getDesktop().open(file)
                        val console = System.console() ?: continue@unzip
                        query@ while (true) {
                            print("-- Keep ${entry.name}? (y/n/r): ")
                            when (console.readLine()?.toLowerCase()?.trim()?.getOrDefault(0, 'x')) {
                                'y' -> {
                                    state[entry.name] = "white"
                                    break@query
                                }
                                'n' -> {
                                    state[entry.name] = "black"
                                    break@query
                                }
                                'r' -> Desktop.getDesktop().open(file)
                            }
                        }
                    }

                    if (state[entry.name] == "black") {
                        file.delete()
                    }
                }

                // clean up any remaining files that are not explicitly whitelisted
                Files.list(targetDir).map { it.fileName.toString() }.toList()
                    .subtract(state.filterValues { it == "white" }.keys)
                    .forEach {
                        targetDir.resolve(it).toFile().deleteRecursively()
                        println("Deleted '$it'")
                    }

                // update filter lists
                val pattern = "$tag\\.jpg\$".toRegex()
                blacklist.updateAndSave(
                    state.filterValues { it == "black" }.keys.map { it.replace(pattern, "") },
                    blacklistPath
                )
                whitelist.updateAndSave(
                    state.filterValues { it == "white" }.keys.map { it.replace(pattern, "") },
                    whitelistPath
                )
            }

        return 0
    }
}

private fun MutableSet<String>.updateAndSave(elements: Collection<String>, path: Path) {
    if (this.addAll(elements)) {
        val tempPath = Paths.get("$path.tmp")
        tempPath.toFile().writeText(this.sorted().joinToString(separator = "\n", postfix = "\n"))
        Files.move(
            tempPath,
            path,
            StandardCopyOption.ATOMIC_MOVE
        )
    }
}

private fun String.getOrDefault(i: Int, c: Char): Char {
    if (i >= 0 && i < this.length) {
        return this[i]
    }
    return c
}
