package app.gamenative.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.gamenative.R
import app.gamenative.ui.component.dialog.OrientationDialog

@Composable
fun SettingsGroupEmulation() {
    var showOrientationDialog by rememberSaveable { mutableStateOf(false) }
    var showPresetsDialog by rememberSaveable { mutableStateOf(false) }
    var showComponentsManager by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSectionHeader(stringResource(R.string.settings_emulation_title))

        SettingsTile(
            title = stringResource(R.string.settings_emulation_components_title),
            subtitle = stringResource(R.string.settings_emulation_components_subtitle),
            icon = Icons.Default.Extension,
            onClick = { showComponentsManager = true },
        )

        SettingsTile(
            title = "Presets",
            subtitle = "Manage Default Settings, Box64, and FEXCore presets",
            icon = Icons.Default.Tune,
            onClick = { showPresetsDialog = true },
        )

        SettingsTile(
            title = stringResource(R.string.settings_emulation_orientations_title),
            subtitle = stringResource(R.string.settings_emulation_orientations_subtitle),
            icon = Icons.Default.ScreenRotation,
            onClick = { showOrientationDialog = true },
        )
    }

    // Dialogs
    OrientationDialog(
        openDialog = showOrientationDialog,
        onDismiss = { showOrientationDialog = false },
    )

    if (showPresetsDialog) {
        PresetsDialog(
            open = showPresetsDialog,
            onDismiss = { showPresetsDialog = false },
        )
    }

    if (showComponentsManager) {
        app.gamenative.ui.screen.settings.ComponentsManagerDialog(
            open = showComponentsManager,
            onDismiss = { showComponentsManager = false },
        )
    }
}
