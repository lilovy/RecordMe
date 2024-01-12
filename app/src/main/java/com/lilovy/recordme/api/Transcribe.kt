package com.lilovy.recordme.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException

class Transcribe {

    private val api: UploadApi = Retrofit.Builder()
        .baseUrl("https://trnb-1-i9150047.deta.app")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UploadApi::class.java)

    suspend fun getTranscribe(audioFilePath: String?): String? {
        val audioFile = audioFilePath?.let { File(it) }

        if (audioFile != null) {
            if (!audioFile.exists()) {
                return "File not found"
            }
        }

        val requestFile = audioFile?.asRequestBody("audio/*".toMediaTypeOrNull())
        val filePart = requestFile?.let { MultipartBody.Part.createFormData("file", audioFile.name, it) }

        var response : Response<Transcription>? = null

        try {
            response = requestFile?.let { api.transcribeAudio(filePart!!) }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: HttpException) {
            e.printStackTrace()
        }

        return if (response != null) {
            response.body()?.text
        } else {
            "error"
        }
    }
}

