@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package org.opensentry.installsentry

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var refreshCounter by mutableIntStateOf(0)
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
                var showSystem by remember { mutableStateOf(true) }
                var loading by remember { mutableStateOf(true) }

                LaunchedEffect(refreshCounter) {
                    loading = true
                    apps = withContext(Dispatchers.IO) {
                            runCatching {
                                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                                    .mapNotNull { loadAppInfo(this@MainActivity, it.packageName) }
                                    .sortedBy { it.label.lowercase() }
                            }.getOrDefault(emptyList())
                    }
                    loading = false
                }

                val visibleApps = apps.filter {
                    (showSystem || !it.isSystem) &&
                        (it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true))
                }
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Column {
                                        Text("InstallSentry", fontWeight = FontWeight.Bold)
                                        Text("DEVICE APP MONITOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { refreshCounter++ }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh app list")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                            Text("Your apps, at a glance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            Text("Local app inventory and install alerts. Your data stays on this device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .82f))
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                                Text("OFFLINE-FIRST", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .9f))
                                    }
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatCard("VISIBLE APPS", apps.size.toString(), Modifier.weight(1f))
                                    StatCard("USER APPS", apps.count { !it.isSystem }.toString(), Modifier.weight(1f))
                                    StatCard("SYSTEM", apps.count { it.isSystem }.toString(), Modifier.weight(1f))
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Search apps or package names") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("Clear") } },
                                    singleLine = true,
                                    shape = RoundedCornerShape(18.dp)
                                )
                            }
                            item {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Installed applications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("${visibleApps.size} shown", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    FilterChip(
                                        selected = !showSystem,
                                        onClick = { showSystem = !showSystem },
                                        label = { Text(if (showSystem) "All apps" else "User apps") },
                                        leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                }
                            }
                            if (loading) {
                                item { Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                            } else if (visibleApps.isEmpty()) {
                                item {
                                    Card(shape = RoundedCornerShape(20.dp)) {
                                        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(38.dp), tint = MaterialTheme.colorScheme.primary)
                                            Text(if (apps.isEmpty()) "No apps could be loaded" else "No matching apps", fontWeight = FontWeight.SemiBold)
                                            Text(if (apps.isEmpty()) "Tap refresh to try again." else "Try another search or show system apps.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            } else {
                                items(visibleApps, key = { it.packageName }) { app ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            startActivity(Intent(this@MainActivity, OverviewActivity::class.java).putExtra(OverviewActivity.EXTRA_PACKAGE, app.packageName))
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                                    ) {
                                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                                            AppIcon(app.packageName, Modifier.size(48.dp))
                                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text(app.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text("v${app.versionName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(app.sizeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            if (app.isSystem) Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                                Text("SYS", modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
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

    override fun onResume() {
        super.onResume()
        refreshCounter++
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(14.dp)),
        factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
        update = { view ->
            runCatching { view.setImageDrawable(view.context.packageManager.getApplicationIcon(packageName)) }
                .onFailure { view.setImageResource(android.R.drawable.sym_def_app_icon) }
        }
    )
}

@Composable
fun InstallSentryTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = Color(0xFF9BD5FF),
        onPrimary = Color(0xFF00344D),
        primaryContainer = Color(0xFF12394F),
        onPrimaryContainer = Color(0xFFD0E9FF),
        secondary = Color(0xFFB9CAD9),
        background = Color(0xFF0C1116),
        surface = Color(0xFF141B21),
        surfaceContainer = Color(0xFF1B242B),
        surfaceContainerLow = Color(0xFF151D23),
        secondaryContainer = Color(0xFF2A3944)
    )
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}
