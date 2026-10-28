package org.opensentry.installsentry

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.security.MessageDigest
import java.text.DateFormat
import java.util.Date

data class AppInfo(
    val label: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val apkBytes: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val installer: String,
    val targetSdk: Int,
    val minSdk: Int?,
    val isSystem: Boolean,
    val apkPath: String,
    val sha256: String,
    val permissions: List<String>
) {
    val sizeLabel: String get() = formatBytes(apkBytes)
    val installDate: String get() = formatDate(firstInstallTime)
    val updateDate: String get() = formatDate(lastUpdateTime)
}

fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "Unknown"
    if (bytes < 1024) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = -1
    do { value /= 1024.0; unit++ } while (value >= 1024 && unit < units.lastIndex)
    return String.format(java.util.Locale.US, "%.2f %s", value, units[unit])
}

fun formatDate(time: Long): String =
    if (time <= 0L) "Unavailable"
    else DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(time))

@Suppress("DEPRECATION")
fun loadAppInfo(context: Context, packageName: String): AppInfo? {
    val pm = context.packageManager
    return try {
        val pi: PackageInfo = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
        } else {
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        }
        val ai = pi.applicationInfo ?: return null
        val label = pm.getApplicationLabel(ai).toString()
        val source = try {
            if (Build.VERSION.SDK_INT >= 30) pm.getInstallSourceInfo(packageName).installingPackageName
            else @Suppress("DEPRECATION") pm.getInstallerPackageName(packageName)
        } catch (_: Exception) { null } ?: "Unknown / unavailable"
        val apk = File(ai.sourceDir)
        val certHash = try {
            val signatures = if (Build.VERSION.SDK_INT >= 28) {
                val flags = PackageManager.GET_SIGNING_CERTIFICATES
                val p = if (Build.VERSION.SDK_INT >= 33)
                    pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
                else pm.getPackageInfo(packageName, flags)
                p.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
            } else {
                val p = if (Build.VERSION.SDK_INT >= 33)
                    pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNATURES.toLong()))
                else pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION") p.signatures?.firstOrNull()?.toByteArray()
            }
            signatures?.let { MessageDigest.getInstance("SHA-256").digest(it).joinToString("") { b -> "%02x".format(b) } }
                ?: "Unavailable"
        } catch (_: Exception) { "Unavailable" }
        val requested = pi.requestedPermissions?.toList().orEmpty()
        val minSdk = try {
            if (Build.VERSION.SDK_INT >= 24) ai.minSdkVersion else null
        } catch (_: Exception) { null }
        AppInfo(
            label = label,
            packageName = packageName,
            versionName = pi.versionName ?: "Unknown",
            versionCode = if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode else @Suppress("DEPRECATION") pi.versionCode.toLong(),
            apkBytes = if (apk.exists()) apk.length() else -1L,
            firstInstallTime = pi.firstInstallTime,
            lastUpdateTime = pi.lastUpdateTime,
            installer = source,
            targetSdk = ai.targetSdkVersion,
            minSdk = minSdk,
            isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
            apkPath = ai.sourceDir ?: "Unavailable",
            sha256 = certHash,
            permissions = requested
        )
    } catch (_: Exception) { null }
}
