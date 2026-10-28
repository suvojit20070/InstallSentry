package org.opensentry.installsentry

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InstallNotifications.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)

        setContent {
            InstallSentryTheme {
                var apps by remember { mutableStateOf(emptyList<AppInfo>()) }
                var query by remember { mutableStateOf("") }
                fun refresh() {
                    apps = try {
                        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                            .mapNotNull { loadAppInfo(this, it.packageName) }
                            .sortedBy { it.label.lowercase() }
                    } catch (_: Exception) { emptyList() }
                }
                LaunchedEffect(Unit) { refresh() }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text("InstallSentry", fontWeight = FontWeight.Bold)
                                    Text("Offline installation monitor", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            actions = {
                                IconButton(onClick = { refresh() }) { Icon(Icons.Default.Settings, contentDescription = "Refresh installed apps") }
                            }
                        )
                    }
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(38.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Your device. Your data.", fontWeight = FontWeight.SemiBold)
                                    Text("No accounts, analytics, or network requests.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Installed apps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${apps.size} apps visible to this app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = query, onValueChange = { query = it },
                            label = { Text("Search name or package") },
                            singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                            items(apps.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }, key = { it.packageName }) { app ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        startActivity(Intent(this@MainActivity, OverviewActivity::class.java).putExtra(OverviewActivity.EXTRA_PACKAGE, app.packageName))
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(app.label, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                            Text("${app.versionName} • ${app.sizeLabel}", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallSentryTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF9AD7FF),
        secondary = androidx.compose.ui.graphics.Color(0xFFB8C9D9),
        background = androidx.compose.ui.graphics.Color(0xFF101418),
        surface = androidx.compose.ui.graphics.Color(0xFF171D22),
        secondaryContainer = androidx.compose.ui.graphics.Color(0xFF263640)
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}
