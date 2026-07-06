package dev.aroca.voice2taskapp.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

   // private const val BASE_URL = "http://10.0.2.2:8000/"
    private const val BASE_URL = "http://192.168.1.197:8000/"

    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                token?.let { addHeader("Authorization", "Bearer $it") }
            }.build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val listasApi: ListasApi = retrofit.create(ListasApi::class.java)
    val tareasApi: TareasApi = retrofit.create(TareasApi::class.java)
}