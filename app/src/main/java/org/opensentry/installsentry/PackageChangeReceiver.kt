package org.opensentry.installsentry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_PACKAGE_ADDED) return
        // Android also sends PACKAGE_ADDED while replacing/updating an existing app.
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
        val packageName = intent.data?.schemeSpecificPart ?: return
        if (packageName == context.packageName) return

        val pending = goAsync()
        Thread {
            try {
                val info = loadAppInfo(context.applicationContext, packageName) ?: return@Thread
                InstallNotifications.show(context.applicationContext, info)
            } finally {
                pending.finish()
            }
        }.start()
    }
}
