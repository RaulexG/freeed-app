# FreeEd Supabase Architecture

Este documento resume como queda pensada la base de datos y que responsabilidad vive en la app vs Supabase.

## 1. Registro y roles

- El rol se elige en el registro: `student` o `company`.
- La app debe enviar ese rol dentro de `raw_user_meta_data` al hacer sign up con Supabase Auth.
- Un trigger en `auth.users` crea automaticamente:
  - `profiles`
  - `student_profiles` si el rol es estudiante
  - `company_profiles` si el rol es empresa
- El rol queda bloqueado para usuarios autenticados despues de creado. Esto evita corrupcion de datos y simplifica la logica productiva.

Metadata recomendada en registro:

```json
{
  "role": "student",
  "display_name": "Raul",
  "university_name": "Universidad X",
  "degree_program": "Ingenieria en Sistemas",
  "semester": "6"
}
```

Ejemplo empresa:

```json
{
  "role": "company",
  "display_name": "Cafe Central",
  "business_name": "Cafe Central",
  "industry": "Alimentos y bebidas",
  "contact_person_name": "Laura Perez"
}
```

## 2. Modelo principal

### Perfil base

- `profiles`: datos compartidos para todos los usuarios.
- `student_profiles`: campos academicos y CV.
- `company_profiles`: datos del negocio y verificacion.

### Oferta profesional

- `categories`: catalogo de categorias.
- `skills`: catalogo de habilidades.
- `profile_skills`: relacion perfil-habilidad.
- `services`: servicios publicados por estudiantes.
- `portfolio_items`: proyectos y evidencias del estudiante.
- `reviews`: valoraciones de empresas despues de trabajos completados.

### Oportunidades

- `service_requests`: solicitudes enviadas por empresas.
- `request_status_history`: historial de cambios de estado.
- `favorite_services`: favoritos de empresas.

### Archivos

- `assets`: catalogo de archivos en Storage.
- `service_assets`: galeria de servicios.
- `portfolio_assets`: evidencias visuales o documentales.
- `request_assets`: adjuntos de solicitudes.

## 3. Buckets de Storage

Se crean dos buckets:

- `public-media`
  - avatares
  - logos
  - imagenes de servicios
  - imagenes de portafolio
- `private-documents`
  - CVs
  - PDFs de portafolio
  - adjuntos de solicitudes

Convencion recomendada de rutas:

- `public-media/{user_id}/profiles/avatar.webp`
- `public-media/{user_id}/companies/logo.webp`
- `public-media/{user_id}/services/{service_id}/cover.webp`
- `public-media/{user_id}/portfolio/{portfolio_item_id}/preview.webp`
- `private-documents/{user_id}/cv/cv.pdf`
- `private-documents/{user_id}/portfolio/{portfolio_item_id}/evidence.pdf`
- `private-documents/{user_id}/requests/{request_id}/brief.pdf`

Siempre que la app suba un archivo debe:

1. Subir el objeto al bucket correcto.
2. Insertar su metadata en `assets`.
3. Relacionarlo con su entidad (`service_assets`, `portfolio_assets`, `request_assets` o referencia directa en perfil).

## 4. Logica que vive en Supabase

- creacion automatica del perfil desde Auth
- seguridad por rol con RLS
- control de acceso a perfiles, servicios y solicitudes
- integridad de relaciones
- validacion de cambios de estado en solicitudes
- historial de estados
- proteccion de documentos privados
- actualizacion de `updated_at`
- activacion automatica del perfil cuando cumple datos minimos

## 5. Logica que vive en la app Android

- formularios
- onboarding
- validaciones UX adicionales
- ordenar y paginar resultados
- subir archivos y despues registrar `assets`
- mostrar mensajes de error amigables
- generar signed URLs cuando se requiera para documentos privados
- decidir si un servicio pasa de `draft` a `published`

## 6. Flujo recomendado de la app

### Estudiante

1. Se registra con rol `student`.
2. Completa perfil.
3. Agrega habilidades.
4. Sube avatar y opcionalmente CV.
5. Crea servicios.
6. Agrega portafolio.
7. Recibe solicitudes.

### Empresa

1. Se registra con rol `company`.
2. Completa perfil de negocio.
3. Explora estudiantes y servicios.
4. Guarda favoritos.
5. Crea una solicitud.
6. Puede adjuntar brief o documento.
7. Deja review cuando el trabajo termina.

## 7. Estados importantes

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

- `pending`
- `accepted`
- `rejected`
- `in_progress`
- `completed`
- `cancelled`

Transiciones protegidas en base de datos:

- `pending -> accepted/rejected` solo estudiante
- `accepted -> in_progress` participantes
- `in_progress -> completed` participantes
- `pending/accepted/in_progress -> cancelled` participantes

## 8. Nota importante de seguridad

Si alguna key secreta de Supabase fue expuesta visualmente, debe rotarse antes de continuar el desarrollo productivo.
