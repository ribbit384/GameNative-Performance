package app.gamenative.ui.screen.settings

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.gamenative.PrefManager
import app.gamenative.R
import app.gamenative.enums.AppTheme
import app.gamenative.ui.theme.PluviaTheme
import com.materialkolor.PaletteStyle

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsScreen(
    appTheme: AppTheme,
    paletteStyle: PaletteStyle,
    onAppTheme: (AppTheme) -> Unit,
    onPaletteStyle: (PaletteStyle) -> Unit,
    onBack: () -> Unit,
    onNavigateToSteamLogin: () -> Unit = {},
) {
    var selectedSection by rememberSaveable { mutableStateOf(SettingsSection.INTERFACE) }
    val scrollState = rememberScrollState()
    val sidebarScrollState = rememberScrollState()

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
            // Left Section (Back Button)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(8.dp, CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            }

            // Center Section (Title)
            Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.height(44.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                ) {
                    Box(modifier = Modifier.padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.settings_title),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right Section (Empty for symmetry)
            Box(modifier = Modifier.weight(1f))
        }

        // MAIN CONTENT (SIDEBAR + CONTENT)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp)
        ) {
            // SIDEBAR
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .verticalScroll(sidebarScrollState)
                    .padding(start = 24.dp, end = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsSection.entries.forEach { section ->
                    SidebarItem(
                        label = section.label,
                        icon = section.icon,
                        selected = selectedSection == section,
                        onClick = { selectedSection = section }
                    )
                }
            }

            // CONTENT AREA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 12.dp, end = 24.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            ) {
                AnimatedContent(
                    targetState = selectedSection,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))) with
                                    (slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)))
                        } else {
                            (slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))) with
                                    (slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)))
                        }
                    }
                ) { section ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = section.label,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        when (section) {
                            SettingsSection.INTERFACE -> SettingsGroupInterface(
                                appTheme = appTheme,
                                paletteStyle = paletteStyle,
                                onAppTheme = onAppTheme,
                                onPaletteStyle = onPaletteStyle,
                                onNavigateToSteamLogin = onNavigateToSteamLogin,
                            )
                            SettingsSection.EMULATION -> SettingsGroupEmulation()
                            SettingsSection.ADVANCED -> SettingsGroupDebug()
                            SettingsSection.INFO -> SettingsGroupInfo()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
    val borderColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private enum class SettingsSection(val label: String, val icon: ImageVector) {
    INTERFACE("Interface", Icons.Default.Palette),
    EMULATION("Emulation", Icons.Default.SettingsInputComponent),
    ADVANCED("Advanced", Icons.Default.Code),
    INFO("Info & Debug", Icons.Default.Info)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, widthDp = 800, heightDp = 480)
@Composable
private fun Preview_SettingsScreen() {
    val context = LocalContext.current
    PrefManager.init(context)
    PluviaTheme {
        SettingsScreen(
            appTheme = AppTheme.DAY,
            paletteStyle = PaletteStyle.TonalSpot,
            onAppTheme = { },
            onPaletteStyle = { },
            onBack = { },
            onNavigateToSteamLogin = { },
        )
    }
}
