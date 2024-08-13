package browser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import logger
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.support.sqlite.SQLiteDialect
import java.io.File

class DBFile(val file: File) {
    private val ioScope = CoroutineScope(Dispatchers.IO)
    val db = Database.connect(
        "jdbc:sqlite:${file.absolutePath.apply { logger.info("loginSql: $this") }}",
        dialect = SQLiteDialect()
    )
}