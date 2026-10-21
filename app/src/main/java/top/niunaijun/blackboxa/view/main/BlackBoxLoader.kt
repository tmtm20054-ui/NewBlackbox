package top.niunaijun.blackboxa.view.main

import android.app.Application
import android.content.Context
import android.util.Log
import java.io.File
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

    fun hideRoot(): Boolean {
        return try {
            mHideRoot
        } catch (e: Exception) {
            Log.e(TAG, "Error getting hideRoot: ${e.message}")
            false
        }
    }

    fun invalidHideRoot(hideRoot: Boolean) {
        try {
            this.mHideRoot = hideRoot
        } catch (e: Exception) {
            Log.e(TAG, "Error setting hideRoot: ${e.message}")
        }
    }

    fun disableFlagSecure(): Boolean {
        return try {
            mDisableFlagSecure
        } catch (e: Exception) {
            Log.e(TAG, "Error getting disableFlagSecure: ${e.message}")
            false
        }
    }

    fun invalidDisableFlagSecure(disable: Boolean) {
        try {
            this.mDisableFlagSecure = disable
        } catch (e: Exception) {
            Log.e(TAG, "Error setting disableFlagSecure: ${e.message}")
        }
    }

    fun daemonEnable(): Boolean {
        return try {
            mDaemonEnable
        } catch (e: Exception) {
            Log.e(TAG, "Error getting daemonEnable: ${e.message}")
            false
        }
    }

    fun invalidDaemonEnable(enable: Boolean) {
        try {
            this.mDaemonEnable = enable
        } catch (e: Exception) {
            Log.e(TAG, "Error setting daemonEnable: ${e.message}")
        }
    }

    fun showShortcutPermissionDialog(): Boolean {
        return try {
            mShowShortcutPermissionDialog
        } catch (e: Exception) {
            Log.e(TAG, "Error getting showShortcutPermissionDialog: ${e.message}")
            true
        }
    }

    fun invalidShortcutPermissionDialog(show: Boolean) {
        try {
            this.mShowShortcutPermissionDialog = show
        } catch (e: Exception) {
            Log.e(TAG, "Error setting showShortcutPermissionDialog: ${e.message}")
        }
    }

    fun useVpnNetwork(): Boolean {
        return try {
            mUseVpnNetwork
        } catch (e: Exception) {
            Log.e(TAG, "Error getting useVpnNetwork: ${e.message}")
            false
        }
    }

    fun invalidUseVpnNetwork(enable: Boolean) {
        try {
            this.mUseVpnNetwork = enable
        } catch (e: Exception) {
            Log.e(TAG, "Error setting useVpnNetwork: ${e.message}")
        }
    }

    fun getBlackBoxCore(): BlackBoxCore {
        return try {
            BlackBoxCore.get()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting BlackBoxCore: ${e.message}")
            throw e
        }
    }

    fun addLifecycleCallback() {
        try {
            BlackBoxCore.get()
                    .addAppLifecycleCallback(
                            object : AppLifecycleCallback() {
                                override fun beforeCreateApplication(
                                        packageName: String?,
                                        processName: String?,
                                        context: Context?,
                                        userId: Int
                                ) {
                                    try {
                                        Log.d(
                                                TAG,
                                                "beforeCreateApplication: pkg $packageName, processName $processName,userID:${BActivityThread.getUserId()}"
                                        )
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error in beforeCreateApplication: ${e.message}")
                                    }
                                }

                                override fun beforeApplicationOnCreate(
                                        packageName: String?,
                                        processName: String?,
                                        application: Application?,
                                        userId: Int
                                ) {
                                    try {
                                        Log.d(
                                                TAG,
                                                "beforeApplicationOnCreate: pkg $packageName, processName $processName"
                                        )
                                    } catch (e: Exception) {
                                        Log.e(
                                                TAG,
                                                "Error in beforeApplicationOnCreate: ${e.message}"
                                        )
                                    }
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
                                                "afterApplicationOnCreate: pkg $packageName, processName $processName"
                                        )
                                        RockerManager.init(application, userId)
                                    } catch (e: Exception) {
                                        Log.e(
                                                TAG,
                                                "Error in afterApplicationOnCreate: ${e.message}"
                                        )
                                    }
                                }

                                override fun onStoragePermissionNeeded(
                                        packageName: String?,
                                        userId: Int
                                ): Boolean {
                                    try {
                                        Log.w(
                                                TAG,
                                                "Storage permission needed for launching: $packageName"
                                        )
                                        
                                        
                                        
                                        val intent =
                                                android.content.Intent(
                                                        "top.niunaijun.blackboxa.REQUEST_STORAGE_PERMISSION"
                                                )
                                        intent.putExtra("package_name", packageName)
                                        intent.putExtra("user_id", userId)
                                        intent.setPackage(App.getContext().packageName)
                                        App.getContext().sendBroadcast(intent)
                                        
                                        
                                        
                                        
                                        
                                        return false
                                    } catch (e: Exception) {
                                        Log.e(
                                                TAG,
                                                "Error in onStoragePermissionNeeded: ${e.message}"
                                        )
                                        return false
                                    }
                                }
                            }
                    )
        } catch (e: Exception) {
            Log.e(TAG, "Error adding lifecycle callback: ${e.message}")
        }
    }

    fun attachBaseContext(context: Context) {
        try {
            BlackBoxCore.get()
                    .doAttachBaseContext(
                            context,
                            object : ClientConfiguration() {
                                override fun getHostPackageName(): String {
                                    return try {
                                        context.packageName
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error getting package name: ${e.message}")
                                        "unknown"
                                    }
                                }

                                override fun isHideRoot(): Boolean {
                                    return try {
                                        mHideRoot
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error checking hideRoot: ${e.message}")
                                        false
                                    }
                                }

                                override fun isEnableDaemonService(): Boolean {
                                    return try {
                                        mDaemonEnable
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error checking daemonEnable: ${e.message}")
                                        false
                                    }
                                }

                                override fun isUseVpnNetwork(): Boolean {
                                    return try {
                                        mUseVpnNetwork
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error checking useVpnNetwork: ${e.message}")
                                        false
                                    }
                                }

                                override fun isDisableFlagSecure(): Boolean {
                                    return try {
                                        mDisableFlagSecure
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error checking disableFlagSecure: ${e.message}")
                                        false
                                    }
                                }

                                override fun requestInstallPackage(
                                        file: File?,
                                        userId: Int
                                ): Boolean {
                                    return try {
                                        if (file == null) {
                                            Log.w(TAG, "requestInstallPackage: file is null")
                                            return false
                                        }
                                        val packageInfo =
                                                context.packageManager.getPackageArchiveInfo(
                                                        file.absolutePath,
                                                        0
                                                )
                                        false
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error in requestInstallPackage: ${e.message}")
                                        false
                                    }
                                }
                            }
                    )
        } catch (e: Exception) {
            Log.e(TAG, "Error in attachBaseContext: ${e.message}")
        }
    }

    fun doOnCreate(context: Context) {
        try {
            BlackBoxCore.get().doCreate()

            
            try {
                BlackBoxCore.get().addServiceAvailableCallback {
                    Log.d(TAG, "Services became available, triggering app list refresh")
                    // فقط في العملية الرئيسية وعلى thread خلفي، حتى لا يتأثر التطبيق المستنسخ
                    if (BlackBoxCore.get().isMainProcess() && bundledInstallStarted.compareAndSet(false, true)) {
                        Thread { installBundledApksFromAssets(context) }.start()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering service available callback: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in doOnCreate: ${e.message}")
        }
    }

    /**
     * يفحص مجلد assets/apps بحثًا عن ملفات APK ويثبّت أي ملف غير مثبّت
     * داخل البيئة الافتراضية (BlackBox) للمستخدم المحدد. لا يلمس نظام
     * الجهاز الحقيقي إطلاقًا؛ التثبيت يتم فقط داخل الـ sandbox الخاصة
     * بـ BlackBox، تمامًا مثل أي تثبيت يقوم به المستخدم يدويًا من داخل التطبيق.
     */
    fun installBundledApksFromAssets(context: Context, userId: Int = 0) {
        try {
            val outDir = File(context.cacheDir, "bundled_apks").apply { mkdirs() }

            // يفحص جذر assets/ مباشرة، وأيضًا assets/apps/ إن وُجد — يدعم الحالتين
            val candidates = mutableListOf<Pair<String, String>>() // (assetPath, fileName)

            context.assets.list("")
                    ?.filter { it.endsWith(".apk", ignoreCase = true) }
                    ?.forEach { candidates.add(it to it) }

            context.assets.list("apps")
                    ?.filter { it.endsWith(".apk", ignoreCase = true) }
                    ?.forEach { candidates.add("apps/$it" to it) }

            if (candidates.isEmpty()) {
                Log.d(TAG, "installBundledApksFromAssets: no bundled APKs found")
                return
            }

            for ((assetPath, fileName) in candidates) {
                try {
                    val outFile = File(outDir, fileName)

                    // انسخ من assets إلى ملف فعلي (assets غير قابلة للوصول كمسار مباشر)
                    context.assets.open(assetPath).use { input ->
                        outFile.outputStream().use { output -> input.copyTo(output) }
                    }

                    // اقرأ اسم الحزمة لتفادي إعادة تثبيت تطبيق مثبّت أصلًا
                    val archiveInfo = context.packageManager.getPackageArchiveInfo(outFile.absolutePath, 0)
                    val packageName = archiveInfo?.packageName
                    val prefs = context.getSharedPreferences("bundled_apks", Context.MODE_PRIVATE)
                    val stamp = "$assetPath:${outFile.length()}"
                    // لا تعيد التثبيت أبدًا إذا سبق تثبيت نفس الملف (إعادة التثبيت تمسح إعدادات التطبيق)
                    if (prefs.getBoolean(stamp, false)) {
                        Log.d(TAG, "installBundledApksFromAssets: $assetPath already handled, skipping")
                        continue
                    }
                    if (packageName != null && BlackBoxCore.get().isInstalled(packageName, userId)) {
                        prefs.edit().putBoolean(stamp, true).apply()
                        Log.d(TAG, "installBundledApksFromAssets: $packageName already installed, skipping")
                        continue
                    }

                    val result = BlackBoxCore.get().installPackageAsUser(outFile, userId)
                    if (result.success) {
                        prefs.edit().putBoolean(stamp, true).apply()
                        Log.d(TAG, "installBundledApksFromAssets: installed $assetPath -> ${result.packageName}")
                    } else {
                        Log.w(TAG, "installBundledApksFromAssets: failed to install $assetPath: ${result.msg}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "installBundledApksFromAssets: error handling $assetPath: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in installBundledApksFromAssets: ${e.message}")
        }
    }

    companion object {
        val TAG: String = BlackBoxLoader::class.java.simpleName
        private val bundledInstallStarted = java.util.concurrent.atomic.AtomicBoolean(false)
    }
}
