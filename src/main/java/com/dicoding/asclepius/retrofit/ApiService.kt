package com.dicoding.asclepius.retrofit

import com.dicoding.asclepius.response.ArticleResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("top-headlines")
    suspend fun getCancerArticles(
        @Query("q") query: String = "cancer",
        @Query("category") category: String = "health",
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String = "d7455a658fc74bc1b50bafc9459bd1b3"
    ): ArticleResponse
}
