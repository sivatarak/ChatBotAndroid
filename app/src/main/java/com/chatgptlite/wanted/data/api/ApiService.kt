package com.chatgptlite.wanted.data.api

import com.chatgptlite.wanted.constants.ApiRequest
import com.chatgptlite.wanted.constants.Data1
import com.chatgptlite.wanted.constants.LoginRequest
import com.chatgptlite.wanted.constants.LoginResponse
import com.chatgptlite.wanted.constants.QuestionReqData
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST



interface ApiService {
    @POST("chatbot/rag")
    fun sendMessage(@Body requestBody: ApiRequest): Call<ResponseBody>

    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("chatbot/getAgentIds")
    fun getAgents(@Body request: Data1): Call<ResponseBody>

    @POST("chatbot/getQuestionCards")
    fun getQuestionCards(@Body request: QuestionReqData): Call<ResponseBody>

    @POST("chatbot/initializeInstances")
    fun initializeInstances(@Body request: Data1): Call<ResponseBody>
}
