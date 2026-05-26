# FreeEd Android - Fase 1

Este documento define la arquitectura cliente de FreeEd antes de implementar navegacion real, cliente Supabase y pantallas de negocio.

## Objetivo de la fase

- dejar la base estructural del proyecto Android
- definir modulos y responsabilidades
- fijar la estrategia de navegacion Single Activity
- definir contratos de rutas con IDs
- establecer modelos base del dominio
- preparar el terreno para integrar Supabase sin rehacer codigo

## Principios rectores

- Single Activity con Jetpack Compose
- Navigation Compose con rutas limpias y argumentos minimos
- MVVM por feature
- ViewModel + StateFlow
- Repository como frontera entre UI y Supabase
- modelos de dominio desacoplados del SDK
- UI centrada en perfil profesional, no en marketplace generico

## Capas recomendadas

### app

Contiene el entry point de Android, el shell de la app y los contratos globales de navegacion.

### core

Contiene piezas compartidas:

- modelos base
- estados de UI reutilizables
- componentes de design system
- utilidades comunes

### domain

Representa el lenguaje del negocio:

- perfiles
- servicios
- solicitudes
- portafolio
- assets

En fases posteriores agregara interfaces de repositorio y casos de uso.

### data

Se agregara en Fase 3 para:

- cliente Supabase
- auth
- queries a PostgreSQL
- manejo de Storage
- mappers DTO -> domain

### feature

Cada modulo funcional vivira como feature:

- splash
- auth
- onboarding
- home
- explore
- profile
- services
- requests
- favorites
- portfolio
- settings

## Navegacion propuesta

La app se divide en tres grafos principales:

1. AuthGraph
- splash
- login
- register

2. OnboardingGraph
- role_selection
- student_profile_setup
- company_profile_setup

3. MainGraph
- home
- explore
- create_service
- requests
- my_profile

Pantallas de detalle fuera de tabs:

- student_profile/{profileId}
- service_detail/{serviceId}
- service_editor/{serviceId}
- request_detail/{requestId}
- portfolio_item/{portfolioItemId}

## Regla de argumentos

Solo se navega con datos minimos:

- `profileId`
- `serviceId`
- `requestId`
- `portfolioItemId`

Nunca se pasan objetos completos por ruta.

## Resolucion de destino inicial

La app resolvera su punto de entrada con esta logica:

1. si no hay sesion -> `login`
2. si hay sesion y perfil en onboarding -> `role_selection` o setup correspondiente
3. si hay sesion y perfil activo -> `home`
4. si el rol es empresa, la experiencia principal sigue entrando a `home`, pero con contenido adaptado

## Responsabilidad por feature

### splash

- revisar sesion
- revisar perfil
- redireccionar al grafo correcto

### auth

- login
- registro
- sesion persistente
- logout

### onboarding

- seleccion de rol
- completar perfil inicial estudiante
- completar perfil inicial empresa

### home

- descubrimiento principal
- categorias
- destacados
- CTA de crecimiento profesional

### explore

- busqueda
- filtros
- lista de estudiantes o servicios

### profile

- perfil publico de estudiante
- perfil propio
- tabs: portafolio, servicios, sobre mi

### services

- crear servicio
- editar
- listar
- detalle
- gestionar estado

### requests

- solicitudes enviadas
- solicitudes recibidas
- detalle y cambios de estado

### favorites

- guardados por empresa

### portfolio

- proyectos
- evidencias
- links
- documentos

### settings

- preferencias
- privacidad basica
- cerrar sesion

## Estructura de paquetes aterrizada

```text
com.raulcn.freeed
├── app
│   ├── FreeEdApp.kt
│   └── navigation
├── core
│   ├── model
│   └── ui
├── domain
│   └── model
└── feature
    ├── auth
    ├── home
    ├── onboarding
    ├── portfolio
    ├── profile
    ├── requests
    ├── services
    └── splash
```

## Estrategia de datos

La UI no hablara directo con tablas de Supabase. El flujo correcto sera:

UI -> ViewModel -> Repository -> Supabase source -> mapper -> domain model

## Modelos base definidos en Fase 1

- `UserRole`
- `ProfileStatus`
- `VisibilityLevel`
- `ServiceStatus`
- `ServiceModality`
- `RequestStatus`
- `ProfileSummary`
- `StudentProfile`
- `CompanyProfile`
- `Category`
- `Service`
- `ServiceRequest`
- `PortfolioItem`
- `MediaAsset`

## Criterios para considerar cerrada la Fase 1

- arquitectura documentada
- paquetes base creados
- entry point limpio en `MainActivity`
- contratos de rutas definidos
- modelos base definidos
- estados iniciales de features creados

## Lo que NO entra en esta fase

- dependencias Supabase
- Navigation Compose real
- cliente remoto
- autenticacion
- CRUDs
- formularios finales
- pantallas de negocio productivas

