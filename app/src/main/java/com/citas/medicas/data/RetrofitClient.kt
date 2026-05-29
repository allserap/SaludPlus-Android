package com.citas.medicas.data

import android.content.Context
import android.content.Intent
import com.citas.medicas.ui.auth.LoginActivity
import com.citas.medicas.utils.RolesUsuario
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api-isss-idsn411-production.up.railway.app/"
    private var retrofit: Retrofit? = null

    private fun getClient(context: Context): Retrofit {
        if (retrofit == null) {
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()

                val prefs = context.applicationContext.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
                val token = prefs.getString("token_jwt", "")

                val requestBuilder = originalRequest.newBuilder()

                if (!token.isNullOrEmpty()) {
                    val tokenFormateado = if (token.startsWith("Bearer ")) token else "Bearer $token"
                    requestBuilder.addHeader("Authorization", token)
                }

                chain.proceed(requestBuilder.build())
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
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
