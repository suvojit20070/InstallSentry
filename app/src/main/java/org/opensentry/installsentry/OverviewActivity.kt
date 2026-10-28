package org.opensentry.installsentry

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
class OverviewActivity : ComponentActivity() {
    companion object { const val EXTRA_PACKAGE = "package_name" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        val app = loadAppInfo(this, packageName)
        setContent {
            InstallSentryTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text("App overview") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                        },
                        actions = {
                            if (app != null) IconButton(onClick = {
                                val launch = packageManager.getLaunchIntentForPackage(app.packageName)
                                if (launch != null) startActivity(launch)
                            }) { Icon(Icons.Default.OpenInNew, contentDescription = "Open app") }
                        }
                    )
                }) { padding ->
                    if (app == null) {
                        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Text("App details are no longer available.")
                        }
                    } else {
                        Column(
                            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card {
                                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(app.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    AssistChip(onClick = {}, label = { Text(if (app.isSystem) "System app" else "User-installed app") })
                                }
                            }
                            Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            DetailCard("App size", app.sizeLabel, "Installed base APK size; split APKs and app data may not be included.")
                            DetailCard("Version", "${app.versionName} (${app.versionCode})")
                            DetailCard("Installer source", app.installer, "Android may return an unknown value for some installation methods.")
                            DetailCard("First installed", app.installDate)
                            DetailCard("Last updated", app.updateDate)
                            DetailCard("Target SDK", app.targetSdk.toString())
                            DetailCard("Minimum SDK", app.minSdk?.toString() ?: "Unavailable")
                            DetailCard("APK path", app.apkPath)
                            DetailCard("Signing certificate SHA-256", app.sha256, "This is the signing-certificate fingerprint, not a SHA-256 hash of the APK file.")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    startActivity(Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:${app.packageName}")))
                                }, modifier = Modifier.weight(1f)) { Text("System app settings") }
                                OutlinedButton(onClick = {
                                    val launch = packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (launch != null) startActivity(launch)
                                }, modifier = Modifier.weight(1f)) { Text("Open app") }
                            }
                            Text("Declared permissions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (app.permissions.isEmpty()) {
                                Text("No requested permissions were reported.")
                            } else {
                                app.permissions.forEach { permission -> DetailCard(permission.substringAfterLast('.'), permission) }
                            }
                            Text(
                                "Permissions listed here are declarations, not proof that a permission is currently granted or actively used.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, value: String, note: String? = null) {
    Card {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            SelectionContainerCompat(value)
            if (note != null) {
                Spacer(Modifier.height(4.dp))
                Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SelectionContainerCompat(value: String) {
    androidx.compose.foundation.text.selection.SelectionContainer {
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
