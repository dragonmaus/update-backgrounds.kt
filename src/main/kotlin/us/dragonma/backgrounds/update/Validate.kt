package us.dragonma.backgrounds.update

import java.io.File
import java.util.zip.CRC32

private fun ByteArray.validate(size: Long, checksum: Long) {
    if (this.size.toLong() != size) {
        throw ValidateException("sizes do not match: expected $size; got ${this.size}")
    }
    val crc = CRC32().also {
        it.update(this)
    }
    if (crc.value != checksum) {
        throw ValidateException("checksums do not match: expected $checksum; got ${crc.value}")
    }
}

internal fun File.validate(size: Long, checksum: Long) {
    if (!this.exists()) {
        throw ValidateException("file '${this.name}' does not exist")
    }
    try {
        this.readBytes().validate(size, checksum)
    } catch (e: ValidateException) {
        throw ValidateException("file '${this.name}'", e)
    }
}

internal fun File.validates(size: Long, checksum: Long): Boolean {
    try {
        this.validate(size, checksum)
    } catch (e: ValidateException) {
        return false
    }
    return true
}
