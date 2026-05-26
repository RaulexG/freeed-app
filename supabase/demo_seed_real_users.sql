begin;

-- Demo users already created in Supabase Auth
-- Students:
--   79d8738b-912f-4314-90b3-93786e63e884 -> anatorresdemo@gmail.com
--   dc5a215f-1eb1-4c3c-b428-6108ac27de44 -> diegoortizdemo@gmail.com
--   972c5a16-a92e-4dc2-b5ba-63fdc9d7e753 -> carlosmendezdemo@gmail.com
-- Company:
--   9a828ad2-65b9-4ea5-ba4e-dc22e3655796 -> impulsalabdemo@gmail.com

-- Cleanup of demo relational data for idempotency
delete from public.favorite_services
where company_id = '9a828ad2-65b9-4ea5-ba4e-dc22e3655796';

delete from public.service_requests
where company_id = '9a828ad2-65b9-4ea5-ba4e-dc22e3655796'
   or student_id in (
        '79d8738b-912f-4314-90b3-93786e63e884',
        'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
        '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753'
   );

delete from public.service_assets
where service_id in (
    'a1111111-aaaa-4444-8888-aaaaaaaaaaa1',
    'b2222222-bbbb-4444-8888-bbbbbbbbbbb2',
    'c3333333-cccc-4444-8888-ccccccccccc3'
);

delete from public.portfolio_assets
where portfolio_item_id in (
    'aa111111-aaaa-4444-8888-aaaaaaaaaaa1',
    'bb222222-bbbb-4444-8888-bbbbbbbbbbb2',
    'cc333333-cccc-4444-8888-ccccccccccc3'
);

delete from public.services
where id in (
    'a1111111-aaaa-4444-8888-aaaaaaaaaaa1',
    'b2222222-bbbb-4444-8888-bbbbbbbbbbb2',
    'c3333333-cccc-4444-8888-ccccccccccc3'
)
or student_id in (
    '79d8738b-912f-4314-90b3-93786e63e884',
    'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
    '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753'
);

delete from public.portfolio_items
where id in (
    'aa111111-aaaa-4444-8888-aaaaaaaaaaa1',
    'bb222222-bbbb-4444-8888-bbbbbbbbbbb2',
    'cc333333-cccc-4444-8888-ccccccccccc3'
)
or student_id in (
    '79d8738b-912f-4314-90b3-93786e63e884',
    'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
    '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753'
);

delete from public.profile_skills
where profile_id in (
    '79d8738b-912f-4314-90b3-93786e63e884',
    'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
    '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753'
);

delete from public.company_profiles
where profile_id = '9a828ad2-65b9-4ea5-ba4e-dc22e3655796';

delete from public.student_profiles
where profile_id in (
    '79d8738b-912f-4314-90b3-93786e63e884',
    'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
    '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753',
    '9a828ad2-65b9-4ea5-ba4e-dc22e3655796'
);

-- Core profiles
update public.profiles
set
    role = 'student',
    profile_status = 'active',
    profile_visibility = 'public',
    username = 'ana.torres',
    display_name = 'Ana Torres',
    headline = 'Ing. en Sistemas Computacionales | Apps, formularios y soporte digital',
    bio = 'Estudiante del TecNM Campus Tuxtla Gutierrez enfocada en desarrollo de soluciones digitales para pequenos negocios y proyectos universitarios.',
    state = 'Chiapas',
    city = 'Tuxtla Gutierrez',
    linkedin_url = 'https://linkedin.com/in/anatorresfreeed',
    github_url = 'https://github.com/anatorresfreeed',
    portfolio_url = 'https://freeed.app/demo/ana-torres',
    is_profile_completed = true,
    updated_at = timezone('utc', now())
where id = '79d8738b-912f-4314-90b3-93786e63e884';

update public.profiles
set
    role = 'student',
    profile_status = 'active',
    profile_visibility = 'public',
    username = 'diego.ortiz',
    display_name = 'Diego Ortiz',
    headline = 'Ing. en Gestion Empresarial | Marketing digital y contenido para negocios',
    bio = 'Estudiante del TecNM Campus Tuxtla Gutierrez con enfoque en estrategias digitales, organizacion de campanas y comunicacion para marcas emergentes.',
    state = 'Chiapas',
    city = 'Tuxtla Gutierrez',
    linkedin_url = 'https://linkedin.com/in/diegoortizfreeed',
    portfolio_url = 'https://freeed.app/demo/diego-ortiz',
    is_profile_completed = true,
    updated_at = timezone('utc', now())
where id = 'dc5a215f-1eb1-4c3c-b428-6108ac27de44';

update public.profiles
set
    role = 'student',
    profile_status = 'active',
    profile_visibility = 'public',
    username = 'carlos.mendez',
    display_name = 'Carlos Mendez',
    headline = 'Ing. Industrial | Dashboards, inventarios y analisis operativo',
    bio = 'Estudiante del TecNM Campus Tuxtla Gutierrez enfocado en control de procesos, reportes operativos y soluciones con Excel y analisis de datos.',
    state = 'Chiapas',
    city = 'Tuxtla Gutierrez',
    linkedin_url = 'https://linkedin.com/in/carlosmendezfreeed',
    portfolio_url = 'https://freeed.app/demo/carlos-mendez',
    is_profile_completed = true,
    updated_at = timezone('utc', now())
where id = '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753';

update public.profiles
set
    role = 'company',
    profile_status = 'active',
    profile_visibility = 'public',
    username = 'impulsa.lab',
    display_name = 'Impulsa Lab',
    headline = 'Estudio pequeno de soluciones digitales para negocios locales',
    bio = 'Negocio enfocado en apoyar a emprendimientos y pymes con presencia digital, materiales comerciales y organizacion operativa.',
    phone = '9611234567',
    state = 'Chiapas',
    city = 'Tuxtla Gutierrez',
    website_url = 'https://impulsalab-demo.mx',
    is_profile_completed = true,
    updated_at = timezone('utc', now())
where id = '9a828ad2-65b9-4ea5-ba4e-dc22e3655796';

-- Student profile details
insert into public.student_profiles (
    profile_id,
    university_name,
    degree_program,
    semester,
    graduation_year,
    availability_note,
    preferred_work_modality
) values
(
    '79d8738b-912f-4314-90b3-93786e63e884',
    'TecNM Campus Tuxtla Gutierrez',
    'Ingenieria en Sistemas Computacionales',
    7,
    2027,
    'Disponible para proyectos web pequenos, formularios y soporte remoto por las tardes.',
    'remote'
),
(
    'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
    'TecNM Campus Tuxtla Gutierrez',
    'Ingenieria en Gestion Empresarial',
    6,
    2027,
    'Disponible para estrategias de contenido, publicaciones y materiales de presentacion.',
    'hybrid'
),
(
    '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753',
    'TecNM Campus Tuxtla Gutierrez',
    'Ingenieria Industrial',
    8,
    2026,
    'Disponible para reportes operativos, tableros en Excel y documentacion de procesos.',
    'hybrid'
);

insert into public.company_profiles (
    profile_id,
    business_name,
    legal_name,
    industry,
    company_size,
    description,
    contact_person_name,
    phone_contact,
    website_url,
    is_verified,
    verification_notes
) values (
    '9a828ad2-65b9-4ea5-ba4e-dc22e3655796',
    'Impulsa Lab',
    'Impulsa Lab Studio',
    'Servicios digitales para pymes',
    '1-10',
    'Empresa pequena que apoya a negocios locales con materiales digitales, organizacion de informacion y presencia comercial.',
    'Laura Hernandez',
    '9611234567',
    'https://impulsalab-demo.mx',
    false,
    null
);

-- Student skills
insert into public.profile_skills (profile_id, skill_id, level, years_experience) values
('79d8738b-912f-4314-90b3-93786e63e884', 'ba5ab09d-89eb-4bd6-bdd1-3b632b772f30', 'advanced', 2),
('79d8738b-912f-4314-90b3-93786e63e884', 'f1492846-057b-4f2a-bfa0-83450b2e8568', 'intermediate', 1),
('79d8738b-912f-4314-90b3-93786e63e884', 'a79b38fd-e41c-4681-94d9-8132d865a95f', 'intermediate', 1),
('79d8738b-912f-4314-90b3-93786e63e884', 'b8295af9-088e-4799-b042-5ed3e0afbd07', 'advanced', 2),
('dc5a215f-1eb1-4c3c-b428-6108ac27de44', '61cc83f0-0d18-46f3-ae09-e20bba8aed38', 'advanced', 2),
('dc5a215f-1eb1-4c3c-b428-6108ac27de44', 'e19fb6d5-8a96-49ec-b1d4-e8aa69e1cc50', 'advanced', 2),
('dc5a215f-1eb1-4c3c-b428-6108ac27de44', 'ec62f228-3cfb-473d-b616-42bd0bad50ba', 'intermediate', 2),
('dc5a215f-1eb1-4c3c-b428-6108ac27de44', '145f8831-1667-4780-97ff-b277a32c8151', 'intermediate', 1),
('972c5a16-a92e-4dc2-b5ba-63fdc9d7e753', '437ec543-fb79-40be-99ee-2cad78b56129', 'advanced', 3),
('972c5a16-a92e-4dc2-b5ba-63fdc9d7e753', 'a79b38fd-e41c-4681-94d9-8132d865a95f', 'intermediate', 1),
('972c5a16-a92e-4dc2-b5ba-63fdc9d7e753', '20c6fa05-b513-4a96-8040-80f9854bdbc2', 'intermediate', 1);

-- Services
insert into public.services (
    id,
    student_id,
    category_id,
    title,
    short_description,
    description,
    price_type,
    price_amount,
    currency_code,
    estimated_delivery_days,
    modality,
    location_text,
    service_status,
    is_featured,
    tags,
    published_at
) values
(
    'a1111111-aaaa-4444-8888-aaaaaaaaaaa1',
    '79d8738b-912f-4314-90b3-93786e63e884',
    '575564af-3b68-42eb-8102-afb066ec2121',
    'Formulario web y base de datos para pequenos negocios',
    'Creo formularios de registro y seguimiento conectados a base de datos.',
    'Desarrollo formularios para clientes, pedidos o registros internos con estructura clara y enfoque practico para negocios pequenos o proyectos escolares.',
    'fixed',
    1800,
    'MXN',
    7,
    'remote',
    'Tuxtla Gutierrez, Chiapas',
    'published',
    true,
    array['formularios','base de datos','soporte digital'],
    timezone('utc', now())
),
(
    'b2222222-bbbb-4444-8888-bbbbbbbbbbb2',
    'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
    '43a51b7e-d154-43d8-98b3-0ecac494340b',
    'Paquete de contenido y calendario para redes sociales',
    'Organizo publicaciones, copies y propuesta visual basica para tu negocio.',
    'Te apoyo con un plan sencillo de contenido para Instagram y Facebook, incluyendo ideas de publicaciones, copies y una linea visual consistente para tu marca.',
    'fixed',
    1500,
    'MXN',
    5,
    'hybrid',
    'Tuxtla Gutierrez, Chiapas',
    'published',
    false,
    array['marketing digital','contenido','redes sociales'],
    timezone('utc', now())
),
(
    'c3333333-cccc-4444-8888-ccccccccccc3',
    '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753',
    'f7099a93-2090-4990-9df5-ef33bc9dcbf5',
    'Dashboard de inventario y control en Excel',
    'Diseno tableros para seguimiento de inventario, compras y reportes.',
    'Creo una plantilla clara con indicadores, entradas de inventario y resumen ejecutivo para negocios que quieren ordenar su informacion operativa.',
    'fixed',
    2200,
    'MXN',
    6,
    'hybrid',
    'Tuxtla Gutierrez, Chiapas',
    'published',
    true,
    array['excel','inventario','reportes'],
    timezone('utc', now())
);

-- Portfolio
insert into public.portfolio_items (
    id,
    student_id,
    item_type,
    title,
    description,
    contribution,
    project_url,
    repository_url,
    visibility,
    started_on,
    completed_on
) values
(
    'aa111111-aaaa-4444-8888-aaaaaaaaaaa1',
    '79d8738b-912f-4314-90b3-93786e63e884',
    'project',
    'Sistema de registros para brigada estudiantil',
    'Proyecto academico para organizar asistencia y actividades de una brigada universitaria.',
    'Modele el flujo, construi formularios y prepare la estructura de almacenamiento.',
    'https://freeed.app/demo/ana-proyecto',
    'https://github.com/anatorresfreeed/registro-brigada',
    'public',
    date '2026-01-20',
    date '2026-03-15'
),
(
    'bb222222-bbbb-4444-8888-bbbbbbbbbbb2',
    'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
    'project',
    'Campana digital para cafeteria local',
    'Propuesta de contenido y promocion para una cafeteria cercana al campus.',
    'Defini calendario, copies, tono de comunicacion y materiales de publicacion.',
    'https://freeed.app/demo/diego-proyecto',
    null,
    'public',
    date '2026-02-05',
    date '2026-03-01'
),
(
    'cc333333-cccc-4444-8888-ccccccccccc3',
    '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753',
    'project',
    'Control de inventario para negocio familiar',
    'Tablero de seguimiento para entradas, salidas y alertas de producto.',
    'Estructure el archivo, cree formulas clave y organice el reporte semanal.',
    'https://freeed.app/demo/carlos-proyecto',
    null,
    'public',
    date '2026-01-10',
    date '2026-02-18'
);

-- Company favorites
insert into public.favorite_services (company_id, service_id) values
('9a828ad2-65b9-4ea5-ba4e-dc22e3655796', 'a1111111-aaaa-4444-8888-aaaaaaaaaaa1'),
('9a828ad2-65b9-4ea5-ba4e-dc22e3655796', 'c3333333-cccc-4444-8888-ccccccccccc3');

-- Service requests from the demo company
insert into public.service_requests (
    id,
    service_id,
    company_id,
    student_id,
    status,
    title,
    message,
    proposed_budget,
    currency_code,
    desired_deadline,
    last_status_changed_at
) values
(
    'd1111111-aaaa-4444-8888-aaaaaaaaaaa1',
    'a1111111-aaaa-4444-8888-aaaaaaaaaaa1',
    '9a828ad2-65b9-4ea5-ba4e-dc22e3655796',
    '79d8738b-912f-4314-90b3-93786e63e884',
    'pending',
    'Formulario para captar prospectos',
    'Necesitamos un formulario sencillo para registrar interesados y dar seguimiento desde una base de datos basica.',
    2000,
    'MXN',
    date '2026-06-15',
    timezone('utc', now())
),
(
    'e2222222-bbbb-4444-8888-bbbbbbbbbbb2',
    'b2222222-bbbb-4444-8888-bbbbbbbbbbb2',
    '9a828ad2-65b9-4ea5-ba4e-dc22e3655796',
    'dc5a215f-1eb1-4c3c-b428-6108ac27de44',
    'pending',
    'Contenido para promocionar taller de verano',
    'Buscamos apoyo para organizar publicaciones y copies de una campana corta en redes sociales.',
    1600,
    'MXN',
    date '2026-06-10',
    timezone('utc', now())
),
(
    'f3333333-cccc-4444-8888-ccccccccccc3',
    'c3333333-cccc-4444-8888-ccccccccccc3',
    '9a828ad2-65b9-4ea5-ba4e-dc22e3655796',
    '972c5a16-a92e-4dc2-b5ba-63fdc9d7e753',
    'pending',
    'Dashboard para control de materiales',
    'Queremos ordenar compras y existencias en un solo archivo con indicadores faciles de leer.',
    2400,
    'MXN',
    date '2026-06-18',
    timezone('utc', now())
);

select public.refresh_profile_completion('79d8738b-912f-4314-90b3-93786e63e884');
select public.refresh_profile_completion('dc5a215f-1eb1-4c3c-b428-6108ac27de44');
select public.refresh_profile_completion('972c5a16-a92e-4dc2-b5ba-63fdc9d7e753');
select public.refresh_profile_completion('9a828ad2-65b9-4ea5-ba4e-dc22e3655796');

commit;
