package us.dragonma.backgrounds.update

import us.dragonma.getopt.GetOpt
import us.dragonma.getopt.Option
import java.io.BufferedInputStream
import java.net.URL
import java.nio.file.Files
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
    val name = "update-backgrounds"

    private val usage = "Usage: $name [-h]"
    private val help = """$usage
        |  -f   force extraction of non-updated files
        |  -h   display this help
    """.trimMargin()

    private var force = false

    fun run(args: Array<String>): Int {
        GetOpt(args, "h").forEach {
            when (it) {
                Option('f') -> force = true
                Option('h') -> {
                    println(help)
                    return@run 0
                }
            }
        }

        val homePath = System.getProperty("user.home")
        val backgroundsDir = Paths.get(homePath, "Pictures", "Backgrounds").ensureDirectory()
        val downloadsDir = backgroundsDir.resolve("Archives").ensureDirectory()

        val whitelistURL = URL("https://github.com/dragonmaus/__backgrounds/raw/master/white.list")
        val whitelist = whitelistURL.openStream().reader().readLines()

        val credentials = Credentials("Digital Blasphemy Sign In")

        backgroundsDir.resolve("resolutions.list").toFile().readLines()
            .filterNot { it.startsWith('#') }
            .map { it.split(' ').map(String::trim).take(2) }
            .forEach { (resolution, tag) ->
                val targetDir = backgroundsDir
                    .resolve(Ratio(resolution).prettyPrint().replace(':', '_'))
                    .resolve(resolution)
                    .resolve("digitalblasphemy")
                    .ensureDirectory()
                val keep = whitelist.map { "$it$tag.jpg" }

                println(">> Updating $resolution.zip")
                val (zipFile, updated) = fetchFile("$resolution.zip", downloadsDir, credentials)

                if (updated || force) {
                    val zip = ZipInputStream(BufferedInputStream(zipFile.inputStream()))

                    println(">> Extracting ${zipFile.name} into $targetDir")
                    unzip@ while (true) {
                        val entry = zip.nextEntry ?: break
                        if (entry.isDirectory) {
                            continue@unzip
                        }

                        val file = targetDir.resolve(entry.name).toFile()
                        if (!file.validates(entry.size, entry.crc)) {
                            Files.copy(zip, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                            file.setLastModified(entry.time)
                            file.validate(entry.size, entry.crc)
                        }

                        if (!keep.contains(entry.name)) {
                            file.delete()
                        }
                    }

                    // clean up any remaining files that are not explicitly whitelisted
                    Files.list(targetDir).map { it.fileName.toString() }.toList()
                        .subtract(keep)
                        .forEach {
                            targetDir.resolve(it).toFile().deleteRecursively()
                            println("Deleted '$it'")
                        }
                }
            }

        return 0
    }
}
