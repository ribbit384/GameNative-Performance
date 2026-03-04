package app.gamenative.ui.screen.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import app.gamenative.R
import app.gamenative.utils.Net
import app.gamenative.service.SteamService
import com.winlator.contents.ContentProfile
import com.winlator.contents.ContentsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import okhttp3.Request
import java.io.File
import timber.log.Timber
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.Json

private enum class ContentPane { INSTALLED, DOWNLOAD }

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContentsManagerDialog(open: Boolean, onDismiss: () -> Unit) {
    if (!open) return

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val mgr = remember(ctx) { ContentsManager(ctx) }

    var selectedPane by rememberSaveable { mutableStateOf(ContentPane.INSTALLED) }
    val sidebarScrollState = rememberScrollState()

    var isBusy by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var mtrContents by remember { mutableStateOf<List<ContentProfile>>(emptyList()) }
    var isLoadingMtr by remember { mutableStateOf(false) }

    // Installed list state
    var currentType by remember { mutableStateOf(ContentProfile.ContentType.CONTENT_TYPE_DXVK) }
    val installedProfiles = remember { mutableStateListOf<ContentProfile>() }
    var typeExpanded by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<ContentProfile?>(null) }

    val refreshInstalled: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            try { mgr.syncContents() } catch (_: Exception) {}
            val list = mgr.getProfiles(currentType)
            withContext(Dispatchers.Main) {
                installedProfiles.clear()
                if (list != null) installedProfiles.addAll(list.filter { it.remoteUrl == null })
            }
        }
    }

    LaunchedEffect(currentType) {
        refreshInstalled()
    }

    // Load MTR contents
    LaunchedEffect(selectedPane) {
        if (selectedPane == ContentPane.DOWNLOAD && mtrContents.isEmpty()) {
            isLoadingMtr = true
            scope.launch(Dispatchers.IO) {
                try {
                    val url = "https://raw.githubusercontent.com/maxjivi05/Components/main/contents.json"
                    val request = Request.Builder().url(url).build()
                    Net.http.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val jsonString = response.body?.string() ?: "[]"
                            val jsonArray = Json.parseToJsonElement(jsonString).jsonArray
                            val profiles = jsonArray.map { 
                                val obj = it.jsonObject
                                val typeStr = obj["type"]!!.toString().trim('"')
                                val type = when(typeStr.lowercase()) {
                                    "dxvk" -> ContentProfile.ContentType.CONTENT_TYPE_DXVK
                                    "vkd3d" -> ContentProfile.ContentType.CONTENT_TYPE_VKD3D
                                    "box64" -> ContentProfile.ContentType.CONTENT_TYPE_BOX64
                                    "wowbox64" -> ContentProfile.ContentType.CONTENT_TYPE_WOWBOX64
                                    "fexcore" -> ContentProfile.ContentType.CONTENT_TYPE_FEXCORE
                                    else -> ContentProfile.ContentType.CONTENT_TYPE_WINE
                                }
                                ContentProfile().apply {
                                    this.type = type
                                    this.verName = obj["verName"]!!.toString().trim('"')
                                    this.verCode = obj["verCode"]!!.toString().trim('"').toInt()
                                    this.remoteUrl = obj["remoteUrl"]!!.toString().trim('"')
                                }
                            }
                            withContext(Dispatchers.Main) { mtrContents = profiles }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "ContentsManagerDialog: Error loading MTR contents")
                } finally {
                    isLoadingMtr = false
                }
            }
        }
    }

    val downloadAndInstallContent = { profile: ContentProfile ->
        scope.launch {
            isBusy = true
            statusMessage = "Downloading..."
            try {
                val fileName = profile.remoteUrl!!.substringAfterLast('/')
                val destFile = File(ctx.cacheDir, fileName)
                
                withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(profile.remoteUrl!!).build()
                    Net.http.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw java.io.IOException("Unexpected code $response")
                        val body = response.body ?: throw java.io.IOException("Empty body")
                        body.byteStream().use { inp ->
                            java.io.FileOutputStream(destFile).use { out ->
                                inp.copyTo(out)
                            }
                        }
                    }
                }

                statusMessage = "Validating..."
                val uri = Uri.fromFile(destFile)
                val result = withContext(Dispatchers.IO) {
                    var outProfile: ContentProfile? = null
                    var fail: ContentsManager.InstallFailedReason? = null
                    val latch = CountDownLatch(1)
                    mgr.extraContentFile(uri, object : ContentsManager.OnInstallFinishedCallback {
                        override fun onFailed(reason: ContentsManager.InstallFailedReason, e: Exception) { fail = reason; latch.countDown() }
                        override fun onSucceed(p: ContentProfile) { outProfile = p; latch.countDown() }
                    })
                    latch.await()
                    outProfile to fail
                }

                val (extractedProfile, fail) = result
                if (extractedProfile != null) {
                    performFinishInstall(ctx, mgr, extractedProfile) {
                        refreshInstalled()
                        statusMessage = null
                        isBusy = false
                    }
                } else {
                    Toast.makeText(ctx, "Install failed: $fail", Toast.LENGTH_SHORT).show()
                    isBusy = false
                }
                destFile.delete()
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                isBusy = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            isBusy = true
            statusMessage = "Validating..."
            val result = withContext(Dispatchers.IO) {
                var profile: ContentProfile? = null
                var latch = CountDownLatch(1)
                mgr.extraContentFile(uri, object : ContentsManager.OnInstallFinishedCallback {
                    override fun onFailed(r: ContentsManager.InstallFailedReason, e: Exception) { latch.countDown() }
                    override fun onSucceed(p: ContentProfile) { profile = p; latch.countDown() }
                })
                latch.await()
                profile
            }

            if (result != null) {
                performFinishInstall(ctx, mgr, result) {
                    currentType = result.type
                    refreshInstalled()
                    isBusy = false
                }
            } else {
                Toast.makeText(ctx, "Import failed", Toast.LENGTH_SHORT).show()
                isBusy = false
            }
        }
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
                            Text("Content Manager", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    IconButton(
                        onClick = { importLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(8.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.FileUpload, "Import .wcp", tint = Color.White)
                    }
                }
            }

            // CONTENT
            Row(modifier = Modifier.fillMaxSize().padding(top = 72.dp)) {
                // SIDEBAR
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .verticalScroll(sidebarScrollState)
                        .padding(start = 24.dp, end = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SidebarItem(label = "Installed", icon = Icons.Default.Inventory, selected = selectedPane == ContentPane.INSTALLED, onClick = { selectedPane = ContentPane.INSTALLED })
                    SidebarItem(label = "Download", icon = Icons.Default.CloudDownload, selected = selectedPane == ContentPane.DOWNLOAD, onClick = { selectedPane = ContentPane.DOWNLOAD })
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
                        targetState = selectedPane,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
                    ) { pane ->
                        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                            when (pane) {
                                ContentPane.INSTALLED -> {
                                    Text("Installed Content", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(16.dp))
                                    
                                    // Type Selector
                                    Box {
                                        Surface(
                                            onClick = { typeExpanded = true },
                                            modifier = Modifier.fillMaxWidth().height(56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = 0.05f),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(currentType.toString(), color = Color.White, modifier = Modifier.weight(1f))
                                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                                            }
                                        }
                                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                            listOf(ContentProfile.ContentType.CONTENT_TYPE_DXVK, ContentProfile.ContentType.CONTENT_TYPE_VKD3D, ContentProfile.ContentType.CONTENT_TYPE_BOX64, ContentProfile.ContentType.CONTENT_TYPE_WOWBOX64, ContentProfile.ContentType.CONTENT_TYPE_FEXCORE, ContentProfile.ContentType.CONTENT_TYPE_WINE).forEach { t ->
                                                DropdownMenuItem(text = { Text(t.toString()) }, onClick = { currentType = t; typeExpanded = false })
                                            }
                                        }
                                    }
                                    
                                    Spacer(Modifier.height(16.dp))
                                    
                                    if (installedProfiles.isEmpty()) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("No content installed for this type", color = Color.White.copy(alpha = 0.5f))
                                        }
                                    } else {
                                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(installedProfiles) { p ->
                                                ContentTile(
                                                    title = p.verName,
                                                    subtitle = "Code: ${p.verCode}${if (!p.desc.isNullOrEmpty()) " - ${p.desc}" else ""}",
                                                    onDelete = { deleteTarget = p }
                                                )
                                            }
                                        }
                                    }
                                }
                                ContentPane.DOWNLOAD -> {
                                    Text("Download Components", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(16.dp))
                                    if (isLoadingMtr) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                    } else {
                                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(mtrContents) { p ->
                                                val isInstalled = installedProfiles.any { it.verName == p.verName && it.verCode >= p.verCode }
                                                val canUpgrade = installedProfiles.any { it.verName == p.verName && it.verCode < p.verCode }
                                                
                                                ContentTile(
                                                    title = p.verName,
                                                    subtitle = "${p.type} (v${p.verCode})",
                                                    actionText = if (isInstalled) "Installed" else if (canUpgrade) "Upgrade" else "Install",
                                                    actionEnabled = !isInstalled && !isBusy,
                                                    onAction = { downloadAndInstallContent(p) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Progress
            if (isBusy) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    Surface(color = Color(0xFF1A1A1A), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text(statusMessage ?: "Working...", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = { Text("Remove Content") },
            text = { Text("Are you sure you want to remove ${target.verName}?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) { mgr.removeContent(target) }
                        refreshInstalled()
                        deleteTarget = null
                    }
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = Color.White) } }
        )
    }
}

@Composable
private fun SidebarItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
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
        Icon(icon, null, tint = contentColor, modifier = Modifier.size(24.dp))
        Text(text = label, color = contentColor, fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun ContentTile(
    title: String,
    subtitle: String,
    actionText: String? = null,
    actionEnabled: Boolean = true,
    onAction: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
            
            if (onDelete != null) {
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            }
            
            if (actionText != null && onAction != null) {
                Button(
                    onClick = onAction,
                    enabled = actionEnabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Text(actionText, fontSize = 13.sp)
                }
            }
        }
    }
}

private suspend fun performFinishInstall(
    context: Context,
    mgr: ContentsManager,
    profile: ContentProfile,
    onDone: (String) -> Unit,
) {
    val msg = withContext(Dispatchers.IO) {
        var message = ""
        val latch = CountDownLatch(1)
        try {
            mgr.finishInstallContent(profile, object : ContentsManager.OnInstallFinishedCallback {
                override fun onFailed(reason: ContentsManager.InstallFailedReason, e: Exception) {
                    message = when (reason) {
                        ContentsManager.InstallFailedReason.ERROR_EXIST -> "Content updated successfully"
                        ContentsManager.InstallFailedReason.ERROR_NOSPACE -> "Not enough space"
                        else -> "Failed to install content"
                    }
                    latch.countDown()
                }
                override fun onSucceed(profileArg: ContentProfile) {
                    message = "Content installed successfully"
                    latch.countDown()
                }
            })
        } catch (e: Exception) {
            message = "Installation error: ${e.message}"
            latch.countDown()
        }
        latch.await()
        message
    }
    onDone(msg)
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
