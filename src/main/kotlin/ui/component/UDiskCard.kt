package ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import browser.AbsProject
import browser.Project
import browser.models.VirUdisk
import compose.icons.TablerIcons
import compose.icons.tablericons.BoxModel
import utils.fileSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UDiskCard(disk: VirUdisk, modifier: Modifier = Modifier, dbProject: AbsProject) {
    Card(
        modifier = Modifier.then(modifier)
    ) {
        Column(Modifier.padding(6.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Icon(TablerIcons.BoxModel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    if (dbProject is Project) {
                        AnimatedContent(dbProject.getCustomUDiskNameState(disk.udiskId)?: disk.name) {
                            Text(it, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                        }
                    } else {
                        Text(disk.name, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                    }
                    Text("${fileSize(disk.totalSize - disk.freeSize)} / ${fileSize(disk.totalSize)}", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                }
                if (dbProject is Project) {
                    val manager = dbProject.uDiskManager
                    if (manager != null) {
                        val action = manager.getUDiskAction(disk)
                        if (action!= null) {
                            ActionLED(action)
                        }
                    }
                }
            }
        }
    }
}