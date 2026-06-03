package com.citas.medicas.data

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api-isss-idsn411-production.up.railway.app/"
    private var retrofit: Retrofit? = null

    private fun getClient(context: Context): Retrofit {
        if (retrofit == null) {

            // 1. Definimos el interceptor para agregar el Token JWT de forma dinámica
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()

                val prefs = context.applicationContext.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
                val token = prefs.getString("token_jwt", "")

                val requestBuilder = originalRequest.newBuilder()

                if (!token.isNullOrEmpty()) {
                    val tokenFormateado = if (token.startsWith("Bearer ")) token else "Bearer $token"
                    requestBuilder.addHeader("Authorization", tokenFormateado) // <-- Corregido aquí
                }

                chain.proceed(requestBuilder.build())
            }

            // 2. Agregamos un Logging Interceptor para poder auditar el tráfico en Logcat
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // 3. Construimos el cliente OkHttp enlazando los interceptores y tiempos
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

            // 4. Inicializamos Retrofit
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getApiService(context: Context): ApiService {
        return getClient(context).create(ApiService::class.java)
    }
}