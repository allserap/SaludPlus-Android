package com.citas.medicas.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.citas.medicas.utils.SessionDialogHelper // Tu helper centralizado
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

            // Inyectar Token y capturar Expiración de Sesión
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()

                val prefs = context.applicationContext.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
                val token = prefs.getString("token_jwt", "")

                val requestBuilder = originalRequest.newBuilder()

                if (!token.isNullOrEmpty()) {
                    val tokenFormateado = if (token.startsWith("Bearer ")) token else "Bearer $token"
                    requestBuilder.addHeader("Authorization", tokenFormateado)
                }

                // Ejecutar la petición hacia el servidor de Railway
                val response = chain.proceed(requestBuilder.build())

                // Capturar la respuesta del middleware de Node.js
                if (response.code == 401) {
                    val responseBodyString = response.peekBody(Long.MAX_VALUE).string()

                    // Buscar el flag exacto que manda tu backend
                    if (responseBodyString.contains("\"expired\":true")) {

                        // Saltar de forma segura al hilo principal (UI Thread) para mostrar el AlertDialog
                        Handler(Looper.getMainLooper()).post {
                            SessionDialogHelper.mostrarDialogoExpiracion(context)
                        }
                    }
                }

                response
            }

            // Agregar un Logging Interceptor para poder auditar el tráfico en Logcat
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Construir el cliente OkHttp enlazando los interceptores y tiempos
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor) // Sigue consumiéndose aquí de manera transparente
                .addInterceptor(loggingInterceptor)
                .build()

            // Inicializar Retrofit
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