package utils

import WorkDir
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.IllegalStateException
import java.nio.file.Files

fun File.cloneFile(): File {
    return requestTempFile(name)
        .apply { autoTransferTo(this) }
}
fun requestTempFile(name: String = ""): File {
    return File(WorkDir.globalServiceConfig.tempDir, getOnlyId() +
            if (name.isNotEmpty()) {
                "_$name"
            } else ""
    )
}

fun File.linkDir(name: String): File = File(this, name).apply { if (exists() && isDirectory.not()) throw IllegalStateException("$this 不是文件夹") }
fun File.linkFile(name: String): File = File(this, name).apply { if (exists() && isFile.not()) throw IllegalStateException("$this 不是文件") }
fun File.link(name: String): File = File(this, name)
fun File.ifNotExits(block: (File) -> Unit): File {
    if (exists().not()) {
        block(this)
    }
    return this
}
fun File.listDirectory(): List<File> {
    return listFiles()?.filter { it.isDirectory }?: listOf()
}
fun File.mustFile(): File = apply { createNewFile() }
fun File.mustDir(): File = apply { mkdirs() }
fun File.rename(newName: String): File {
    val file = File(parentFile, newName)
    renameTo(file)
    return file
}
fun File.hidden() {
    if (System.getProperty("os.name").lowercase().contains("win")) {
//        val attributes = Files.readAttributes(toPath(), DosFileAttributes::class.java)
        Files.setAttribute(toPath(), "dos:hidden", true)
        println("隐藏文件创建成功")
    } else {
//        val attributes = Files.readAttributes(toPath(), PosixFileAttributes::class.java)
        Files.setAttribute(toPath(), "unix:hidden", true)
        println("隐藏文件创建成功")
    }
}
//fun composeFilePath(parentPath: String, childName: String): String {
//    return if (parentPath.isEmpty()) childName else "$parentPath/$childName"
//}
//fun File.toDBPath(): String {
//    return absolutePath.toDBPath()
//}
//fun String.toDBPath(): String {
//    return removePrefix("${this[0]}:\\").enbase64
//}
//
//fun File.streamFilePart(tag: String, skip: Long): MultipartBody.Part {
//    return inputStream().streamFilePart(tag, name, length(), skip)
//}
//
//fun InputStream.streamFilePart(tag: String, name: String, size: Long, skip: Long): MultipartBody.Part {
//    skip(skip)
//    val requestFile = object : RequestBody() {
//        override fun contentType() = "application/octet-stream".toMediaType()
//
//        override fun contentLength() = size
//
//        override fun writeTo(sink: BufferedSink) {
//            source().use { source -> sink.writeAll(source) }
//        }
//    }
//    return MultipartBody.Part.createFormData(tag, name, requestFile)
//}
val Long.fileSize: String get() = fileSize(this)
fun fileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun File(file: File, vararg path: String): File {
    return File(file, path.joinToString(File.separator))
}

fun File.appendOutputStream(): OutputStream = FileOutputStream(this, true)