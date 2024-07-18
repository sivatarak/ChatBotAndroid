package com.chatgptlite.wanted.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteModel(private val context: Context) {
//    private val modelFileName = "whisper-tiny.tflite"
//    private val modelUrl = "https://your-server.com/path/to/whisper-tiny.tflite"

    private val huggingFaceApiKey = "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" // Add your Hugging Face API key here
    private val huggingFaceApiUrl = "https://api-inference.huggingface.co/models/openai/whisper-small" // Update with the correct endpoint if necessary

//    fun initializeModel(onModelReady: () -> Unit, onError: (Exception) -> Unit) {
//        val modelFile = File(context.filesDir, modelFileName)
//        if (modelFile.exists()) {
//            loadModel(modelFile, onModelReady, onError)
//        } else {
//            downloadModel(modelFile, onModelReady, onError)
//        }
//    }

//    private fun loadModel(modelFile: File, onModelReady: () -> Unit, onError: (Exception) -> Unit) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Load the model here if needed for local inference
//                // interpreter = Interpreter(loadModelFile(modelFile))
//                withContext(Dispatchers.Main) {
//                    onModelReady()
//                }
//            } catch (e: IOException) {
//                withContext(Dispatchers.Main) {
//                    onError(e)
//                }
//            }
//        }
//    }

    @Throws(IOException::class)
    private fun loadModelFile(modelFile: File): MappedByteBuffer {
        val inputStream = FileInputStream(modelFile)
        val fileChannel = inputStream.channel
        val startOffset = 0L
        val declaredLength = modelFile.length()
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

//    private fun downloadModel(modelFile: File, onModelReady: () -> Unit, onError: (Exception) -> Unit) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val url = URL(modelUrl)
//                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
//                connection.connect()
//                val inputStream: InputStream = connection.inputStream
//                val outputStream = FileOutputStream(modelFile)
//                val buffer = ByteArray(1024)
//                var bytesRead: Int
//                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
//                    outputStream.write(buffer, 0, bytesRead)
//                }
//                outputStream.close()
//                inputStream.close()
//                loadModel(modelFile, onModelReady, onError)
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    onError(e)
//                }
//            }
//        }
//    }

    fun transcribeAudioUsingHuggingFace(audioFile: File, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val mediaType = "audio/wav"?.toMediaTypeOrNull() // Update the media type according to your audio file format
                val requestBody = RequestBody.create(mediaType, audioFile)
                val request = Request.Builder()
                    .url(huggingFaceApiUrl)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $huggingFaceApiKey")
                    .addHeader("Content-Type", "audio/wav") // Update as needed
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        CoroutineScope(Dispatchers.Main).launch {
                            onError(e)
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                            Log.d("TFLiteModel", "Transcription result: $responseData")
                            CoroutineScope(Dispatchers.Main).launch {
                                onSuccess(responseData ?: "No response data")
                            }
                        } else {
                            Log.e("TFLiteModel", "Error: ${response.code}")
                            CoroutineScope(Dispatchers.Main).launch {
                                onError(IOException("HTTP Error: ${response.code}"))
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onError(e)
                }
            }
        }
    }

    // Add methods for local inference if needed
    fun runInference(input: FloatArray): FloatArray {
        // Placeholder for local model inference if needed
        // val output = FloatArray(1) // Adjust the size based on your model
        // interpreter.run(input, output)
        // Log.d("TFLiteModel", "Inference result: ${output[0]}")
        // return output
        throw NotImplementedError("Local inference is not implemented.")
    }
}
