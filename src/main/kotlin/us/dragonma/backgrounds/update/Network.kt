package us.dragonma.backgrounds.update

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.net.ssl.HttpsURLConnection

internal fun fetchFile(filename: String, directory: Path, credentials: Credentials): File {
    val url = URL("https://digitalblasphemy.com/content/zips/$filename")
    val path = directory.resolve(filename)
    val file = path.toFile()

    if (file.exists()) {
        url.setupConnection("HEAD", credentials).run {
            if (contentLengthLong == file.length() && lastModified == file.lastModified()) {
                return file
            }
        }
    }

    url.setupConnection("GET", credentials).run {
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
        file.setLastModified(lastModified)
    }

    return file
}

private fun URL.setupConnection(method: String, credentials: Credentials): HttpsURLConnection {
    return (this.openConnection() as HttpsURLConnection).apply {
        setRequestProperty("Authorization", credentials.basicAuth)
        setRequestProperty("User-Agent", "Mozilla/5.0")
        requestMethod = method
        allowUserInteraction = false
        doInput = true
        doOutput = false

        if (responseCode != 200) {
            throw NetworkException("unable to connect: $responseMessage")
        }
        if (contentType != "application/zip") {
            throw NetworkException("unable to connect: Unknown error")
        }
    }
}
