package com.example.newsapp.main

import com.example.newsapp.models.NewsApiResponse
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("everything")
    fun getData(
        @Query("q") q: String,
        @Query("pageSize") pageSize: Int,
        @Query("page") page: Int,
        @Query("apiKey") apiKey: String
    ): Observable<NewsApiResponse.Result>

    companion object {
        private const val BASE_URL = "https://newsapi.org/v2/"

        fun create(baseUrl: String = BASE_URL): NewsApiService = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
            .create(NewsApiService::class.java)
    }
}