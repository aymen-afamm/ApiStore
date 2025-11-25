package com.example.fakeapistore.network

import com.example.fakeapistore.models.Product
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FakeStoreApiService {

    @GET("products")
    fun getAllProducts(): Call<List<Product>>

    @GET("products/categories")
    fun getCategories(): Call<List<String>>

    @GET("products/category/{category}")
    fun getProductsByCategory(@Path("category") category: String): Call<List<Product>>
}