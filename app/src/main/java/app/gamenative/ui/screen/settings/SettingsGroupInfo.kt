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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.gamenative.Constants
import app.gamenative.R
import app.gamenative.ui.component.dialog.LibrariesDialog

@Composable
fun SettingsGroupInfo() {
    val uriHandler = LocalUriHandler.current
    var showLibrariesDialog by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSectionHeader(stringResource(R.string.settings_info_title))

        SettingsTile(
            title = stringResource(R.string.settings_info_source_title),
            subtitle = stringResource(R.string.settings_info_source_subtitle),
            icon = Icons.Default.Code,
            onClick = { uriHandler.openUri(Constants.Misc.GITHUB_LINK) },
        )

        SettingsTile(
            title = stringResource(R.string.settings_info_libraries_title),
            subtitle = stringResource(R.string.settings_info_libraries_subtitle),
            icon = Icons.Default.MenuBook,
            onClick = { showLibrariesDialog = true },
        )

        SettingsTile(
            title = stringResource(R.string.settings_info_privacy_title),
            subtitle = stringResource(R.string.settings_info_privacy_subtitle),
            icon = Icons.Default.PrivacyTip,
            onClick = { uriHandler.openUri(Constants.Misc.PRIVACY_LINK) },
        )
    }

    LibrariesDialog(
        visible = showLibrariesDialog,
        onDismissRequest = { showLibrariesDialog = false },
    )
}
