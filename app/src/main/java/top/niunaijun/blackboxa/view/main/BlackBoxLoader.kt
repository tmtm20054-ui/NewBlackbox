package top.niunaijun.blackboxa.view.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File
import java.io.FileOutputStream

import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.app.BActivityThread
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback
import top.niunaijun.blackbox.app.configuration.ClientConfiguration
import top.niunaijun.blackboxa.app.App
import top.niunaijun.blackboxa.app.rocker.RockerManager
import top.niunaijun.blackboxa.biz.cache.AppSharedPreferenceDelegate

class BlackBoxLoader {

    private var mHideRoot by AppSharedPreferenceDelegate(App.getContext(), false)
    private var mDaemonEnable by AppSharedPreferenceDelegate(App.getContext(), false)
    private var mShowShortcutPermissionDialog by AppSharedPreferenceDelegate(App.getContext(), true)
    private var mUseVpnNetwork by AppSharedPreferenceDelegate(App.getContext(), false)
    private var mDisableFlagSecure by AppSharedPreferenceDelegate(App.getContext(), false)

    companion object {
        val TAG: String = BlackBoxLoader::class.java.simpleName
    }

    fun hideRoot(): Boolean {
        return try {
            mHideRoot
        } catch (e: Exception) {
            Log.e(TAG, "Error getting hideRoot", e)
            false
        }
    }

    fun invalidHideRoot(hideRoot: Boolean) {
        try {
            mHideRoot = hideRoot
        } catch (e: Exception) {
            Log.e(TAG, "Error setting hideRoot", e)
        }
    }

    fun disableFlagSecure(): Boolean {
        return try {
            mDisableFlagSecure
        } catch (e: Exception) {
            Log.e(TAG, "Error getting disableFlagSecure", e)
            false
        }
    }

    fun invalidDisableFlagSecure(disable: Boolean) {
        try {
            mDisableFlagSecure = disable
        } catch (e: Exception) {
            Log.e(TAG, "Error setting disableFlagSecure", e)
        }
    }

    fun daemonEnable(): Boolean {
        return try {
            mDaemonEnable
        } catch (e: Exception) {
            Log.e(TAG, "Error getting daemonEnable", e)
            false
        }
    }

    fun invalidDaemonEnable(enable: Boolean) {
        try {
            mDaemonEnable = enable
        } catch (e: Exception) {
            Log.e(TAG, "Error setting daemonEnable", e)
        }
    }

    fun showShortcutPermissionDialog(): Boolean {
        return try {
            mShowShortcutPermissionDialog
        } catch (e: Exception) {
            Log.e(TAG, "Error getting showShortcutPermissionDialog", e)
            true
        }
    }

    fun invalidShortcutPermissionDialog(show: Boolean) {
        try {
            mShowShortcutPermissionDialog = show
        } catch (e: Exception) {
            Log.e(TAG, "Error setting showShortcutPermissionDialog", e)
        }
    }

    fun useVpnNetwork(): Boolean {
        return try {
            mUseVpnNetwork
        } catch (e: Exception) {
            Log.e(TAG, "Error getting useVpnNetwork", e)
            false
        }
    }

    fun invalidUseVpnNetwork(enable: Boolean) {
        try {
            mUseVpnNetwork = enable
        } catch (e: Exception) {
            Log.e(TAG, "Error setting useVpnNetwork", e)
        }
    }

    fun getBlackBoxCore(): BlackBoxCore {
        return BlackBoxCore.get()
    }

    /**
     * ينسخ جميع ملفات APK من assets/apps إلى التخزين الداخلي.
     * يعيد قائمة الملفات التي تم نسخها بنجاح.
     */
    fun importApkAssets(context: Context): List<File> {
        val importedFiles = mutableListOf<File>()

        try {
            val apkNames = context.assets.list("apps") ?: emptyArray()
            val outputDir = File(context.filesDir, "apk_imports")

            if (!outputDir.exists() && !outputDir.mkdirs()) {
                Log.e(TAG, "Cannot create APK directory")
                return emptyList()
            }

            for (name in apkNames) {
                if (!name.endsWith(".apk", ignoreCase = true)) {
                    continue
                }

                try {
                    val outputFile = File(outputDir, name)

                    context.assets.open("apps/$name").use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (outputFile.isFile && outputFile.length() > 0L) {
                        importedFiles.add(outputFile)
                        Log.i(TAG, "APK prepared: ${outputFile.absolutePath}")
                    } else {
                        Log.e(TAG, "APK file is empty: $name")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to copy APK: $name", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read assets/apps", e)
        }

        return importedFiles
    }

    fun addLifecycleCallback() {
        try {
            BlackBoxCore.get().addAppLifecycleCallback(
                object : AppLifecycleCallback() {

                    override fun beforeCreateApplication(
                        packageName: String?,
                        processName: String?,
                        context: Context?,
                        userId: Int
                    ) {
                        Log.d(
                            TAG,
                            "beforeCreateApplication: pkg=$packageName, " +
                                "process=$processName, userID=${BActivityThread.getUserId()}"
                        )
                    }

                    override fun beforeApplicationOnCreate(
                        packageName: String?,
                        processName: String?,
                        application: Application?,
                        userId: Int
                    ) {
                        Log.d(
                            TAG,
                            "beforeApplicationOnCreate: pkg=$packageName, process=$processName"
                        )
                    }

                    override fun afterApplicationOnCreate(
                        packageName: String?,
                        processName: String?,
                        application: Application?,
                        userId: Int
                    ) {
                        try {
                            Log.d(
                                TAG,
                                "afterApplicationOnCreate: pkg=$packageName, process=$processName"
                            )
                            RockerManager.init(application, userId)
                        } catch (e: Exception) {
                            Log.e(TAG, "RockerManager initialization failed", e)
                        }
                    }

                    override fun onStoragePermissionNeeded(
                        packageName: String?,
                        userId: Int
                    ): Boolean {
                        return try {
                            Log.w(TAG, "Storage permission needed: $packageName")

                            val intent = Intent(
                                "top.niunaijun.blackboxa.REQUEST_STORAGE_PERMISSION"
                            ).apply {
                                putExtra("package_name", packageName)
                                putExtra("user_id", userId)
                                setPackage(App.getContext().packageName)
                            }

                            App.getContext().sendBroadcast(intent)
                            false
                        } catch (e: Exception) {
                            Log.e(TAG, "Storage permission request failed", e)
                            false
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error adding lifecycle callback", e)
        }
    }

    fun attachBaseContext(context: Context) {
        try {
            BlackBoxCore.get().doAttachBaseContext(
                context,
                object : ClientConfiguration() {

                    override fun getHostPackageName(): String {
                        return context.packageName
                    }

                    override fun isHideRoot(): Boolean {
                        return hideRoot()
                    }

                    override fun isEnableDaemonService(): Boolean {
                        return daemonEnable()
                    }

                    override fun isUseVpnNetwork(): Boolean {
                        return useVpnNetwork()
                    }

                    override fun isDisableFlagSecure(): Boolean {
                        return disableFlagSecure()
                    }

                    override fun requestInstallPackage(
                        file: File?,
                        userId: Int
                    ): Boolean {
                        return try {
                            if (file == null || !file.isFile) {
                                Log.e(TAG, "Install request has no valid APK file")
                                false
                            } else {
                                Log.i(
                                    TAG,
                                    "Install requested: ${file.absolutePath}, userId=$userId"
                                )

                                // لم تتم إضافة عملية التثبيت هنا لأن واجهتها
                                // تعتمد على إصدار BlackBox المستخدم.
                                false
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Install request failed", e)
                            false
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in attachBaseContext", e)
        }
    }

    fun doOnCreate(context: Context) {
        try {
            BlackBoxCore.get().doCreate()

            try {
                BlackBoxCore.get().addServiceAvailableCallback {
                    Log.d(TAG, "BlackBox services are available")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register service callback", e)
            }

            // تجهيز ملفات APK الموجودة في assets/apps.
            val apkFiles = importApkAssets(context)

            Log.i(TAG, "Number of APK files prepared: ${apkFiles.size}")

            apkFiles.forEach { apk ->
                Log.i(TAG, "Ready for installation: ${apk.name}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in doOnCreate", e)
        }
    }
}
