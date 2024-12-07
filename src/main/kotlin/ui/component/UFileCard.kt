package ui.component

import androidx.compose.animation.AnimatedContent
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
import browser.models.VirFile
import compose.icons.TablerIcons
import compose.icons.tablericons.File
import logger
import toast
import ui.utils.Animates
import utils.fileSize
import java.io.IOException
import kotlin.jvm.Throws

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UFileCard(file: VirFile, modifier: Modifier = Modifier, dbProject: AbsProject) {
    Card(
        modifier = Modifier.then(modifier)
    ) {
        Column(Modifier.padding(6.dp)) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                Icon(file, dbProject)
                Column(Modifier.weight(1f)) {
                    Text(file.name, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }
            Animates.VisibilityAnimates {
                Text(fileSize(file.size))
            }
        }
    }
}

@Composable
private fun Icon(file: VirFile, dbProject: AbsProject) {
    var painter: Painter? by rememberSaveable(file, dbProject) {
        mutableStateOf(null)
    }
    LaunchedEffect(file, dbProject) {
        try {
            if (file.name.endsWith(".png") || file.name.endsWith(".jpg") || file.name.endsWith(".jpeg")) {
                val input = dbProject.getFileInputStream(file.udiskId, file.fileId)
                val bitmap = loadImageBitmap(input)
                painter = BitmapPainter(bitmap)
            }
        } catch (e: IOException) {
            logger.warn { "加载缩略图失败[WARM]: $file" }
        } catch (e: Throwable) {
            toast.applyShow("加载缩略图失败: $file")
            logger.error { "加载缩略图失败: $file" }
        }
    }
    AnimatedContent(painter) {
        if (it == null) {
            Icon(TablerIcons.File, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        } else {
            Image(it, contentDescription = null)
        }
    }
}