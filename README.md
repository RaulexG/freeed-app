# FreeEd

Aplicación Android nativa que conecta estudiantes universitarios con empresas: los estudiantes publican servicios, construyen portafolio y reciben oportunidades; las empresas exploran talento, guardan favoritos y envían solicitudes.

## Stack

- **Android**: Kotlin + Jetpack Compose, Material 3, Navigation Compose, MVVM (ViewModel + StateFlow), Single Activity.
- **Backend**: Supabase (Postgres con RLS + Auth + Storage + Realtime).
- **Imágenes**: Coil 2.
- **Compilación**: Gradle Kotlin DSL, version catalog en [gradle/libs.versions.toml](gradle/libs.versions.toml).
- **minSdk**: 26 — **compileSdk/targetSdk**: 36.

## Estructura

```
com.raulcn.freeed
├── app           // MainActivity, FreeEdApp shell, navigation, sesión global
├── core          // enums (UserRole, ProfileStatus, ServiceStatus...) y UiState base
├── domain/model  // modelos puros (AppUserProfile, Service, ServiceRequest, PortfolioItem, ...)
├── data          // remote/supabase (Client, Config, DTOs) + repository/ (Auth, Profile, Browse, Services, Portfolio, Requests, Favorites, Assets, Dashboard)
├── feature       // splash, auth, onboarding, home (home+explore+create), profile, services, portfolio, requests, favorites, system
└── ui/theme      // tema Compose (Color, Theme, Type)
```

Cada feature usa el patrón `Routes.kt` (composables) + `ViewModel.kt` + `UiState.kt`. La frontera entre UI y Supabase la cruza siempre un Repository.

## Funcionalidades

### Estudiante

- Registro con rol fijo (`raw_user_meta_data.role = "student"`) y onboarding obligatorio.
- Perfil con tabs: Sobre mí · Servicios · Portafolio.
- CRUD de servicios (draft / published / paused / archived) con categoría, modalidad y precio.
- CRUD de portafolio (proyecto, freelance, práctica, voluntariado, competencia, certificación) con visibilidad pública/privada.
- Bandeja de solicitudes recibidas con transiciones controladas: pending → accepted/rejected → in_progress → completed (cancel en cualquier estado abierto).

### Empresa

- Registro con rol `company` y onboarding de negocio.
- Explorar servicios publicados con búsqueda libre y filtros por categoría y modalidad.
- Ver perfil público del estudiante (header + tabs reusados).
- Enviar solicitud desde el detalle del servicio (título, mensaje, presupuesto y deadline opcionales).
- Bandeja de solicitudes enviadas con seguimiento del estado.
- Favoritos: botón corazón en cards y detalle, `FavoritesRoute` con quitar inline.

### Transversal

- Anon browsing: usuarios sin sesión pueden explorar servicios visibles, categorías y perfiles públicos (RLS extendido con `anon, authenticated`).
- Upload de imágenes a `public-media` (avatar de perfil, imagen de servicio, portada de portafolio) vía Android Photo Picker + Coil para mostrar.
- RLS por rol: las acciones que el backend no permite tampoco aparecen como botones en la UI (validación 1:1 con triggers de Postgres).

## Backend (Supabase)

Las migraciones viven en [supabase/migrations](supabase/migrations) y se aplican en orden cronológico:

1. `202605140001_freeed_initial_schema.sql` — schema completo (enums, tablas, índices, triggers, funciones helper).
2. `202605140002_freeed_hardening.sql` — políticas RLS endurecidas + revokes en funciones internas con `security definer`.
3. `202605250001_guest_browsing_access.sql` y `202605260001_freeed_v1_public_browse_and_scope.sql` — apertura controlada de lectura pública para invitados.
4. `202605260002_fix_profile_completion_trigger.sql` — corrige trigger compartido que referenciaba `new.id` en tablas sin esa columna.
5. `202605270001_fix_storage_owner_path.sql` — corrige `split_part(..., '\\', 1)` por `'/'` para que la policy de Storage funcione.
6. Migraciones `202605270002+` — skills y scopes adicionales de assets.

Las tablas principales son `profiles` / `student_profiles` / `company_profiles`, `services`, `service_assets`, `portfolio_items`, `portfolio_assets`, `service_requests`, `favorite_services`, `assets`, `categories` y `skills`.

## Cómo correr

1. Clonar el repo y abrir en Android Studio.
2. Crear `local.properties` (ignorado por git) con:

```properties
FREEED_SUPABASE_URL=https://<tu-proyecto>.supabase.co
FREEED_SUPABASE_PUBLISHABLE_KEY=sb_publishable_xxx
FREEED_AUTH_SCHEME=freeed
FREEED_AUTH_HOST=auth
```

3. Aplicar las migraciones SQL en orden contra tu instancia de Supabase (SQL Editor o CLI).
4. Crear los buckets `public-media` (público) y `private-documents` (privado).
5. Run `:app` desde Android Studio o `./gradlew :app:installDebug`.

**Importante:** usar siempre la *publishable key*, nunca la service key en el cliente.

## Documentación adicional

- [docs/freeed_phase1_android_architecture.md](docs/freeed_phase1_android_architecture.md) — arquitectura cliente y principios.
- [docs/freeed_phase3_android_setup.md](docs/freeed_phase3_android_setup.md) — setup técnico.
- [docs/freeed_v1_backend_blueprint.md](docs/freeed_v1_backend_blueprint.md) — alcance funcional v1.
- [docs/freeed_supabase_architecture.md](docs/freeed_supabase_architecture.md) — responsabilidades backend vs cliente.
