# 🏥 Frontend SaludPlus Android - Sistema de Gestión de Citas Médicas

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=for-the-badge&logo=kotlin)
![Android Studio](https://img.shields.io/badge/Android%20Studio-Hedgehog+-3DDC84?style=for-the-badge&logo=android-studio)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Repository-blue?style=for-the-badge)
![Database](https://img.shields.io/badge/Local%20DB-Room-61DAFB?style=for-the-badge)

## 📌 Descripción

**SaludPlus** es una aplicación móvil nativa desarrollada en Kotlin diseñada para optimizar y automatizar el agendamiento y control de citas médicas. El sistema ofrece una experiencia multi-rol integrada que conecta de manera fluida a Pacientes, Médicos y Administradores, consumiendo una API REST centralizada y asegurando persistencia de datos local.

---

## 🏗️ Arquitectura del Proyecto

La aplicación implementa el patrón de diseño **MVVM (Model-View-ViewModel)** junto con el patrón **Repository**, garantizando una separación limpia de responsabilidades, alta mantenibilidad y desacoplamiento de la lógica de negocio de los componentes del ciclo de vida de Android.

### 📦 Flujo de Capas y Datos

1. **View (UI):** Representada por `Activities` y `Fragments`. Observan el estado expuesto por los ViewModels y renderizan la interfaz mediante XML de manera reactiva.
2. **ViewModel:** Retiene y gestiona los datos UI-related conscientes del ciclo de vida. Se comunica con el repositorio usando **Kotlin Coroutines**.
3. **Repository (`AuthRepository`):** Actúa como la "Single Source of Truth" (Fuente única de verdad). Media la obtención de datos decidiendo estratégicamente si extraerlos del almacenamiento local o de la red.
4. **Data Sources:**
   * **Local:** Persistencia local gestionada mediante **Room Database** (`AppDatabase`).
   * **Remoto:** Cliente HTTP configurado con **Retrofit** y **Gson** para el parseo de solicitudes.

---

## 🔄 Flujo Completo de Operación (Ejemplo: Autenticación)

```text
[ View: LoginActivity ]
       │  (1) Despacha credenciales ingresadas
       ▼
[ ViewModel: AuthViewModel ]
       │  (2) Ejecuta corrutina en Dispatchers.IO
       ▼
[ Repository: AuthRepository ]
       │  (3) Solicita autenticación remota
       ▼
[ API Service: ApiService (Retrofit) ] ──(4) HTTPS POST /api/auth/login──> [ Backend REST API ]
       │                                                                        │
       ▼                                                                        ▼
[ Actualiza UI mediante LiveData ] <──(6) Procesa LoginResponse <── (5) Retorna JSON + JWT
```

---

## 📂 Estructura Real de Paquetes (`com.citas.medicas`)

A diferencia de las estructuras estándar, el proyecto organiza la interfaz de usuario de manera modular orientada al contexto operativo y **roles de usuario**:

```text
com.citas.medicas/
│
├── data/
│   ├── ApiService.kt
│   ├── AuthRepository.kt
│   └── RetrofitClient.kt
│
├── models/
│   ├── ApiResponse.kt
│   ├── CatalogoResponse.kt
│   ├── CitaMedicoResponse.kt
│   ├── ErrorResponse.kt
│   ├── HistorialRequest.kt
│   ├── LoginRequest.kt
│   ├── LoginResponse.kt
│   ├── MedicoResponse.kt
│   ├── MedicoUpdateRequest.kt
│   ├── PacienteResponse.kt
│   ├── PacienteUpdateRequest.kt
│   ├── RegistroRequest.kt
│   ├── RegistroResponse.kt
│   └── Usuario.kt
│
├── ui/
│   ├── admin/
│   ├── auth/
│   ├── base/
│   ├── medico/
│   ├── paciente/
│   └── AppDatabase.kt
│
└── utils/
    ├── RolesUsuario.kt
    ├── SessionManager.kt
    ├── Validation.kt
    └── ViewExtensions.kt
```


## 🎨 Recursos y Componentes de Interfaz (/res/layout)
La aplicación aprovecha una arquitectura de diseño basada en Single Activity + Fragments para los dashboards, usando componentes dinámicos de Android Jetpack:

**Vistas Complejas:** Implementación de mapas interactivos (activity_mapa.xml) para geolocalización de clínicas.

**Componentes UX Avanzados:** Diálogos modales personalizados (dialog_cambiar_clave.xml, dialog_editar_contacto.xml) y paneles deslizables inferiores (bottom_sheet_reprogramar.xml).

**Visualización de listados:** Listas eficientes e infladas mediante Adapters personalizados (UsuariosFragment + UsuarioAdapter, AgendaFragment + CitasAdapter).

## ⚙️ Stack Tecnológico Utilizado

* **Lenguaje:** Kotlin 1.9+ (con programación funcional y extensiones nativas).
* **Compilación y Target SDK:** Compilado con SDK 34 utilizando Java 17 como compatibilidad base (JVM Target 17).
* **Network:** Retrofit 2.9.0 + OkHttp Logging Interceptor 4.12.0 para el monitoreo de peticiones HTTP.
* **Concurrencia:** Kotlin Coroutines 1.7.3 (Manejo asíncrono y seguro de hilos con `Dispatchers.IO`).
* **Local Storage:** Room Database 2.6.1 procesado eficientemente mediante **KSP (Kotlin Symbol Processing)** + SharedPreferences.
* **UI Architecture:** Jetpack LiveData / ViewModel (`lifecycle-ktx:2.7.0`) y ViewBinding activo para la vinculación de vistas XML.
* **Dependencias de Terceros integradas:** * **Glide 4.16.0:** Gestión y caché eficiente de imágenes.
  * **MPAndroidChart v3.1.0:** Generación de reportes gráficos en los paneles de control.
* **Diseño:** Material Design 3 (Componentes dinámicos, Cards, Custom Dialogs, ConstraintLayouts).
* **Seguridad:** `androidx.security:security-crypto` para el manejo seguro de almacenamiento local.

## 🔐 Mecanismo de Seguridad y Sesión
**Intercepción JWT:** Al iniciar sesión exitosamente, el backend retorna un token cifrado.

**Almacenamiento:** El SessionManager intercepta este token guardándolo en memoria local de la app.

**Inyección Automática:** Las llamadas subsecuentes protegidas dentro de ApiService adjuntan la cabecera correspondiente en las peticiones HTTP:

```http
Authorization: Bearer <tu_token_jwt_aqui>
```

## 🚀 Instalación y Ejecución
### Prerrequisitos
* Android Studio (Versión Hedgehog o superior recomendada).
* **JDK 17** configurado explícitamente en el entorno de desarrollo (Gradle Toolchain).
* **Dispositivo de Pruebas:** Emulador o dispositivo físico con Android 5.0 (API 24 - MinSDK) o superior para su correcta ejecución.
1. Clonación del Repositorio
git clone [https://github.com/TU-USUARIO/SaludPlus-Android.git](https://github.com/TU-USUARIO/SaludPlus-Android.git)

2. Sincronización
Abre el proyecto desde Android Studio.
Permite que Gradle descargue las dependencias requeridas (Build > Clean Project o Sync Project with Gradle Files).

3. Configuración de Variables de Entorno (API URL)
Modificar según la IP local de desarrollo o el host de producción. Ej:
private const val BASE_URL = "https://<TU_IP_O_HOST>_VAL_BACKEND:8080/"


## 📄 Licencia
Este software se ha desarrollado bajo un entorno estrictamente académico y de formación técnica profesional para la institución ITCA. Todos los derechos reservados 2026.
