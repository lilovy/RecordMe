package com.lilovy.recordme.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi {

    @Multipart
    @POST("/uploadfile")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part
    ): Response<Transcription>
}

data class Transcription(
    val text: String?
)