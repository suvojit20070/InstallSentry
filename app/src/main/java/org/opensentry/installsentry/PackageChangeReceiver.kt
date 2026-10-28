package org.opensentry.installsentry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return
        if (packageName == context.packageName) return
        val pending = goAsync()
        Thread {
            try {
                val isReplacement = intent.action == Intent.ACTION_PACKAGE_REPLACED
                val info = loadAppInfo(context, packageName) ?: return@Thread
                // Replacements are updates, not fresh installs. Only alert on PACKAGE_ADDED.
                if (!isReplacement && intent.action == Intent.ACTION_PACKAGE_ADDED) {
                    InstallNotifications.show(context, info)
                }
            } finally {
                pending.finish()
            }
        }.start()
    }
}
