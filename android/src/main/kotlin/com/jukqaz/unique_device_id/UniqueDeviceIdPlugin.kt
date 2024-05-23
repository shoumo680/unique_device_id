package com.jukqaz.unique_device_id

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/** UniqueDeviceIdPlugin */
class UniqueDeviceIdPlugin : FlutterPlugin, MethodCallHandler {
    companion object {
        private val filePath = Environment.getExternalStorageDirectory().absolutePath.plus("/.udi")
        private const val fileName = ".unique_device_id"
        private val defaultSecretKey = "UniqueDeviceIdPlugin"
    }

    private lateinit var channel: MethodChannel
    private var context: Context? = null

    private var aesUtil: AESUtil? = null
    private var secretKey = defaultSecretKey
    private var isDefaultUseUUID = false

    private val uuidFile by lazy {
        val directory = File(filePath)
        directory.takeUnless { dir -> dir.exists() }?.mkdir()
        File(directory, fileName)
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "unique_device_id")
        channel.setMethodCallHandler(this)

        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "setSecretKey" -> {
                secretKey = (call.arguments as? String?) ?: defaultSecretKey
                result.success(null)
            }
            "setDefaultUseUUID" -> {
                isDefaultUseUUID = (call.arguments as? Boolean?) ?: false
                result.success(null)
            }
            "getUniqueId" -> CoroutineScope(Dispatchers.Main).launch {
                try {
                    result.success(getUniqueId())
                } catch (e: PermissionNotGrantedException) {
                    result.error("1011", "permission is not granted!", null)
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)

        context = null
    }

    private suspend fun getUniqueId(): String {
        var uniqueId: String? = if (isDefaultUseUUID) null else getAndroidId()
        if (uniqueId?.isBlank() != false) {
            uniqueId = getSavedUUID()
        }
        return uniqueId
    }

    private suspend fun getSavedUUID(): String {
        if (aesUtil?.key != secretKey)
            aesUtil = AESUtil(secretKey)

        var uuid = readUUIDFromInternalStorage()
        if (uuid?.isBlank() != false) {
            uuid = UUID.randomUUID().toString()
            writeUUIDToInternalStorage(uuid)
        }
        return uuid
    }

    private suspend fun readUUIDFromInternalStorage() = withContext(Dispatchers.IO) {
        if (!checkExternalStoragePermission()) throw PermissionNotGrantedException()
        try {
            uuidFile.bufferedReader().use { aesUtil?.decode(it.readLine()) }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun writeUUIDToInternalStorage(uuid: String) = withContext(Dispatchers.IO) {
        if (!checkExternalStoragePermission()) throw PermissionNotGrantedException()
        try {
            uuidFile.bufferedWriter().use { it.write((aesUtil?.encode(uuid))) }
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId() = context?.run {
        Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun checkExternalStoragePermission() =
        context?.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

class PermissionNotGrantedException : Exception()
