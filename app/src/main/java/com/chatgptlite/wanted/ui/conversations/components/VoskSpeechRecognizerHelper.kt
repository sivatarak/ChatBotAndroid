package com.chatgptlite.wanted.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.RecognitionListener
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class VoskSpeechRecognizerHelper(private val context: Context) {
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null

    private val modelPath = "model"
    private val modelUrl = "openai/whisper-tiny"  // Replace with your model URL
    private val modelFileName = "vosk-model-small-en-us-0.15.zip"

    init {
        initModel()
    }

    private fun initModel() {
        val modelFile = File(context.filesDir, modelPath)
        if (modelFile.exists()) {
            loadModel(modelFile)
        } else {
            downloadAndExtractModel()
        }
    }

    private fun loadModel(modelFile: File) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                model = Model(modelFile.absolutePath)  // Correctly create a Model instance
                Log.i("Vosk", "Model loaded successfully")
            } catch (e: Exception) {
                Log.e("Vosk", "Failed to load model: ${e.message}")
            }
        }
    }

    private fun downloadAndExtractModel() {
        CoroutineScope(Dispatchers.IO).launch {
            val zipFile = downloadFile(context, modelUrl, modelFileName)
            zipFile?.let {
                withContext(Dispatchers.IO) {
                    try {
                        val unzipFile = File(context.filesDir, modelPath)
                        unzip(zipFile, unzipFile)
                        loadModel(unzipFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun unzip(zipFile: File, targetDir: File) {
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            var ze: ZipEntry?
            val buffer = ByteArray(1024)
            while (zis.nextEntry.also { ze = it } != null) {
                val file = File(targetDir, ze!!.name)
                if (ze!!.isDirectory) {
                    file.mkdirs()
                } else {
                    FileOutputStream(file).use { fos ->
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
            }
        }
    }

    private fun downloadFile(context: Context, url: String, fileName: String): File? {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            return file
        }

        try {
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.connect()
            val inputStream: InputStream = urlConnection.inputStream
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.close()
            inputStream.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun startListening(listener: (String) -> Unit) {
        model?.let {
            recognizer = Recognizer(it, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(object : RecognitionListener {
                override fun onResult(result: String) {
                    listener(result)
                }

                override fun onPartialResult(partialResult: String) {
                    // Handle partial results if needed
                }

                override fun onError(e: Exception) {
                    Log.e("Vosk", "Recognition error: ${e.message}")
                }

                override fun onTimeout() {
                    // Handle timeout if needed
                }

                override fun onFinalResult(hypothesis: String) {
                    listener(hypothesis)
                }
            })
        }
    }

    fun stopListening() {
        speechService?.stop()
        speechService?.shutdown()
    }
}
