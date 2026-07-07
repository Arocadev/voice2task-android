package dev.aroca.voice2taskapp.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExternalApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val trelloApi: TrelloApi = Retrofit.Builder()
        .baseUrl("https://api.trello.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TrelloApi::class.java)

    val notionApi: NotionApi = Retrofit.Builder()
        .baseUrl("https://api.notion.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NotionApi::class.java)
}