package com.ninjaconfig.app.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.ninjaconfig.app.MainActivity
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray

/**
 * Real VPN service: creates the TUN interface, hands its file descriptor to the
 * compiled Xray core (see .github/workflows/build.yml -> build-core job, and
 * XrayConfigBuilder.kt for how the JSON config is produced from a share link),
 * and keeps it running until stopped.
 */
class CtVpnService : VpnService(), CoreCallbackHandler {

    companion object {
        const val ACTION_CONNECT = "com.ninjaconfig.app.CONNECT"
        const val ACTION_DISCONNECT = "com.ninjaconfig.app.DISCONNECT"
        const val EXTRA_CONFIG_LINK = "config_link"

        const val ACTION_STATUS = "com.ninjaconfig.app.VPN_STATUS"
        const val EXTRA_STATUS = "status" // "connected" | "disconnected" | "error" | "diagnostic"
        const val EXTRA_MESSAGE = "message"

        private const val NOTIFICATION_CHANNEL_ID = "ct_vpn_channel"
        private const val NOTIFICATION_ID = 1
    }

    private var tunInterface: ParcelFileDescriptor? = null
    private var coreController: CoreController? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val link = intent.getStringExtra(EXTRA_CONFIG_LINK)
                if (link.isNullOrBlank()) {
                    startForeground(NOTIFICATION_ID, buildNotification())
                    broadcastStatus("error", "کانفیگی برای اتصال پیدا نشد")
                    stopVpn()
                } else {
                    startVpn(link)
                }
            }
            ACTION_DISCONNECT -> stopVpn()
            else -> {
                // Android requires startForeground() to be called unconditionally
                // shortly after startForegroundService(), even for an unknown/empty action.
                startForeground(NOTIFICATION_ID, buildNotification())
                stopVpn()
            }
        }
        return START_NOT_STICKY
    }

    private fun startVpn(configLink: String) {
        // Must be the very first thing that happens: the OS kills the service
        // if startForeground() isn't called within a few seconds of
        // startForegroundService(), regardless of what we do afterwards.
        startForeground(NOTIFICATION_ID, buildNotification())

        try {
            val jsonConfig = XrayConfigBuilder.build(configLink)

            // No geoip/geosite asset files are bundled - pass an empty path.
            Libv2ray.initCoreEnv(filesDir.absolutePath, "")

            val builder = Builder()
                .setSession("CT VPN")
                .addAddress("10.10.10.1", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .setMtu(1500)

            runCatching { builder.addDisallowedApplication(packageName) }

            tunInterface = builder.establish()
            val fd = tunInterface?.fd ?: run {
                broadcastStatus("error", "برقراری تانل شکست خورد")
                stopVpn()
                return
            }

            coreController = Libv2ray.newCoreController(this)
            coreController?.startLoop(jsonConfig, fd)
        } catch (e: Exception) {
            broadcastStatus("error", "${e.javaClass.simpleName}: ${e.message ?: "خطای نامشخص"}")
            stopVpn()
        }
    }

    private fun stopVpn() {
        runCatching { coreController?.stopLoop() }
        coreController = null
        runCatching { tunInterface?.close() }
        tunInterface = null
        broadcastStatus("disconnected", "قطع شد")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        runCatching { coreController?.stopLoop() }
        runCatching { tunInterface?.close() }
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    // ---- CoreCallbackHandler (called from the Go core) ----

    override fun startup(): Long {
        broadcastStatus("connected", "متصل شد")
        return 0
    }

    override fun shutdown(): Long {
        broadcastStatus("disconnected", "قطع شد")
        return 0
    }

    override fun onEmitStatus(code: Long, message: String?): Long {
        if (code != 0L && !message.isNullOrBlank()) {
            broadcastStatus("error", message)
        }
        return 0
    }

    private fun broadcastStatus(status: String, message: String) {
        val intent = Intent(ACTION_STATUS).apply {
            putExtra(EXTRA_STATUS, status)
            putExtra(EXTRA_MESSAGE, message)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "CT VPN",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("CT VPN")
            .setContentText("متصل")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()
    }
}
