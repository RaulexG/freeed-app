begin;

delete from auth.identities
where provider = 'email'
  and provider_id in (
    'ana.torres.demo@freeed.app',
    'diego.ortiz.demo@freeed.app',
    'carlos.mendez.demo@freeed.app',
    'contacto.impulsalab.demo@freeed.app'
  );

delete from auth.users
where email in (
  'ana.torres.demo@freeed.app',
  'diego.ortiz.demo@freeed.app',
  'carlos.mendez.demo@freeed.app',
  'contacto.impulsalab.demo@freeed.app'
);

insert into auth.users (
  id,
  aud,
  role,
  email,
  encrypted_password,
  email_confirmed_at,
  raw_app_meta_data,
  raw_user_meta_data,
  created_at,
  updated_at,
  is_sso_user,
  is_anonymous
)
values
(
  'a10ac111-1111-4111-8111-111111111111',
  'authenticated',
  'authenticated',
  'ana.torres.demo@freeed.app',
  crypt('FreeEdDemo2026!', gen_salt('bf')),
  timezone('utc', now()),
  '{"provider":"email","providers":["email"]}'::jsonb,
  '{"role":"student","display_name":"Ana Torres"}'::jsonb,
  timezone('utc', now()),
  timezone('utc', now()),
  false,
  false
),
(
  'b20bd222-2222-4222-8222-222222222222',
  'authenticated',
  'authenticated',
  'diego.ortiz.demo@freeed.app',
  crypt('FreeEdDemo2026!', gen_salt('bf')),
  timezone('utc', now()),
  '{"provider":"email","providers":["email"]}'::jsonb,
  '{"role":"student","display_name":"Diego Ortiz"}'::jsonb,
  timezone('utc', now()),
  timezone('utc', now()),
  false,
  false
),
(
  'c30ce333-3333-4333-8333-333333333333',
  'authenticated',
  'authenticated',
  'carlos.mendez.demo@freeed.app',
  crypt('FreeEdDemo2026!', gen_salt('bf')),
  timezone('utc', now()),
  '{"provider":"email","providers":["email"]}'::jsonb,
  '{"role":"student","display_name":"Carlos Mendez"}'::jsonb,
  timezone('utc', now()),
  timezone('utc', now()),
  false,
  false
),
(
  'd40df444-4444-4444-8444-444444444444',
  'authenticated',
  'authenticated',
  'contacto.impulsalab.demo@freeed.app',
  crypt('FreeEdDemo2026!', gen_salt('bf')),
  timezone('utc', now()),
  '{"provider":"email","providers":["email"]}'::jsonb,
  '{"role":"company","display_name":"Impulsa Lab"}'::jsonb,
  timezone('utc', now()),
  timezone('utc', now()),
  false,
  false
);

insert into auth.identities (
  provider_id,
  user_id,
  identity_data,
  provider,
  last_sign_in_at,
  created_at,
  updated_at
)
values
(
  'ana.torres.demo@freeed.app',
  'a10ac111-1111-4111-8111-111111111111',
  '{"sub":"ana.torres.demo@freeed.app","email":"ana.torres.demo@freeed.app","email_verified":true,"phone_verified":false}'::jsonb,
  'email',
  timezone('utc', now()),
  timezone('utc', now()),
  timezone('utc', now())
),
(
  'diego.ortiz.demo@freeed.app',
  'b20bd222-2222-4222-8222-222222222222',
  '{"sub":"diego.ortiz.demo@freeed.app","email":"diego.ortiz.demo@freeed.app","email_verified":true,"phone_verified":false}'::jsonb,
  'email',
  timezone('utc', now()),
  timezone('utc', now()),
  timezone('utc', now())
),
(
  'carlos.mendez.demo@freeed.app',
  'c30ce333-3333-4333-8333-333333333333',
  '{"sub":"carlos.mendez.demo@freeed.app","email":"carlos.mendez.demo@freeed.app","email_verified":true,"phone_verified":false}'::jsonb,
  'email',
  timezone('utc', now()),
  timezone('utc', now()),
  timezone('utc', now())
),
(
  'contacto.impulsalab.demo@freeed.app',
  'd40df444-4444-4444-8444-444444444444',
  '{"sub":"contacto.impulsalab.demo@freeed.app","email":"contacto.impulsalab.demo@freeed.app","email_verified":true,"phone_verified":false}'::jsonb,
  'email',
  timezone('utc', now()),
  timezone('utc', now()),
  timezone('utc', now())
);

update public.profiles
set
  display_name = 'Ana Torres',
  headline = 'UI UX Designer en formacion',
  bio = 'Diseno interfaces limpias y prototipos funcionales para negocios y proyectos universitarios.',
  city = 'CDMX',
  state = 'Ciudad de Mexico'
where id = 'a10ac111-1111-4111-8111-111111111111';

update public.student_profiles
set
  university_name = 'UNAM',
  degree_program = 'Diseno y Comunicacion Visual',
  semester = 7,
  availability_note = 'Disponible por las tardes',
  preferred_work_modality = 'remote'
where profile_id = 'a10ac111-1111-4111-8111-111111111111';

update public.profiles
set
  display_name = 'Diego Ortiz',
  headline = 'Brand Designer y creador visual',
  bio = 'Trabajo branding, piezas visuales y presentaciones para marcas emergentes.',
  city = 'Guadalajara',
  state = 'Jalisco'
where id = 'b20bd222-2222-4222-8222-222222222222';

update public.student_profiles
set
  university_name = 'UAM',
  degree_program = 'Diseno Grafico',
  semester = 6,
  availability_note = 'Disponible en proyectos por entregable',
  preferred_work_modality = 'remote'
where profile_id = 'b20bd222-2222-4222-8222-222222222222';

update public.profiles
set
  display_name = 'Carlos Mendez',
  headline = 'Analista de datos junior',
  bio = 'Creo dashboards, reportes y automatizaciones simples para negocios pequenos.',
  city = 'Monterrey',
  state = 'Nuevo Leon'
where id = 'c30ce333-3333-4333-8333-333333333333';

update public.student_profiles
set
  university_name = 'Tec de Monterrey',
  degree_program = 'Ingenieria en Ciencia de Datos',
  semester = 8,
  availability_note = 'Disponible en remoto',
  preferred_work_modality = 'remote'
where profile_id = 'c30ce333-3333-4333-8333-333333333333';

update public.profiles
set
  display_name = 'Impulsa Lab',
  headline = 'Empresa enfocada en crecimiento digital',
  bio = 'Buscamos talento universitario para proyectos de marketing, diseno y producto digital.',
  city = 'CDMX',
  state = 'Ciudad de Mexico'
where id = 'd40df444-4444-4444-8444-444444444444';

update public.company_profiles
set
  business_name = 'Impulsa Lab',
  industry = 'Consultoria digital',
  contact_person_name = 'Laura Herrera',
  description = 'Equipo pequeno que apoya a negocios con branding, sitios y analitica.'
where profile_id = 'd40df444-4444-4444-8444-444444444444';

insert into public.profile_skills (profile_id, skill_id, level, years_experience)
values
  ('a10ac111-1111-4111-8111-111111111111', '8e3f2729-82c5-49d2-bbb9-7441b5dc94d0', 'advanced', 2.5),
  ('a10ac111-1111-4111-8111-111111111111', '7ac70220-18a1-431f-a35a-91cf8062eda5', 'advanced', 2.0),
  ('a10ac111-1111-4111-8111-111111111111', 'e19fb6d5-8a96-49ec-b1d4-e8aa69e1cc50', 'intermediate', 2.0),
  ('b20bd222-2222-4222-8222-222222222222', '145f8831-1667-4780-97ff-b277a32c8151', 'advanced', 3.0),
  ('b20bd222-2222-4222-8222-222222222222', 'e19fb6d5-8a96-49ec-b1d4-e8aa69e1cc50', 'advanced', 3.0),
  ('c30ce333-3333-4333-8333-333333333333', '437ec543-fb79-40be-99ee-2cad78b56129', 'advanced', 3.5),
  ('c30ce333-3333-4333-8333-333333333333', 'a79b38fd-e41c-4681-94d9-8132d865a95f', 'advanced', 2.5),
  ('c30ce333-3333-4333-8333-333333333333', '20c6fa05-b513-4a96-8040-80f9854bdbc2', 'intermediate', 2.0)
on conflict (profile_id, skill_id) do update
set level = excluded.level,
    years_experience = excluded.years_experience;

insert into public.services (
  id, student_id, category_id, title, short_description, description, price_type, price_amount, currency_code,
  estimated_delivery_days, modality, service_status, tags, published_at
)
values
(
  '11111111-aaaa-4111-8111-111111111111',
  'a10ac111-1111-4111-8111-111111111111',
  'b642741d-4160-4b35-bf5a-4fb8bb4a3e94',
  'Diseno de landing page profesional',
  'Pantalla principal clara y moderna para negocios o proyectos.',
  'Diseno interfaces y landing pages en Figma con enfoque en claridad visual, estructura y conversion.',
  'fixed',
  3500,
  'MXN',
  7,
  'remote',
  'published',
  array['figma','landing-page','ui-ux'],
  timezone('utc', now())
),
(
  '22222222-bbbb-4222-8222-222222222222',
  'b20bd222-2222-4222-8222-222222222222',
  'c0e716e7-4450-4de9-a1ab-73362c795577',
  'Identidad visual para marca emergente',
  'Logo, paleta y piezas base para negocios pequenos.',
  'Creo una identidad visual base con lineamientos y piezas iniciales para redes o presentaciones.',
  'fixed',
  4200,
  'MXN',
  10,
  'remote',
  'published',
  array['branding','canva','diseno'],
  timezone('utc', now())
),
(
  '33333333-cccc-4333-8333-333333333333',
  'c30ce333-3333-4333-8333-333333333333',
  'f7099a93-2090-4990-9df5-ef33bc9dcbf5',
  'Dashboard de ventas y reportes en Excel',
  'Reportes claros para entender ingresos, categorias y tendencias.',
  'Construyo dashboards y reportes operativos en Excel y SQL para visualizar ventas y rendimiento.',
  'fixed',
  3900,
  'MXN',
  6,
  'remote',
  'published',
  array['excel','sql','dashboard'],
  timezone('utc', now())
)
on conflict (id) do update
set title = excluded.title,
    short_description = excluded.short_description,
    description = excluded.description,
    price_type = excluded.price_type,
    price_amount = excluded.price_amount,
    estimated_delivery_days = excluded.estimated_delivery_days,
    modality = excluded.modality,
    service_status = excluded.service_status,
    tags = excluded.tags,
    published_at = excluded.published_at,
    updated_at = timezone('utc', now());

insert into public.portfolio_items (
  id, student_id, item_type, title, description, contribution, visibility, started_on, completed_on
)
values
(
  'aaaa1111-aaaa-4111-8111-aaaaaaaa1111',
  'a10ac111-1111-4111-8111-111111111111',
  'project',
  'Rediseno de plataforma de tutorias',
  'Caso academico donde reestructure el flujo principal y el sistema visual de una app de tutorias.',
  'Research, wireframes y prototipo final en Figma.',
  'public',
  '2025-08-01',
  '2025-10-15'
),
(
  'bbbb2222-bbbb-4222-8222-bbbbbbbb2222',
  'b20bd222-2222-4222-8222-222222222222',
  'freelance_work',
  'Branding para cafeteria local',
  'Identidad visual, menu base y piezas para Instagram para una cafeteria independiente.',
  'Logo, color, plantillas y adaptaciones.',
  'public',
  '2025-09-10',
  '2025-10-05'
),
(
  'cccc3333-cccc-4333-8333-cccccccc3333',
  'c30ce333-3333-4333-8333-333333333333',
  'project',
  'Dashboard comercial para tienda minorista',
  'Panel de control con ventas mensuales, ticket promedio y categorias mas rentables.',
  'Limpieza de datos, formulas y visualizacion ejecutiva.',
  'public',
  '2025-07-20',
  '2025-08-12'
)
on conflict (id) do update
set title = excluded.title,
    description = excluded.description,
    contribution = excluded.contribution,
    visibility = excluded.visibility,
    started_on = excluded.started_on,
    completed_on = excluded.completed_on,
    updated_at = timezone('utc', now());

insert into public.favorite_services (company_id, service_id)
values
  ('d40df444-4444-4444-8444-444444444444', '11111111-aaaa-4111-8111-111111111111'),
  ('d40df444-4444-4444-8444-444444444444', '33333333-cccc-4333-8333-333333333333')
on conflict (company_id, service_id) do nothing;

insert into public.service_requests (
  id, service_id, company_id, student_id, status, title, message, proposed_budget, currency_code, desired_deadline
)
values
(
  '99990000-aaaa-4111-8111-999999999001',
  '11111111-aaaa-4111-8111-111111111111',
  'd40df444-4444-4444-8444-444444444444',
  'a10ac111-1111-4111-8111-111111111111',
  'pending',
  'Landing page para programa de mentorias',
  'Nos interesa una landing clara para una convocatoria de mentorias con formulario y secciones informativas.',
  4200,
  'MXN',
  '2026-06-20'
),
(
  '99990000-bbbb-4222-8222-999999999002',
  '33333333-cccc-4333-8333-333333333333',
  'd40df444-4444-4444-8444-444444444444',
  'c30ce333-3333-4333-8333-333333333333',
  'pending',
  'Dashboard para seguimiento de ventas',
  'Buscamos un dashboard sencillo para revisar ventas semanales y categorias mas rentables.',
  3900,
  'MXN',
  '2026-06-18'
)
on conflict (id) do update
set title = excluded.title,
    message = excluded.message,
    proposed_budget = excluded.proposed_budget,
    desired_deadline = excluded.desired_deadline,
    updated_at = timezone('utc', now());

select public.refresh_profile_completion('a10ac111-1111-4111-8111-111111111111');
select public.refresh_profile_completion('b20bd222-2222-4222-8222-222222222222');
select public.refresh_profile_completion('c30ce333-3333-4333-8333-333333333333');
select public.refresh_profile_completion('d40df444-4444-4444-8444-444444444444');

commit;
