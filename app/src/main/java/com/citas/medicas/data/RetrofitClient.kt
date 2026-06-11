package com.citas.medicas.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.citas.medicas.utils.SessionDialogHelper
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

            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val path = originalRequest.url.encodedPath

                val prefs = context.applicationContext.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
                val token = prefs.getString("token_jwt", "")

                val requestBuilder = originalRequest.newBuilder()

                // Inyecta token a todo (incluyendo auth/logout) EXCEPTO al refresh_token
                if (!token.isNullOrEmpty() && !path.contains("auth/refresh_token")) {
                    val tokenFormateado = if (token.startsWith("Bearer ")) token else "Bearer $token"
                    requestBuilder.addHeader("Authorization", tokenFormateado)
                }

                val response = chain.proceed(requestBuilder.build())

                // Captura expiración global del middleware de Node.js
                if (response.code == 401 && !path.contains("auth/refresh_token")) {
                    val responseBodyString = response.peekBody(Long.MAX_VALUE).string()

                    if (responseBodyString.contains("\"expired\":true")) {
                        Handler(Looper.getMainLooper()).post {
                            SessionDialogHelper.mostrarDialogoExpiracion(context)
                        }
                    }
                }

                response
            }

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

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