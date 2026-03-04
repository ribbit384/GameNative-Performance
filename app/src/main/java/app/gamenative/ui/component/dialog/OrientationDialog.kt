package app.gamenative.ui.component.dialog

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import app.gamenative.PrefManager
import app.gamenative.R
import app.gamenative.ui.enums.Orientation
import app.gamenative.ui.screen.settings.SettingsTile
import app.gamenative.ui.theme.PluviaTheme
import java.util.EnumSet

@Composable
fun OrientationDialog(
    openDialog: Boolean,
    onDismiss: () -> Unit,
) {
    if (!openDialog) return

    val context = LocalContext.current
    var currentSettings by remember { mutableStateOf(PrefManager.allowedOrientation.toList()) }
    val scrollState = rememberScrollState()

    val onSave: () -> Unit = {
        if (currentSettings.isNotEmpty()) {
            PrefManager.allowedOrientation = EnumSet.copyOf(currentSettings)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { if (currentSettings.isNotEmpty()) onSave() },
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
                        onClick = { if (currentSettings.isNotEmpty()) onSave() },
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
                            Text(stringResource(R.string.allowed_orientations), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    IconButton(
                        onClick = onSave,
                        enabled = currentSettings.isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(8.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = if (currentSettings.isNotEmpty()) 0.6f else 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Check, "Save", tint = Color.White)
                    }
                }
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
                    text = "Select which orientations the application is allowed to use.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Orientation.entries.dropLast(1).forEach { orientation ->
                    val isChecked = currentSettings.contains(orientation)
                    SettingsTile(
                        title = orientation.name,
                        icon = when(orientation) {
                            Orientation.LANDSCAPE, Orientation.REVERSE_LANDSCAPE -> Icons.Default.ScreenLockLandscape
                            Orientation.PORTRAIT, Orientation.REVERSE_PORTRAIT -> Icons.Default.ScreenLockPortrait
                            else -> Icons.Default.ScreenRotation
                        },
                        trailing = {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { enable ->
                                    currentSettings = if (enable) currentSettings + orientation else currentSettings - orientation
                                }
                            )
                        },
                        onClick = {
                            currentSettings = if (!isChecked) currentSettings + orientation else currentSettings - orientation
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, widthDp = 800, heightDp = 480)
@Composable
private fun Preview_OrientationDialog() {
    val context = LocalContext.current
    PrefManager.init(context)
    PluviaTheme {
        OrientationDialog(
            openDialog = true,
            onDismiss = {},
        )
    }
}
