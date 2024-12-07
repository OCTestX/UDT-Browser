package utils

import java.io.File
import java.io.InputStream
import java.io.OutputStream

fun File.transferFilePartTo(targetFile: File, startOffset: Long = 0): Long {
    if (length() < startOffset) throw IllegalStateException("传输文件字节数超出源文件: $this[${length() < startOffset}]")
    if (length() == startOffset) return 0
    val output = if (startOffset == 0L) targetFile.outputStream() else targetFile.appendOutputStream()
    val input = inputStream()
    if (startOffset != 0L) input.skip(startOffset)
    val blockSize = 1024
    val buffer = ByteArray(blockSize)
    var bytesRead: Int
    var transfered = 0L
    while (input.read(buffer).also { bytesRead = it } != -1) {
        output.write(buffer, 0, bytesRead)
        transfered += bytesRead
    }
    input.close()
    output.close()
    return transfered
}

fun File.autoTransferTo(out: File, speedLimit: Long = Long.MAX_VALUE, append: Boolean = false) {
    if (!isFile) {
        throw Exception("仅支持复制文件: $this -> $out")
    }
    if (!out.exists()) {
        out.parentFile.mkdirs()
        out.createNewFile()
    } else if (!out.isFile) {
        throw Exception("仅支持复制文件: $this -> $out")
    }
    if (append) {
        if (length() <= out.length()) {
            return
        }
        inputStream().autoTransferTo(out.appendOutputStream(), out.length(), speedLimit)
    } else inputStream().autoTransferTo(out.outputStream(), speedLimit = speedLimit)
}
fun InputStream.autoTransferTo(out: OutputStream, startOffset: Long = 0L, speedLimit: Long = Long.MAX_VALUE) {
    try {
        if (startOffset > 0L) {
            skip(startOffset)
        }
        RateLimitInputStream(this, speedLimit).transferTo(out)
    } finally {
        close()
        out.close()
    }
}
fun InputStream.autoTransferTo(out: OutputStream, startOffset: Long = 0L, speedLimit: Long = Long.MAX_VALUE, bufferSize: Int = 1024 * 1024, progress: (copied: Long, current: Int) -> Unit) {
    try {
        if (startOffset > 0L) {
            skip(startOffset)
        }
        val input = RateLimitInputStream(this, speedLimit)
        val buffer = ByteArray(bufferSize)
        var transferred: Long = 0
        var read: Int
        while ((input.read(buffer, 0, bufferSize).also { read = it }) >= 0) {
            out.write(buffer, 0, read)
            val current = read
            transferred += current
            progress(transferred, current)
        }
    } finally {
        close()
        out.close()
    }
}

class SpeedCalculator() {
    private val initialTime = System.currentTimeMillis()
    private var lastTime: Long = 0
    private var lastBytes: Long = 0
    private var speed: Long = 0
    fun calculate(bytes: Long): Long {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastTime
        if (timeDiff == 0L) return 0
        val bytesDiff = bytes - lastBytes
        val speed = bytesDiff * 1000 / timeDiff
        this.speed = speed
        this.lastTime = currentTime
        this.lastBytes = bytes
        return speed
    }
    fun getUsedTime(): Long = System.currentTimeMillis() - initialTime
}