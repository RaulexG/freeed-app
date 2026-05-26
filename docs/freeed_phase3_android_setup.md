# FreeEd Android - Fase 3

Esta fase deja el cliente Android preparado para conectarse con Supabase real.

## Incluye

- dependencias para Navigation Compose
- dependencias para ViewModel y StateFlow en Compose
- cliente Supabase configurado
- Auth, Postgrest, Storage y Realtime instalados
- manejo de deeplinks para Auth
- theme visual base de FreeEd
- `NavHost` con grafos anidados
- shell visual y rutas preparadas para las siguientes fases

## Variables requeridas

Agregar a `local.properties` o variables de entorno:

```properties
FREEED_SUPABASE_URL=https://tu-proyecto.supabase.co
FREEED_SUPABASE_PUBLISHABLE_KEY=sb_publishable_xxx
FREEED_AUTH_SCHEME=freeed
FREEED_AUTH_HOST=auth
```

## Notas

- usar solo la publishable key en Android
- no usar la secret key en el cliente
- el deeplink base esperado es `freeed://auth`
- `MainActivity` ya llama `handleDeeplinks(intent)`
- la navegacion actual es un shell tecnico listo para Auth real en Fase 4

