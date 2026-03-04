package app.gamenative.ui.component.dialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import app.gamenative.ui.screen.settings.SettingsTile

@Composable
fun WineDebugChannelsDialog(
    openDialog: Boolean,
    allChannels: List<String>,
    currentSelection: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    if (!openDialog) return

    var selectedChannels by remember(currentSelection) { mutableStateOf(currentSelection.toSet()) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(8.dp, CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }

                Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.height(44.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                            Text("Wine Debug Channels", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    IconButton(
                        onClick = { onSave(selectedChannels.toList()) },
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(8.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Save, "Save", tint = Color.White)
                    }
                }
            }

            // CONTENT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allChannels) { channel ->
                        val isSelected = selectedChannels.contains(channel)
                        SettingsTile(
                            title = channel,
                            trailing = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        selectedChannels = if (it) selectedChannels + channel else selectedChannels - channel
                                    }
                                )
                            },
                            onClick = {
                                selectedChannels = if (!isSelected) selectedChannels + channel else selectedChannels - channel
                            }
                        )
                    }
                }
            }
        }
    }
}
