package ui.component

import WorkDir
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import browser.AbsProject
import browser.Project
import browser.models.VirFile
import compose.icons.TablerIcons
import compose.icons.tablericons.CloudDownload
import compose.icons.tablericons.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logger
import toast
import ui.utils.Animates
import utils.fileSize
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UFileCard(file: VirFile, modifier: Modifier = Modifier, dbProject: AbsProject, showPath: Boolean = false) {
    Card(
        modifier = Modifier.then(modifier)
    ) {
        val fileExits = remember(file) { dbProject.isFileLocaled(file.udiskId, file.fileId) }
        Column(Modifier.padding(6.dp)) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                Icon(file, dbProject)
                Column(Modifier.weight(1f)) {
                    Text(file.name, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
                AnimatedVisibility(fileExits.not()) {
                    Icon(TablerIcons.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }
            Animates.VisibilityAnimates {
                Column {
                    Text(fileSize(file.size))
                    if (showPath) {
                        Text((dbProject as Project).dbDataProvider.getFilePath(file), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun Icon(file: VirFile, dbProject: AbsProject) {
    var extraPainter: Painter? by rememberSaveable(file, dbProject) {
        mutableStateOf(null)
    }
    LaunchedEffect(file, dbProject, WorkDir.globalServiceConfig.enableThumbnail.value) {
        try {
            if (WorkDir.globalServiceConfig.enableThumbnail.value && dbProject.isFileLocaled(file.udiskId, file.fileId)) {
                if (file.name.endsWith(".png") || file.name.endsWith(".jpg") || file.name.endsWith(".jpeg")) {
                    val bitmap = withContext(Dispatchers.IO) {
                        val input = dbProject.getFileInputStream(file.udiskId, file.fileId)
                        loadImageBitmap(input)
                    }
                    extraPainter = BitmapPainter(bitmap)
                }
            } else {
                extraPainter = null
            }
        } catch (e: IOException) {
            logger.warn { "加载缩略图失败[WARM]: $file" }
        } catch (e: Throwable) {
            toast.applyShow("加载缩略图失败: $file")
            logger.error { "加载缩略图失败: $file" }
        }
    }
    AnimatedContent(extraPainter) {
        if (it == null) {
            Icon(TablerIcons.File, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        } else {
            Image(it, contentDescription = null)
        }
    }
}