package com.tvlauncher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : Activity() {

    private lateinit var list: RecyclerView
    private lateinit var bgTop: ImageView
    private lateinit var bgBottom: ImageView
    private lateinit var adapter: AppAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val backgrounds = intArrayOf(
        R.drawable.bg_1, R.drawable.bg_2, R.drawable.bg_3, R.drawable.bg_4, R.drawable.bg_5
    )
    private var bgIndex = 0
    private var topVisible = true

    private val rotateTask = object : Runnable {
        override fun run() {
            crossfade()
            handler.postDelayed(this, ROTATE_MS)
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = loadApps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bgTop = findViewById(R.id.bg_top)
        bgBottom = findViewById(R.id.bg_bottom)
        bgTop.setImageResource(backgrounds[0])
        bgBottom.setImageResource(backgrounds[0])

        adapter = AppAdapter(packageManager, ::launch, ::openAppInfo)
        list = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        list.adapter = adapter
        list.setHasFixedSize(true)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(packageReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        })
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(rotateTask, ROTATE_MS)
    }

    override fun onPause() {
        handler.removeCallbacks(rotateTask)
        super.onPause()
    }

    override fun onStop() {
        unregisterReceiver(packageReceiver)
        super.onStop()
    }

    // Botón HOME estando ya en el launcher: volver al inicio de la fila.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        list.scrollToPosition(0)
        focusFirst()
    }

    // Un launcher no debe cerrarse con "Atrás".
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() = Unit

    private fun crossfade() {
        bgIndex = (bgIndex + 1) % backgrounds.size
        if (topVisible) {
            bgBottom.setImageResource(backgrounds[bgIndex])
            bgTop.animate().alpha(0f).setDuration(FADE_MS).start()
        } else {
            bgTop.setImageResource(backgrounds[bgIndex])
            bgTop.animate().alpha(1f).setDuration(FADE_MS).start()
        }
        topVisible = !topVisible
    }

    private fun loadApps() {
        Thread {
            val apps = queryApps()
            runOnUiThread {
                if (!isDestroyed) {
                    adapter.submit(apps)
                    if (!list.hasFocus()) focusFirst()
                }
            }
        }.start()
    }

    private fun focusFirst() {
        list.post { list.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus() }
    }

    @Suppress("DEPRECATION")
    private fun query(category: String): List<ResolveInfo> =
        packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)

    private fun queryApps(): List<AppEntry> {
        val seen = LinkedHashMap<String, ResolveInfo>()
        // Primero apps de TV; luego las que solo tienen icono de móvil.
        for (ri in query(Intent.CATEGORY_LEANBACK_LAUNCHER) + query(Intent.CATEGORY_LAUNCHER)) {
            val pkg = ri.activityInfo.packageName
            if (pkg != packageName && pkg !in seen) seen[pkg] = ri
        }
        return seen.values
            .map {
                AppEntry(
                    it.loadLabel(packageManager).toString(),
                    ComponentName(it.activityInfo.packageName, it.activityInfo.name),
                    it
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun launch(app: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(app.component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show()
        }
    }

    // Pulsación larga en OK: ajustes de la app (desinstalar, permisos, etc.).
    private fun openAppInfo(app: AppEntry) {
        try {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.component.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ROTATE_MS = 30_000L
        private const val FADE_MS = 1_500L
    }
}
