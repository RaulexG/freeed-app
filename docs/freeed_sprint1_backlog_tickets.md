# FreeEd Sprint 1 Backlog (MVP diferenciador)

## Objetivo del sprint

Activar el eje de crecimiento profesional estudiantil con habilidades reales, exploracion por talento y reglas solidas de solicitudes.

## Ticket S1-001 - Skills end-to-end (base)
- Prioridad: P1
- Estimacion: 2.5 dias
- Dependencias: ninguna
- Alcance:
  - Implementar `SkillsRepository` para catalogo de skills y skills del estudiante autenticado.
  - Integrar carga de skills en `ProfileRepository.getCurrentProfile()` para rol estudiante.
  - Soportar reemplazo atomico de skills del usuario (delete + insert por lista seleccionada).
- Criterios de aceptacion:
  - Estudiante autenticado obtiene sus skills actuales desde `profile_skills`.
  - App puede consultar catalogo activo de `skills`.
  - App puede guardar una nueva seleccion de skills del estudiante.

## Ticket S1-002 - Onboarding/Edicion de skills en UI
- Prioridad: P1
- Estimacion: 2 dias
- Dependencias: S1-001
- Alcance:
  - UI de seleccion de skills (busqueda + chips) en onboarding estudiante.
  - UI de edicion de skills desde perfil propio.
  - Persistencia mediante `SkillsRepository`.
- Criterios de aceptacion:
  - Usuario puede agregar/quitar skills y verlas al reabrir la pantalla.

## Ticket S1-003 - Skills en perfil publico del estudiante
- Prioridad: P1
- Estimacion: 1 dia
- Dependencias: S1-001
- Alcance:
  - Extender `StudentProfileDetailViewModel` para cargar skills del perfil publico.
  - Mostrar bloque visual de skills en `StudentProfileDetailRoute`.
- Criterios de aceptacion:
  - Perfil publico muestra skills reales de la base.

## Ticket S1-004 - Filtros de exploracion por talento
- Prioridad: P1
- Estimacion: 2.5 dias
- Dependencias: S1-001
- Alcance:
  - Filtros por skill, universidad, carrera y semestre en explorar.
  - Soporte en repositorio y estado UI.
- Criterios de aceptacion:
  - Empresa filtra por skill y cambia el resultado.

## Ticket S1-005 - Hardening de transiciones de solicitudes (DB)
- Prioridad: P1
- Estimacion: 1.5 dias
- Dependencias: ninguna
- Alcance:
  - Migracion SQL con validacion de transiciones permitidas por rol/participacion.
  - Bloquear updates invalidos por RLS/funcion.
- Criterios de aceptacion:
  - No se pueden forzar transiciones invalidas via API.

## Ticket S1-006 - Hardening de transiciones de solicitudes (App)
- Prioridad: P1
- Estimacion: 1 dia
- Dependencias: S1-005
- Alcance:
  - Mensajes de error amigables cuando el backend rechaza transiciones.
  - UI muestra solo acciones validas por estado/rol.
- Criterios de aceptacion:
  - UX consistente ante errores de negocio.

## Ticket S1-007 - Pruebas de sprint
- Prioridad: P2
- Estimacion: 1.5 dias
- Dependencias: S1-001, S1-004, S1-006
- Alcance:
  - Unit tests de filtros de explorar.
  - Unit tests de transiciones de solicitudes.
  - Smoke test manual guiado para flujo estudiante/empresa.
- Criterios de aceptacion:
  - `:app:testDebugUnitTest` en verde y checklist QA completado.

## Orden recomendado de ejecucion
1. S1-001
2. S1-005
3. S1-002
4. S1-003
5. S1-004
6. S1-006
7. S1-007
