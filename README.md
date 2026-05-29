# FreeEd

FreeEd es una aplicacion Android orientada al crecimiento profesional temprano de estudiantes universitarios. La plataforma permite que estudiantes construyan un perfil profesional, publiquen servicios, armen un portafolio y reciban solicitudes de pequenas empresas o negocios que buscan talento universitario emergente.

El proyecto fue construido como una app real, con backend remoto en Supabase, arquitectura modular y una experiencia diferenciada para invitado, estudiante y empresa.

## Enfoque del producto

FreeEd no busca sentirse como un marketplace generico tipo Fiverr o Freelancer. Su centro es:

- perfil profesional estudiantil
- experiencia temprana comprobable
- portafolio dinamico
- oportunidades reales con pequenos negocios

### Roles principales

**Invitado**
- puede explorar servicios, categorias y perfiles publicos
- puede navegar por la app antes de registrarse

**Estudiante**
- crea perfil profesional
- publica servicios
- construye portafolio
- recibe solicitudes

**Empresa**
- crea perfil de negocio
- explora talento y servicios
- guarda favoritos
- envia solicitudes

## Stack tecnologico

### Android
- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- MVVM
- ViewModel + StateFlow

### Backend
- Supabase Auth
- Supabase PostgreSQL
- Supabase Storage
- Supabase Realtime

### Librerias principales
- Coil
- Ktor Client
- Kotlinx Serialization

### SDKs y versiones base
- `minSdk`: 26
- `compileSdk`: 36
- `targetSdk`: 36

## Estado actual del proyecto

La aplicacion ya integra flujo real con backend, incluyendo:

- exploracion publica como invitado
- registro e inicio de sesion con Supabase Auth
- seleccion de rol
- perfiles de estudiante y empresa
- servicios publicados
- portafolio basico
- favoritos
- solicitudes
- carga de imagenes con Storage

## Arquitectura del proyecto

El proyecto sigue una estructura modular por capas y features:

```text
app/src/main/java/com/raulcn/freeed
├── app
│   ├── FreeEdApp.kt
│   ├── navigation
│   └── session
├── core
│   ├── model
│   └── ui
├── data
│   ├── remote/supabase
│   └── repository
├── domain
│   └── model
├── feature
│   ├── auth
│   ├── favorites
│   ├── home
│   ├── onboarding
│   ├── portfolio
│   ├── profile
│   ├── requests
│   ├── services
│   └── splash
└── ui/theme
```

### Capas

**app**
- punto de entrada
- navegacion
- estado global de sesion

**core**
- enums y modelos compartidos
- utilidades base de UI

**domain**
- modelos del negocio

**data**
- cliente Supabase
- DTOs
- repositories

**feature**
- modulos funcionales de la app

## Funcionalidades principales

### Estudiante
- registro con rol `student`
- onboarding del perfil profesional
- publicacion y gestion de servicios
- portafolio con evidencias
- bandeja de solicitudes recibidas
- perfil con resumen de actividad

### Empresa
- registro con rol `company`
- perfil de negocio
- exploracion de servicios y talento
- favoritos
- solicitudes enviadas
- seguimiento de oportunidades

### Invitado
- home publico
- exploracion de servicios visibles
- acceso al login y registro desde puntos protegidos

## Backend con Supabase

Supabase se usa como backend remoto real del proyecto.

### Servicios utilizados
- Auth
- Postgrest
- Storage
- Realtime

### Tablas principales
- `profiles`
- `student_profiles`
- `company_profiles`
- `categories`
- `skills`
- `profile_skills`
- `services`
- `assets`
- `service_assets`
- `portfolio_items`
- `portfolio_assets`
- `favorite_services`
- `service_requests`

### Buckets
- `public-media`
- `private-documents`

### Migraciones principales
Las migraciones del proyecto viven en:

```text
supabase/migrations/
```

Entre las mas importantes:
- `202605140001_freeed_initial_schema.sql`
- `202605140002_freeed_hardening.sql`
- `202605250001_guest_browsing_access.sql`
- `202605260001_freeed_v1_public_browse_and_scope.sql`
- `202605260002_fix_profile_completion_trigger.sql`
- `202605270001_fix_storage_owner_path.sql`
- `202605270002_allow_authenticated_skill_insert.sql`
- `202605270003_assets_read_public_scope.sql`

## Cuentas demo

Estas cuentas ya fueron creadas para probar la app:

### Estudiantes
- `anatorresdemo@gmail.com`
- `diegoortizdemo@gmail.com`
- `carlosmendezdemo@gmail.com`

### Empresa
- `impulsalabdemo@gmail.com`

### Contrasena
- `freee123`

## Como ejecutar el proyecto

1. Clona o descarga este repositorio.
2. Abre el proyecto en Android Studio.
3. Asegurate de tener configurado `local.properties`.
4. Sincroniza Gradle.
5. Ejecuta la app en un emulador o dispositivo.

### Configuracion requerida en `local.properties`

```properties
FREEED_SUPABASE_URL=https://dekuwukbuzjqqsrrxknf.supabase.co
FREEED_SUPABASE_PUBLISHABLE_KEY=sb_publishable_xxx
FREEED_AUTH_SCHEME=freeed
FREEED_AUTH_HOST=auth
```

### Importante
- usa solo la `publishable key`
- nunca pongas la `secret key` dentro de la app Android

## Flujo general de uso

### Invitado
1. entra a la app
2. explora categorias y servicios
3. intenta acceder a una accion protegida
4. se le pide iniciar sesion o crear cuenta

### Estudiante
1. se registra
2. selecciona rol estudiante
3. completa perfil
4. publica servicios
5. construye portafolio
6. recibe solicitudes

### Empresa
1. se registra
2. selecciona rol empresa
3. completa perfil
4. explora talento
5. guarda favoritos
6. envia solicitudes

## Archivos clave del proyecto

### Navegacion y sesion
- `app/src/main/java/com/raulcn/freeed/app/FreeEdApp.kt`
- `app/src/main/java/com/raulcn/freeed/app/navigation/FreeEdNavHost.kt`
- `app/src/main/java/com/raulcn/freeed/app/session/AppSessionViewModel.kt`

### Conexion con Supabase
- `app/src/main/java/com/raulcn/freeed/data/remote/supabase/SupabaseConfig.kt`
- `app/src/main/java/com/raulcn/freeed/data/remote/supabase/SupabaseClientProvider.kt`

### Repositories
- `AuthRepository.kt`
- `ProfileRepository.kt`
- `BrowseRepository.kt`
- `ServicesRepository.kt`
- `PortfolioRepository.kt`
- `RequestsRepository.kt`
- `FavoritesRepository.kt`

### UI principal
- `HomeRoutes.kt`
- `AuthRoutes.kt`
- `ProfileRoutes.kt`
- `RequestRoutes.kt`
- `FavoritesRoutes.kt`
- `ServiceRoutes.kt`

## Documentacion interna

Documentos tecnicos disponibles en:

- `docs/freeed_phase1_android_architecture.md`
- `docs/freeed_phase3_android_setup.md`
- `docs/freeed_supabase_architecture.md`
- `docs/freeed_v1_backend_blueprint.md`

## Notas

- el proyecto esta orientado a un MVP academico serio y funcional
- se priorizo el nucleo del producto sobre funciones extra como chat, pagos o reviews
- la app se sigue puliendo visualmente para acercarse a una experiencia mas profesional

## Referencias base

- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation)
- [Supabase Docs](https://supabase.com/docs)
- [Material 3](https://m3.material.io/)
