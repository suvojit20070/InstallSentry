package org.opensentry.installsentry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_PACKAGE_ADDED) return

        // PACKAGE_ADDED is also sent during updates; do not report those as new installs.
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return

        val packageName = intent.data?.schemeSpecificPart ?: return
        if (packageName == context.packageName) return

        val appContext = context.applicationContext
        val pending = goAsync()
        Thread {
            try {
                // Always attempt an alert. Package metadata can briefly be unavailable
                // while Android finishes publishing the newly installed package.
                val info = loadAppInfo(appContext, packageName)
                if (info != null) {
                    InstallNotifications.show(appContext, info)
                } else {
                    InstallNotifications.showBasic(appContext, packageName)
                }
            } catch (t: Throwable) {
                Log.e("InstallSentry", "Failed to create install alert for $packageName", t)
                try {
                    InstallNotifications.showBasic(appContext, packageName)
                } catch (ignored: Throwable) {
                    Log.e("InstallSentry", "Fallback install alert failed", ignored)
                }
            } finally {
                pending.finish()
            }
        }.start()
    }
}
