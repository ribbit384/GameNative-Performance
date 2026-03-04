package app.gamenative.ui.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import app.gamenative.R
import app.gamenative.ui.component.dialog.Box64PresetsDialog
import app.gamenative.ui.component.dialog.ContainerConfigDialog
import app.gamenative.ui.component.dialog.FEXCorePresetsDialog
import app.gamenative.ui.component.dialog.LoadingDialog
import app.gamenative.utils.ContainerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PresetsDialog(open: Boolean, onDismiss: () -> Unit) {
    if (!open) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showConfigDialog by rememberSaveable { mutableStateOf(false) }
    var showBox64PresetsDialog by rememberSaveable { mutableStateOf(false) }
    var showFexcorePresetsDialog by rememberSaveable { mutableStateOf(false) }
    var isSavingConfig by rememberSaveable { mutableStateOf(false) }

    val sidebarScrollState = rememberScrollState()
    val scrollState = rememberScrollState()

    if (showConfigDialog) {
        ContainerConfigDialog(
            visible = showConfigDialog,
            title = "Default Settings",
            default = true,
            initialConfig = ContainerUtils.getDefaultContainerData(),
            onDismissRequest = { showConfigDialog = false },
            onSave = { config ->
                isSavingConfig = true
                scope.launch(Dispatchers.IO) {
                    try {
                        ContainerUtils.setDefaultContainerData(config)
                    } finally {
                        isSavingConfig = false
                        showConfigDialog = false
                    }
                }
            },
        )
    }

    LoadingDialog(
        visible = isSavingConfig,
        progress = -1f,
        message = stringResource(R.string.settings_saving_restarting)
    )

    if (showBox64PresetsDialog) {
        Box64PresetsDialog(
            visible = showBox64PresetsDialog,
            onDismissRequest = { showBox64PresetsDialog = false },
        )
    }

    if (showFexcorePresetsDialog) {
        FEXCorePresetsDialog(
            visible = showFexcorePresetsDialog,
            onDismissRequest = { showFexcorePresetsDialog = false },
        )
    }

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
                            Text("Presets Manager", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f))
            }

            // CONTENT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Global Emulation Presets",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                SettingsTile(
                    title = "Default Settings",
                    subtitle = "Configure global container defaults",
                    icon = Icons.Default.Settings,
                    onClick = { showConfigDialog = true }
                )
                Spacer(Modifier.height(12.dp))
                SettingsTile(
                    title = "Box64 Presets",
                    subtitle = "Manage Box64 dynarec profiles",
                    icon = Icons.Default.Terminal,
                    onClick = { showBox64PresetsDialog = true }
                )
                Spacer(Modifier.height(12.dp))
                SettingsTile(
                    title = "FEXCore Presets",
                    subtitle = "Manage FEXCore emulation profiles",
                    icon = Icons.Default.Bolt,
                    onClick = { showFexcorePresetsDialog = true }
                )
            }
        }
    }
}
