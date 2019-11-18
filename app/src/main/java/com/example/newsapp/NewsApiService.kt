package com.example.newsapp

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
        @Query("sortBy") sortBy: String,
        @Query("apiKey") apiKey: String
    ): Observable<Model.Result>

    companion object {
        fun create(): NewsApiService = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://newsapi.org/v2/")
                .build()
                .create(NewsApiService::class.java)
    }
}