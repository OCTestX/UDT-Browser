package browser

import browser.models.VirDir
import browser.models.VirFile
import browser.models.VirUdisk
import logger
import org.ktorm.dsl.*
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

class DBDataProvider(private val dbFile: DBFile) {
    fun getAllUDisk(): List<VirUdisk> {
        logger.debug { "Getting all UDisk" }
        return dbFile.db.from(UsbsTable).select().map {
            val id = it[UsbsTable.usbId]!!
            getUDisk(id)
        }
    }

    fun getUDisk(usbId: String): VirUdisk {
        logger.debug { "Getting UDisk with id $usbId" }
        return dbFile.db.from(UsbsTable).select().where {
            UsbsTable.usbId eq usbId
        }.map {
            VirUdisk(
                it[UsbsTable.name]!!,
                it[UsbsTable.totalSize]!!,
                it[UsbsTable.freeSize]!!,
                it[UsbsTable.usbId]!!
            ).apply {
                logger.info("UDisk: $this")
            }
        }.first()
    }

    fun getRootDir(usbId: String): VirDir {
        logger.debug { "Getting root directory of UDisk with id $usbId" }
        return dbFile.db.from(DirsTable).select().where {
            (DirsTable.usbId eq usbId) and (DirsTable.parentDirId eq "DIR-ID-Root")
        }.map {
            VirDir(
                it[DirsTable.path]!!.split("/").last(),
                null,
                usbId,
                it[DirsTable.path]!!,
                it[DirsTable.dirId]!!,
            )
        }.first()
    }

    fun searchFiles(usbId: String?, keyword: String): List<VirFile> {
        return if (usbId != null) {
            logger.debug { "Searched files with keyword $keyword of UDisk with id $usbId" }
            val table = UsbFilesTable(usbId)
            dbFile.db.from(table).select().where {
                table.fpath like "%$keyword%"
            }.map {
                VirFile(
                    it[table.fpath]!!.split("/").last(),
                    it[table.parentDirId]!!,
                    usbId,
                    it[table.size]!!,
                    it[table.fileId]!!,
                )
            }
        } else {
            logger.debug { "Searched files with keyword $keyword of all UDisk" }
            val searchedFiles = mutableListOf<VirFile>()
            for (disk in getAllUDisk()) {
                searchedFiles.addAll(searchFiles(disk.udiskId, keyword))
            }
            return searchedFiles
        }
    }

    fun getDirFiles(usbId: String, dirId: String): List<VirFile> {
        logger.debug { "Getting files of directory with id $dirId of UDisk with id $usbId" }
        val table = UsbFilesTable(usbId)
        return dbFile.db.from(table).select().where {
            table.parentDirId eq dirId
        }.map {
            VirFile(
                it[table.fpath]!!.split("/").last(),
                dirId,
                usbId,
                it[table.size]!!,
                it[table.fileId]!!,
            )
        }
    }

    fun getDirDirs(usbId: String, dirId: String): List<VirDir> {
        logger.debug { "Getting directories of directory with id $dirId of UDisk with id $usbId" }
        return dbFile.db.from(DirsTable).select().where {
            DirsTable.parentDirId eq dirId
        }.map {
            VirDir(
                it[DirsTable.path]!!.split("/").last(),
                it[DirsTable.parentDirId]!!,
                usbId,
                it[DirsTable.path]!!,
                it[DirsTable.dirId]!!,
            )
        }
    }

    fun getDir(usbId: String, dirId: String): VirDir {
        logger.debug { "Getting directory with id $dirId of UDisk with id $usbId" }
        return dbFile.db.from(DirsTable).select().where {
            DirsTable.dirId eq dirId
        }.map {
            VirDir(
                it[DirsTable.path]!!.split("/").last(),
                it[DirsTable.parentDirId].run { if (this == "DIR-ID-Root") null else this },
                usbId,
                it[DirsTable.path]!!,
                it[DirsTable.dirId]!!,
            )
        }.first()
    }

    private val dirSizeCache = mutableMapOf<String, Long>()
    suspend fun deepCountDirSize(udiskId: String, dirId: String, useCache: Boolean = true, publishingSize: suspend (Long) -> Unit,) {
        if (useCache) {
            val cacheData = dirSizeCache[dirId]
            if (cacheData != null) {
                publishingSize(cacheData)
                return
            }
        }
        var currentSize = 0L
        val files = getDirFiles(udiskId, dirId)
        for (file in files) {
            currentSize += file.size
            publishingSize(currentSize)
        }
        val dirs = getDirDirs(udiskId, dirId)
        for (dir in dirs) {
            deepCountDirSizeImpl(
                udiskId,
                dir.dirId,
                currentSize,
                { currentSize = it },
            )
        }
        dirSizeCache[dirId] = currentSize
    }
    private suspend fun deepCountDirSizeImpl(udiskId: String, dirId: String, initialSize: Long = 0, publishingSize: suspend (Long) -> Unit,) {
        var currentSize = initialSize
        val files = getDirFiles(udiskId, dirId)
        for (file in files) {
            currentSize += file.size
            publishingSize(currentSize)
        }
        val dirs = getDirDirs(udiskId, dirId)
        for (dir in dirs) {
            deepCountDirSizeImpl(
                udiskId,
                dir.dirId,
                currentSize,
                { currentSize = it },
            )
        }
    }


    


    class UsbFilesTable(usbId: String): Table<Nothing>(usbId) {
        //注意，这里是经过xbase64编码过的路径，解码后的路径以/分割
        val fpath = varchar("fpath")
        val fileId = varchar("fileId")
        val size = long("size")
        val createTime = long("createTime")
        val parentDirId = varchar("parentDirId")
    }
    object UsbsTable: Table<Nothing>("USB") {
        val name = varchar("name")
        val totalSize = long("totalSize")
        val freeSize = long("freeSize")
        val usbId = varchar("usbId")
        val popCount = int("popCount")
    }
    object DirsTable: Table<Nothing>("Dirs") {
        val usbId = varchar("usbId")
        val path = varchar("path")
        val dirId = varchar("dirId")
        val parentDirId = varchar("parentDirId")
    }
}