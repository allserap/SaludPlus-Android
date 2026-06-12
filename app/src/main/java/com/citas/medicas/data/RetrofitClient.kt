package com.citas.medicas.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.citas.medicas.ActivityProvider
import com.citas.medicas.utils.SessionDialogHelper
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api-isss-idsn411-production.up.railway.app/"
    @Volatile private var retrofit: Retrofit? = null

    private fun getClient(context: Context): Retrofit {
        // Doble verificación con bloqueo para hilos seguros (Thread-safe)
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(context).also { retrofit = it }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        // Logging Interceptor primero para registrar todo de manera transparente
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Interceptor de Autenticación y manejo de sesión caducada
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val path = originalRequest.url.encodedPath

            val prefs = context.applicationContext.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
            val token = prefs.getString("token_jwt", "")

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                val tokenFormateado = if (token.startsWith("Bearer ")) token else "Bearer $token"
                requestBuilder.addHeader("Authorization", tokenFormateado)
            }

            val response = chain.proceed(requestBuilder.build())

            if (response.code == 401 && !path.contains("auth/logout")) {
                try {
                    val responseBodyString = response.peekBody(2048).string() // 2KB es suficiente para un JSON de error corto
                    if (responseBodyString.contains("\"expired\":true")) {
                        Handler(Looper.getMainLooper()).post {
                            val activity = ActivityProvider.currentActivity
                            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                                SessionDialogHelper.mostrarDialogoExpiracion(activity)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            response
        }

        // Construir el cliente enlazando adecuadamente el orden de interceptores
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor) // Registra la entrada original
            .addInterceptor(authInterceptor)    // Procesa cabeceras y errores lógicos
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(context: Context): ApiService {
        return getClient(context).create(ApiService::class.java)
    }
}