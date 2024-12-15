package udiskmanager

import browser.models.VirUdisk
import logger
import utils.linkDir
import utils.linkFile
import java.io.File
import javax.swing.filechooser.FileSystemView

object UsbUtils {
    private fun getDiskSerialNumberByFile(devicePath: File): String {
        try {
            val uuidFile = devicePath.linkDir("System Volume Information").linkFile("IndexerVolumeGuid")
            return filterAlphanumeric(uuidFile.readText().replace("{", "").replace("}", "").replace("-", "").trim())
        } catch (e: Throwable) {
            logger.error("严重错误，无法获取到u盘序列号", e)
            throw e
        }
    }
    private fun filterAlphanumeric(input: String): String {
        // 正则表达式，匹配任何非字母和数字的字符
        val regex = Regex("[^a-zA-Z0-9]")

        // 使用正则表达式过滤掉非字母数字字符
        val filtered = input.replace(regex, "")

        return filtered
    }
//    @Deprecated("Use getDiskSerialNumberByFile instead")
//    fun getDiskSerialNumber(devicePath: File): String? {
//        val command = when (getSystemType()) {
//            SystemType.Win -> "wmic path Win32_PhysicalMedia where DriveType=2 get SerialNumber"
//            SystemType.Linux -> "blkid /dev/${devicePath.absolutePath} | grep UUID"
//            SystemType.Mac -> TODO()
//        }
//        val processBuilder = ProcessBuilder(command.split(" "))
//        processBuilder.redirectErrorStream(true)
//
//        val process = processBuilder.start()
//        val reader = BufferedReader(InputStreamReader(process.inputStream))
//
//        var line: String?
//        while (reader.readLine().also { line = it } != null) {
//            if (line!!.contains("SerialNumber")) continue
//            if (line!!.contains("UUID")) continue
//            return line
//        }
//        return null
//    }

    fun getUsb(usbRootFile: File): VirUdisk {
        val name = FileSystemView.getFileSystemView().getSystemDisplayName(usbRootFile)
        val totalSize = usbRootFile.totalSpace
        val freeSize = usbRootFile.freeSpace
        val usbId = getUsbId(usbRootFile)
        return VirUdisk(name, totalSize, freeSize, usbId)
    }

    /**
     * 使用U盘根目录获取UsbId
     */
    private fun getUsbId(rootFile: File): String {
        logger.debug("获取U盘ID[${rootFile.absolutePath}][UUIDFile]: ${getDiskSerialNumberByFile(rootFile)}")
        return getDiskSerialNumberByFile(rootFile)
//        val f = File(rootFile, ".udtid")
//        if (!f.exists()) {
//            f.createNewFile()
//            f.writeText(System.nanoTime().toString())
//            f.hidden()
//        }
//        var usbId = f.readText()
//        if (usbId.isEmpty()) {
//            usbId = System.nanoTime().toString()
//            f.writeText(usbId)
//        }
//        return usbId
    }
}