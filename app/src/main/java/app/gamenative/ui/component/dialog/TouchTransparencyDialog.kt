package app.gamenative.ui.component.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.gamenative.PrefManager
import app.gamenative.PluviaApp
import app.gamenative.R
import app.gamenative.events.AndroidEvent

@Composable
fun TouchTransparencyDialog(
    onDismiss: () -> Unit
) {
    var transparency by remember { mutableStateOf(PrefManager.getTouchTransparency()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.touch_transparency),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${(transparency * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = transparency,
                    onValueChange = { 
                        transparency = it
                        PrefManager.setTouchTransparency(it)
                        PluviaApp.events.emitJava(AndroidEvent.TouchTransparencyChanged(it))
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}
