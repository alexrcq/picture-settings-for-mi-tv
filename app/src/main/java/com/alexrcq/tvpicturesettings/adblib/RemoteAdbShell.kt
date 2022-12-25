package com.alexrcq.tvpicturesettings.adblib

import android.Manifest
import android.content.Context
import android.os.FileObserver.CREATE
import com.alexrcq.tvpicturesettings.BuildConfig
import com.alexrcq.tvpicturesettings.hasPermission
import com.alexrcq.tvpicturesettings.waitForFileEvent
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import com.tananaev.adblib.AdbStream
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class RemoteAdbShell private constructor(
    private val appContext: Context,
    private val host: String = "127.0.0.1",
    private val port: Int = 5555
) {
    private var adbConnection: AdbConnection? = null
    private var isConnected = false

    suspend fun connect() = withContext(IO) {
        if (!isConnected) {
            adbConnection = createConnection()
            Timber.d("connecting...")
            val isConnectionEstablished = adbConnection?.connect(
                CONNECTION_TIMEOUT_SECONDS,
                TimeUnit.SECONDS,
                false
            )
            if (isConnectionEstablished == false) {
                throw TimeoutException("adb connection timeout")
            }
            isConnected = true
            Timber.d("connected")
        }
    }

    private suspend fun execute(command: String) = withContext(IO) {
        Timber.d(command)
        openShell()?.write("$command\n")
    }

    suspend fun grantPermission(permission: String) {
        execute("pm grant ${BuildConfig.APPLICATION_ID} $permission")
        while (true) {
            delay(50)
            if (appContext.hasPermission(permission)) {
                // added extra time to permissions handling
                delay(200)
                Timber.d("$permission granted")
                break
            }
        }
    }

    suspend fun captureScreen(screenshotsDirPath: String) = withContext(IO) {
        if (!appContext.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            grantPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val saveDir = File(screenshotsDirPath)
        if (!saveDir.exists()) {
            if (!appContext.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                grantPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            saveDir.mkdirs()
        }
        val currentTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS")
        )
        val waitForScreenshotFileCreationJob = launch(start = CoroutineStart.UNDISPATCHED) {
            saveDir.waitForFileEvent(CREATE)
        }
        execute("screencap -p ${screenshotsDirPath}/screenshot$currentTime.png")
        waitForScreenshotFileCreationJob.join()
    }

    private fun openShell(): AdbStream? {
        return adbConnection?.open("shell:")
    }

    private fun setupCrypto(pubKeyFile: String, privKeyFile: String): AdbCrypto? {
        val publicKey = File(pubKeyFile)
        val privateKey = File(privKeyFile)
        var crypto: AdbCrypto? = null
        if (publicKey.exists() && privateKey.exists()) {
            crypto = try {
                AdbCrypto.loadAdbKeyPair(AndroidBase64(), privateKey, publicKey)
            } catch (e: Exception) {
                null
            }
        }
        if (crypto == null) {
            crypto = AdbCrypto.generateAdbKeyPair(AndroidBase64())
            crypto.saveAdbKeyPair(privateKey, publicKey)
            Timber.d("Generated new keypair")
        } else {
            Timber.d("Loaded existing keypair")
        }
        return crypto
    }

    private fun createConnection(): AdbConnection {
        val path = appContext.cacheDir.absolutePath
        val crypto = setupCrypto("$path/pub.key", "$path/priv.key")
        val socket = Socket(host, port)
        return AdbConnection.create(socket, crypto)
    }

    fun disconnect() {
        if (isConnected) {
            adbConnection?.close()
            isConnected = false
            Timber.d("disconnected")
        }
    }

    companion object {
        const val CONNECTION_TIMEOUT_SECONDS = 25L

        @Volatile
        private var INSTANCE: RemoteAdbShell? = null

        fun getInstance(context: Context): RemoteAdbShell =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RemoteAdbShell(context).also { INSTANCE = it }
            }
    }
}