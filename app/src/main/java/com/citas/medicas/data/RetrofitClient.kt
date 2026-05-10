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
    private const val BASE_URL = "http://10.0.2.2:3000/"
    fun getApiService(context: Context): ApiService {
        val authInterceptor = Interceptor { chain ->
            val prefs = context.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)
            val token = prefs.getString("token_jwt", "") // Recuperar lo que se guardó en el login

            val requestBuilder = chain.request().newBuilder()
            if (!chain.request().url.encodedPath.contains("/auth/login")) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val response = chain.proceed(requestBuilder.build())

            if (response.code == 401) {
                val lastRolString = prefs.getString("user_role", "-1")
                val lastRole = lastRolString?.toIntOrNull() ?: -1
                prefs.edit().remove("token_jwt").apply()

                val intent = when (lastRole) {
                    RolesUsuario.ID_ADMIN -> Intent(context, LoginActivity::class.java)
                    RolesUsuario.ID_MEDICO -> Intent(context, LoginActivity::class.java)
                    RolesUsuario.ID_PACIENTE -> Intent(context, LoginActivity::class.java)
                    else -> Intent(context, LoginActivity::class.java)
                }

                intent.apply {
                    putExtra("session_expired", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }

            response

        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)     // poner el token
            .addInterceptor(loggingInterceptor)  // imprimir en consola
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
