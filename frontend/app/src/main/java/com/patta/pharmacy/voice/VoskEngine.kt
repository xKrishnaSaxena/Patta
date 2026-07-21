package com.patta.pharmacy.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline speech recognition via Vosk. The ~40MB Indian-English model is
 * downloaded once at runtime into internal storage, then recognition runs fully
 * on-device (no network). BETA — verify on a real device; the app defaults to
 * Google STT and only uses this when the user opts in and the model is ready.
 */
@Singleton
class VoskEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        const val MODEL_URL = "https://alphacephei.com/vosk/models/vosk-model-small-en-in-0.4.zip"
        const val SAMPLE_RATE = 16000.0f
    }

    private var model: Model? = null
    private var speechService: SpeechService? = null

    /** The unpacked model root (the folder that contains a `conf` directory). */
    private fun findModelDir(): File? =
        context.filesDir.listFiles()?.firstOrNull { it.isDirectory && File(it, "conf").exists() }

    fun isModelReady(): Boolean = findModelDir() != null

    /** Downloads + unzips the model. Returns success/failure; reports 0–100 progress. */
    suspend fun downloadModel(onProgress: (Int) -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(MODEL_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000; readTimeout = 30_000; connect()
            }
            val total = conn.contentLength.toLong()
            val zip = File(context.cacheDir, "vosk-model.zip")
            conn.inputStream.use { input ->
                FileOutputStream(zip).use { out ->
                    val buf = ByteArray(8192)
                    var read: Int
                    var done = 0L
                    while (input.read(buf).also { read = it } != -1) {
                        out.write(buf, 0, read)
                        done += read
                        if (total > 0) onProgress((done * 100 / total).toInt())
                    }
                }
            }
            unzip(zip, context.filesDir)
            zip.delete()
            if (findModelDir() == null) Result.failure(IllegalStateException("Model unpack fail"))
            else Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun ensureLoaded(): Boolean = withContext(Dispatchers.IO) {
        if (model != null) return@withContext true
        val dir = findModelDir() ?: return@withContext false
        try { model = Model(dir.absolutePath); true } catch (t: Throwable) { false }
    }

    /** Starts one utterance of recognition; [onResult] fires once, then stops. */
    fun startListening(onResult: (List<String>) -> Unit, onError: (String) -> Unit) {
        val m = model ?: run { onError("Offline model ready nahi hai"); return }
        try {
            val recognizer = Recognizer(m, SAMPLE_RATE)
            val service = SpeechService(recognizer, SAMPLE_RATE)
            service.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {}
                override fun onResult(hypothesis: String?) {
                    val text = extractText(hypothesis)
                    if (text.isNotBlank()) onResult(listOf(text.lowercase()))
                    stop()
                }
                override fun onFinalResult(hypothesis: String?) {
                    val text = extractText(hypothesis)
                    if (text.isNotBlank()) onResult(listOf(text.lowercase()))
                    stop()
                }
                override fun onError(exception: Exception?) { onError("Vosk error: ${exception?.message}"); stop() }
                override fun onTimeout() { stop() }
            })
            speechService = service
        } catch (t: Throwable) {
            onError("Vosk start fail: ${t.message}")
        }
    }

    fun stop() {
        try { speechService?.stop() } catch (_: Throwable) {}
        speechService = null
    }

    private fun extractText(hypothesis: String?): String =
        try { JSONObject(hypothesis ?: "{}").optString("text").trim() } catch (t: Throwable) { "" }

    private fun unzip(zip: File, targetDir: File) {
        ZipInputStream(BufferedInputStream(FileInputStream(zip))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { zis.copyTo(it) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}
