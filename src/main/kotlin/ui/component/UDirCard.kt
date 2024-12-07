package ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import browser.DBDataProvider
import browser.models.VirDir
import compose.icons.TablerIcons
import compose.icons.tablericons.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ui.utils.Animates
import utils.fileSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UDirCard(dir: VirDir, modifier: Modifier = Modifier, dbDataProvider: DBDataProvider) {
    Card(
        modifier = Modifier.then(modifier)
    ) {
        Column(Modifier.padding(6.dp)) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                Icon(TablerIcons.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(dir.name, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }

            var countingSize by rememberSaveable { mutableStateOf(true) }
            val size by produceState(0L) {
                withContext(Dispatchers.IO) {
                    dbDataProvider.deepCountDirSize(dir.udiskId, dir.dirId) {
                        value = it
                        delay(WorkDir.globalServiceConfig.dirSizeEachCountAnimateDelay.value)
                    }
                    countingSize = false
                }
            }
            Animates.VisibilityAnimates {
                Column {
                    Text(fileSize(size))
                    AnimatedVisibility(countingSize) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}