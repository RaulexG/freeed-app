# FreeEd V1 Backend Blueprint

## Objetivo

FreeEd v1 debe sentirse como una app real y terminable para proyecto final:

- explorar antes de registrarse
- registrarse con correo normal
- elegir rol `student` o `company`
- completar perfil minimo
- publicar servicios
- mostrar portafolio
- enviar y gestionar solicitudes

No buscamos una plataforma enorme. Buscamos una experiencia limpia, coherente y funcional.

## Decisiones de producto

### Se mantiene

- Supabase Auth
- PostgreSQL relacional
- Supabase Storage
- RLS
- perfiles por rol
- servicios
- portafolio
- solicitudes
- favoritos

### Se simplifica o se deja fuera de v1

- sin reviews obligatorias
- sin validacion institucional
- sin empresas verificadas como flujo principal
- sin chat
- sin pagos
- sin notificaciones push
- sin panel admin

## Roles

### Invitado

Puede:

- ver categorias
- ver perfiles publicos
- ver servicios publicados
- ver portafolio publico

No puede:

- publicar
- guardar favoritos
- enviar solicitudes
- editar perfil

### Estudiante

Puede:

- completar perfil profesional
- agregar habilidades
- publicar servicios
- construir portafolio
- recibir solicitudes
- responder solicitudes

### Empresa

Puede:

- completar perfil de negocio
- explorar estudiantes y servicios
- guardar favoritos
- enviar solicitudes
- consultar sus solicitudes enviadas

## Registro

No se usa correo institucional.

Datos minimos de registro:

- email
- password
- display_name
- role

Luego el onboarding completa los campos faltantes.

## Tablas v1 que si usamos

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

## Tablas o funciones no prioritarias en v1

- `reviews`
- `request_status_history`
- `is_verified` como feature visible de producto
- `logo_asset_id` como requisito obligatorio

Pueden quedarse en schema sin estorbar, pero no forman parte del flujo central.

## Storage

### Bucket publico

`public-media`

Para:

- avatar de estudiante
- avatar o imagen de empresa
- imagenes de servicios
- imagenes de portafolio

### Bucket privado

`private-documents`

Para:

- CV
- PDFs de portafolio
- adjuntos de solicitudes

## Rutas recomendadas de objetos

- `public-media/{user_id}/avatar/{file}`
- `public-media/{user_id}/services/{service_id}/{file}`
- `public-media/{user_id}/portfolio/{portfolio_item_id}/{file}`
- `public-media/{user_id}/company/{file}`
- `private-documents/{user_id}/cv/{file}`
- `private-documents/{user_id}/requests/{request_id}/{file}`

## Politicas v1 importantes

### Acceso anonimo

Lectura publica para:

- `profiles` visibles
- `student_profiles` visibles
- `company_profiles` visibles
- `categories` activas
- `skills` activas
- `profile_skills` de perfiles visibles
- `services` publicadas
- `service_assets` de servicios visibles
- `portfolio_items` publicos
- `portfolio_assets` de portafolio publico
- `storage.objects` del bucket `public-media`

### Acceso autenticado

Cada usuario autenticado maneja solo su informacion, salvo:

- empresa puede crear solicitudes
- empresa puede guardar favoritos
- estudiante puede publicar servicios y portafolio
- participantes de una solicitud pueden leerla y actualizar su estado

## Estados simplificados

### Perfil

- `onboarding`
- `active`
- `suspended`
- `archived`

### Servicio

- `draft`
- `published`
- `paused`
- `archived`

### Solicitud

En la app v1 usaremos principalmente:

- `pending`
- `accepted`
- `rejected`
- `completed`

El enum puede seguir teniendo mas estados sin que la app dependa de todos desde el inicio.
