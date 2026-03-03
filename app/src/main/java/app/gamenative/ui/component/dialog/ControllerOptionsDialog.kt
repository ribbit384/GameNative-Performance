package app.gamenative.ui.component.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.gamenative.R

@Composable
fun ControllerOptionsDialog(
    onDismiss: () -> Unit,
    onEditOnScreen: () -> Unit,
    onSelectICP: () -> Unit,
    onImportICP: () -> Unit,
    onExportICP: () -> Unit,
    onControllerManager: () -> Unit,
    onMotionControls: () -> Unit,
    onEditPhysicalController: () -> Unit
) {
    val items = listOf(
        ControllerOption(stringResource(R.string.edit_controls), Icons.Default.Edit, onEditOnScreen),
        ControllerOption("Select ICP", Icons.Default.List, onSelectICP),
        ControllerOption("Import ICP", Icons.Default.FileDownload, onImportICP),
        ControllerOption("Export ICP", Icons.Default.FileUpload, onExportICP),
        ControllerOption(stringResource(R.string.controller_manager), Icons.Default.Gamepad, onControllerManager),
        ControllerOption(stringResource(R.string.motion_controls), Icons.Default.ScreenRotation, onMotionControls),
        ControllerOption(stringResource(R.string.edit_physical_controller), Icons.Default.SettingsInputComponent, onEditPhysicalController)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.button_back),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Controller",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Options Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        ControllerOptionItem(item)
                    }
                }

                // Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@Composable
private fun ControllerOptionItem(item: ControllerOption) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = item.onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isFocused) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            width = 2.dp,
            color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class ControllerOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
