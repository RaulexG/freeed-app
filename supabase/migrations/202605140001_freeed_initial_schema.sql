begin;

create extension if not exists pgcrypto with schema extensions;

create type public.app_role as enum ('student', 'company', 'admin');
create type public.profile_status as enum ('onboarding', 'active', 'suspended', 'archived');
create type public.visibility_level as enum ('public', 'private');
create type public.service_status as enum ('draft', 'published', 'paused', 'archived');
create type public.service_modality as enum ('remote', 'hybrid', 'onsite');
create type public.price_type as enum ('fixed', 'hourly', 'custom', 'free');
create type public.request_status as enum ('pending', 'accepted', 'rejected', 'in_progress', 'completed', 'cancelled');
create type public.asset_kind as enum (
  'profile_avatar',
  'company_logo',
  'student_cv',
  'portfolio_image',
  'portfolio_document',
  'service_image',
  'request_attachment'
);
create type public.asset_access_scope as enum ('public_read', 'owner_only', 'request_participants');
create type public.portfolio_item_type as enum ('project', 'freelance_work', 'internship', 'volunteering', 'competition', 'certification', 'other');
create type public.skill_level as enum ('basic', 'intermediate', 'advanced', 'expert');

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  role public.app_role not null,
  profile_status public.profile_status not null default 'onboarding',
  profile_visibility public.visibility_level not null default 'public',
  username text,
  display_name text not null,
  headline text,
  bio text,
  phone text,
  country text default 'Mexico',
  state text,
  city text,
  website_url text,
  linkedin_url text,
  github_url text,
  portfolio_url text,
  avatar_asset_id uuid,
  is_profile_completed boolean not null default false,
  last_seen_at timestamptz,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint profiles_email_format_check check (position('@' in email) > 1),
  constraint profiles_username_format_check check (
    username is null
    or username ~ '^[a-z0-9](?:[a-z0-9._-]{1,28}[a-z0-9])?$'
  ),
  constraint profiles_headline_length_check check (headline is null or char_length(headline) <= 120)
);

create unique index profiles_email_lower_key on public.profiles (lower(email));
create unique index profiles_username_lower_key on public.profiles (lower(username)) where username is not null;
create index profiles_role_idx on public.profiles (role);
create index profiles_status_idx on public.profiles (profile_status);
create index profiles_search_idx on public.profiles
  using gin (to_tsvector('simple', coalesce(display_name, '') || ' ' || coalesce(headline, '') || ' ' || coalesce(bio, '')));

create table public.student_profiles (
  profile_id uuid primary key references public.profiles(id) on delete cascade,
  university_name text,
  degree_program text,
  semester smallint,
  graduation_year integer,
  availability_note text,
  preferred_work_modality public.service_modality,
  cv_asset_id uuid,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint student_profiles_semester_check check (semester is null or semester between 1 and 20),
  constraint student_profiles_graduation_year_check check (
    graduation_year is null or graduation_year between 2000 and 2100
  )
);

create table public.company_profiles (
  profile_id uuid primary key references public.profiles(id) on delete cascade,
  business_name text not null,
  legal_name text,
  industry text,
  company_size text,
  description text,
  contact_person_name text,
  phone_contact text,
  website_url text,
  logo_asset_id uuid,
  is_verified boolean not null default false,
  verification_notes text,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now())
);

create table public.skills (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  slug text not null,
  is_active boolean not null default true,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint skills_name_length_check check (char_length(name) between 2 and 80),
  constraint skills_slug_format_check check (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$')
);

create unique index skills_slug_key on public.skills (slug);
create unique index skills_name_lower_key on public.skills (lower(name));

create table public.profile_skills (
  profile_id uuid not null references public.profiles(id) on delete cascade,
  skill_id uuid not null references public.skills(id) on delete restrict,
  level public.skill_level not null default 'intermediate',
  years_experience numeric(4,1),
  created_at timestamptz not null default timezone('utc', now()),
  primary key (profile_id, skill_id),
  constraint profile_skills_years_check check (years_experience is null or years_experience between 0 and 60)
);

create table public.categories (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  slug text not null,
  description text,
  is_active boolean not null default true,
  sort_order integer not null default 100,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint categories_slug_format_check check (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$')
);

create unique index categories_slug_key on public.categories (slug);
create unique index categories_name_lower_key on public.categories (lower(name));

create table public.services (
  id uuid primary key default gen_random_uuid(),
  student_id uuid not null references public.profiles(id) on delete cascade,
  category_id uuid not null references public.categories(id) on delete restrict,
  title text not null,
  short_description text,
  description text not null,
  price_type public.price_type not null default 'fixed',
  price_amount numeric(12,2),
  currency_code text not null default 'MXN',
  estimated_delivery_days integer,
  modality public.service_modality not null default 'remote',
  location_text text,
  service_status public.service_status not null default 'draft',
  is_featured boolean not null default false,
  tags text[] not null default '{}'::text[],
  published_at timestamptz,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint services_title_length_check check (char_length(title) between 5 and 120),
  constraint services_short_description_length_check check (
    short_description is null or char_length(short_description) <= 180
  ),
  constraint services_price_amount_check check (
    price_amount is null or price_amount >= 0
  ),
  constraint services_currency_code_check check (currency_code ~ '^[A-Z]{3}$'),
  constraint services_delivery_days_check check (
    estimated_delivery_days is null or estimated_delivery_days between 1 and 365
  ),
  constraint services_price_type_logic_check check (
    (price_type = 'free' and coalesce(price_amount, 0) = 0)
    or (price_type = 'custom')
    or (price_type in ('fixed', 'hourly') and price_amount is not null and price_amount > 0)
  )
);

create index services_student_idx on public.services (student_id);
create index services_category_idx on public.services (category_id);
create index services_status_idx on public.services (service_status);
create index services_created_at_idx on public.services (created_at desc);
create index services_search_idx on public.services
  using gin (to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(short_description, '') || ' ' || coalesce(description, '')));
create index services_tags_gin_idx on public.services using gin (tags);

create table public.assets (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references auth.users(id) on delete cascade,
  bucket_id text not null,
  object_path text not null,
  file_name text not null,
  mime_type text,
  file_size_bytes bigint,
  asset_kind public.asset_kind not null,
  access_scope public.asset_access_scope not null,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint assets_bucket_check check (bucket_id in ('public-media', 'private-documents')),
  constraint assets_file_size_check check (file_size_bytes is null or file_size_bytes >= 0)
);

create unique index assets_bucket_path_key on public.assets (bucket_id, object_path);
create index assets_owner_idx on public.assets (owner_id);
create index assets_kind_idx on public.assets (asset_kind);

alter table public.profiles
  add constraint profiles_avatar_asset_id_fkey
  foreign key (avatar_asset_id) references public.assets(id) on delete set null;

alter table public.student_profiles
  add constraint student_profiles_cv_asset_id_fkey
  foreign key (cv_asset_id) references public.assets(id) on delete set null;

alter table public.company_profiles
  add constraint company_profiles_logo_asset_id_fkey
  foreign key (logo_asset_id) references public.assets(id) on delete set null;

create table public.service_assets (
  service_id uuid not null references public.services(id) on delete cascade,
  asset_id uuid not null references public.assets(id) on delete cascade,
  sort_order integer not null default 0,
  is_primary boolean not null default false,
  created_at timestamptz not null default timezone('utc', now()),
  primary key (service_id, asset_id)
);

create unique index service_assets_primary_key
  on public.service_assets (service_id)
  where is_primary = true;

create table public.portfolio_items (
  id uuid primary key default gen_random_uuid(),
  student_id uuid not null references public.profiles(id) on delete cascade,
  item_type public.portfolio_item_type not null default 'project',
  title text not null,
  description text,
  contribution text,
  project_url text,
  repository_url text,
  visibility public.visibility_level not null default 'public',
  started_on date,
  completed_on date,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint portfolio_items_title_length_check check (char_length(title) between 3 and 140),
  constraint portfolio_items_dates_check check (
    started_on is null or completed_on is null or started_on <= completed_on
  )
);

create index portfolio_items_student_idx on public.portfolio_items (student_id);
create index portfolio_items_visibility_idx on public.portfolio_items (visibility);

create table public.portfolio_assets (
  portfolio_item_id uuid not null references public.portfolio_items(id) on delete cascade,
  asset_id uuid not null references public.assets(id) on delete cascade,
  sort_order integer not null default 0,
  is_cover boolean not null default false,
  created_at timestamptz not null default timezone('utc', now()),
  primary key (portfolio_item_id, asset_id)
);

create unique index portfolio_assets_cover_key
  on public.portfolio_assets (portfolio_item_id)
  where is_cover = true;

create table public.favorite_services (
  company_id uuid not null references public.profiles(id) on delete cascade,
  service_id uuid not null references public.services(id) on delete cascade,
  created_at timestamptz not null default timezone('utc', now()),
  primary key (company_id, service_id)
);

create table public.service_requests (
  id uuid primary key default gen_random_uuid(),
  service_id uuid not null references public.services(id) on delete cascade,
  company_id uuid not null references public.profiles(id) on delete cascade,
  student_id uuid not null references public.profiles(id) on delete cascade,
  status public.request_status not null default 'pending',
  title text not null,
  message text not null,
  proposed_budget numeric(12,2),
  currency_code text not null default 'MXN',
  desired_deadline date,
  rejection_reason text,
  cancelled_reason text,
  completed_at timestamptz,
  accepted_at timestamptz,
  last_status_changed_at timestamptz not null default timezone('utc', now()),
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint service_requests_budget_check check (proposed_budget is null or proposed_budget >= 0),
  constraint service_requests_currency_code_check check (currency_code ~ '^[A-Z]{3}$'),
  constraint service_requests_title_length_check check (char_length(title) between 5 and 140)
);

create index service_requests_service_idx on public.service_requests (service_id);
create index service_requests_company_idx on public.service_requests (company_id);
create index service_requests_student_idx on public.service_requests (student_id);
create index service_requests_status_idx on public.service_requests (status);
create unique index service_requests_one_open_request_per_company_service
  on public.service_requests (service_id, company_id)
  where status in ('pending', 'accepted', 'in_progress');

create table public.request_assets (
  request_id uuid not null references public.service_requests(id) on delete cascade,
  asset_id uuid not null references public.assets(id) on delete cascade,
  created_at timestamptz not null default timezone('utc', now()),
  primary key (request_id, asset_id)
);

create table public.request_status_history (
  id uuid primary key default gen_random_uuid(),
  request_id uuid not null references public.service_requests(id) on delete cascade,
  changed_by uuid references auth.users(id) on delete set null,
  old_status public.request_status,
  new_status public.request_status not null,
  notes text,
  created_at timestamptz not null default timezone('utc', now())
);

create index request_status_history_request_idx on public.request_status_history (request_id, created_at desc);

create table public.reviews (
  id uuid primary key default gen_random_uuid(),
  request_id uuid not null unique references public.service_requests(id) on delete cascade,
  service_id uuid not null references public.services(id) on delete cascade,
  reviewer_company_id uuid not null references public.profiles(id) on delete cascade,
  reviewed_student_id uuid not null references public.profiles(id) on delete cascade,
  rating smallint not null,
  comment text,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  constraint reviews_rating_check check (rating between 1 and 5)
);

create index reviews_student_idx on public.reviews (reviewed_student_id);
create index reviews_service_idx on public.reviews (service_id);

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at := timezone('utc', now());
  return new;
end;
$$;

create or replace function public.current_app_role()
returns public.app_role
language sql
stable
as $$
  select p.role
  from public.profiles p
  where p.id = auth.uid()
$$;

create or replace function public.is_admin()
returns boolean
language sql
stable
as $$
  select coalesce(public.current_app_role() = 'admin', false)
$$;

create or replace function public.is_student()
returns boolean
language sql
stable
as $$
  select coalesce(public.current_app_role() = 'student', false)
$$;

create or replace function public.is_company()
returns boolean
language sql
stable
as $$
  select coalesce(public.current_app_role() = 'company', false)
$$;

create or replace function public.is_visible_profile(target_profile_id uuid)
returns boolean
language sql
stable
as $$
  select exists (
    select 1
    from public.profiles p
    where p.id = target_profile_id
      and p.profile_status = 'active'
      and p.profile_visibility = 'public'
  )
$$;

create or replace function public.is_visible_service(target_service_id uuid)
returns boolean
language sql
stable
as $$
  select exists (
    select 1
    from public.services s
    join public.profiles p on p.id = s.student_id
    where s.id = target_service_id
      and s.service_status = 'published'
      and p.profile_status = 'active'
      and p.profile_visibility = 'public'
  )
$$;

create or replace function public.can_access_request(target_request_id uuid)
returns boolean
language sql
stable
as $$
  select exists (
    select 1
    from public.service_requests r
    where r.id = target_request_id
      and (
        r.company_id = auth.uid()
        or r.student_id = auth.uid()
        or public.is_admin()
      )
  )
$$;

create or replace function public.can_read_asset(target_asset_id uuid)
returns boolean
language sql
stable
as $$
  with asset_row as (
    select a.*
    from public.assets a
    where a.id = target_asset_id
  )
  select exists (
    select 1
    from asset_row a
    where
      public.is_admin()
      or a.owner_id = auth.uid()
      or a.access_scope = 'public_read'
      or (
        a.access_scope = 'request_participants'
        and exists (
          select 1
          from public.request_assets ra
          join public.service_requests sr on sr.id = ra.request_id
          where ra.asset_id = a.id
            and (sr.company_id = auth.uid() or sr.student_id = auth.uid())
        )
      )
  )
$$;

create or replace function public.can_read_storage_object(target_bucket_id text, target_object_path text)
returns boolean
language sql
stable
as $$
  select
    case
      when target_bucket_id = 'public-media' then true
      else exists (
        select 1
        from public.assets a
        where a.bucket_id = target_bucket_id
          and a.object_path = target_object_path
          and public.can_read_asset(a.id)
      )
      or split_part(target_object_path, '/', 1) = auth.uid()::text
      or public.is_admin()
    end
$$;

create or replace function public.is_asset_path_owned_by_user(target_object_path text)
returns boolean
language sql
stable
as $$
  select split_part(target_object_path, '/', 1) = auth.uid()::text
$$;

create or replace function public.refresh_profile_completion(target_profile_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  v_role public.app_role;
  v_completed boolean := false;
begin
  select role into v_role
  from public.profiles
  where id = target_profile_id;

  if v_role = 'student' then
    select
      p.display_name is not null
      and p.bio is not null
      and sp.university_name is not null
      and sp.degree_program is not null
      and sp.semester is not null
    into v_completed
    from public.profiles p
    join public.student_profiles sp on sp.profile_id = p.id
    where p.id = target_profile_id;
  elsif v_role = 'company' then
    select
      p.display_name is not null
      and cp.business_name is not null
      and cp.contact_person_name is not null
      and coalesce(cp.description, p.bio) is not null
    into v_completed
    from public.profiles p
    join public.company_profiles cp on cp.profile_id = p.id
    where p.id = target_profile_id;
  else
    v_completed := true;
  end if;

  update public.profiles
  set is_profile_completed = coalesce(v_completed, false),
      profile_status = case
        when profile_status in ('suspended', 'archived') then profile_status
        when coalesce(v_completed, false) then 'active'::public.profile_status
        else 'onboarding'::public.profile_status
      end
  where id = target_profile_id;
end;
$$;

create or replace function public.handle_profile_completion_refresh()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  perform public.refresh_profile_completion(coalesce(new.id, new.profile_id, old.id, old.profile_id));
  return coalesce(new, old);
end;
$$;

create or replace function public.prevent_role_change()
returns trigger
language plpgsql
as $$
begin
  if old.role <> new.role and auth.uid() is not null then
    raise exception 'Role cannot be changed after account creation.';
  end if;
  return new;
end;
$$;

create or replace function public.validate_student_profile_role()
returns trigger
language plpgsql
as $$
begin
  if not exists (
    select 1 from public.profiles p
    where p.id = new.profile_id and p.role = 'student'
  ) then
    raise exception 'Only student users can have student_profiles.';
  end if;
  return new;
end;
$$;

create or replace function public.validate_profile_avatar()
returns trigger
language plpgsql
as $$
begin
  if new.avatar_asset_id is null then
    return new;
  end if;

  if not exists (
    select 1
    from public.assets a
    where a.id = new.avatar_asset_id
      and a.owner_id = new.id
      and a.asset_kind = 'profile_avatar'
      and a.access_scope = 'public_read'
  ) then
    raise exception 'Avatar asset must be a public profile avatar owned by the profile user.';
  end if;

  return new;
end;
$$;

create or replace function public.validate_student_cv_asset()
returns trigger
language plpgsql
as $$
begin
  if new.cv_asset_id is null then
    return new;
  end if;

  if not exists (
    select 1
    from public.assets a
    where a.id = new.cv_asset_id
      and a.owner_id = new.profile_id
      and a.asset_kind = 'student_cv'
      and a.access_scope = 'owner_only'
  ) then
    raise exception 'CV asset must be a private student CV owned by the student.';
  end if;

  return new;
end;
$$;

create or replace function public.validate_company_profile_role()
returns trigger
language plpgsql
as $$
begin
  if not exists (
    select 1 from public.profiles p
    where p.id = new.profile_id and p.role = 'company'
  ) then
    raise exception 'Only company users can have company_profiles.';
  end if;
  return new;
end;
$$;

create or replace function public.validate_company_logo_asset()
returns trigger
language plpgsql
as $$
begin
  if new.logo_asset_id is null then
    return new;
  end if;

  if not exists (
    select 1
    from public.assets a
    where a.id = new.logo_asset_id
      and a.owner_id = new.profile_id
      and a.asset_kind = 'company_logo'
      and a.access_scope = 'public_read'
  ) then
    raise exception 'Logo asset must be a public company logo owned by the company.';
  end if;

  return new;
end;
$$;

create or replace function public.validate_service_owner()
returns trigger
language plpgsql
as $$
begin
  if not exists (
    select 1 from public.profiles p
    where p.id = new.student_id and p.role = 'student'
  ) then
    raise exception 'Only student users can own services.';
  end if;

  if new.service_status = 'published' and new.published_at is null then
    new.published_at := timezone('utc', now());
  end if;

  if new.service_status <> 'published' then
    new.published_at := null;
  end if;

  return new;
end;
$$;

create or replace function public.validate_asset_owner_and_scope()
returns trigger
language plpgsql
as $$
begin
  if new.owner_id <> auth.uid() and not public.is_admin() and auth.uid() is not null then
    raise exception 'Assets can only be created for the authenticated user.';
  end if;

  if split_part(new.object_path, '/', 1) <> new.owner_id::text then
    raise exception 'Asset path must start with the owner user id.';
  end if;

  if new.bucket_id = 'public-media' and new.access_scope = 'request_participants' then
    raise exception 'Public media cannot be restricted to request participants.';
  end if;

  return new;
end;
$$;

create or replace function public.validate_service_asset()
returns trigger
language plpgsql
as $$
begin
  if not exists (
    select 1
    from public.assets a
    join public.services s on s.id = new.service_id
    where a.id = new.asset_id
      and a.owner_id = s.student_id
      and a.asset_kind = 'service_image'
      and a.access_scope = 'public_read'
  ) then
    raise exception 'Service assets must be public images owned by the service student.';
  end if;
  return new;
end;
$$;

create or replace function public.validate_portfolio_asset()
returns trigger
language plpgsql
as $$
begin
  if not exists (
    select 1
    from public.assets a
    join public.portfolio_items p on p.id = new.portfolio_item_id
    where a.id = new.asset_id
      and a.owner_id = p.student_id
      and a.asset_kind in ('portfolio_image', 'portfolio_document')
  ) then
    raise exception 'Portfolio assets must belong to the portfolio owner.';
  end if;
  return new;
end;
$$;

create or replace function public.validate_request_asset()
returns trigger
language plpgsql
as $$
begin
  if not exists (
    select 1
    from public.assets a
    join public.service_requests r on r.id = new.request_id
    where a.id = new.asset_id
      and a.asset_kind = 'request_attachment'
      and a.access_scope = 'request_participants'
      and a.owner_id in (r.company_id, r.student_id)
  ) then
    raise exception 'Request attachments must belong to a request participant.';
  end if;
  return new;
end;
$$;

create or replace function public.validate_service_request()
returns trigger
language plpgsql
as $$
declare
  v_service_owner uuid;
  v_service_status public.service_status;
  v_company_role public.app_role;
  v_student_role public.app_role;
begin
  select s.student_id, s.service_status into v_service_owner, v_service_status
  from public.services s
  where s.id = new.service_id;

  if v_service_owner is null then
    raise exception 'Service not found.';
  end if;

  select role into v_company_role from public.profiles where id = new.company_id;
  select role into v_student_role from public.profiles where id = new.student_id;

  if v_company_role <> 'company' then
    raise exception 'Only company users can create service requests.';
  end if;

  if v_student_role <> 'student' then
    raise exception 'The target request owner must be a student.';
  end if;

  if new.student_id <> v_service_owner then
    raise exception 'The request student must match the service owner.';
  end if;

  if new.company_id = new.student_id then
    raise exception 'A user cannot request their own service.';
  end if;

  if tg_op = 'INSERT' and v_service_status <> 'published' then
    raise exception 'Only published services can receive requests.';
  end if;

  if tg_op = 'INSERT' and new.status <> 'pending' then
    raise exception 'New requests must start in pending status.';
  end if;

  return new;
end;
$$;

create or replace function public.validate_service_request_status_transition()
returns trigger
language plpgsql
as $$
declare
  v_actor uuid := auth.uid();
begin
  if tg_op <> 'UPDATE' or old.status = new.status then
    if new.status = 'accepted' and new.accepted_at is null then
      new.accepted_at := timezone('utc', now());
    end if;
    if new.status = 'completed' and new.completed_at is null then
      new.completed_at := timezone('utc', now());
    end if;
    new.last_status_changed_at := timezone('utc', now());
    return new;
  end if;

  if v_actor is null then
    new.last_status_changed_at := timezone('utc', now());
    return new;
  end if;

  if v_actor not in (old.company_id, old.student_id) and not public.is_admin() then
    raise exception 'Only request participants can update request status.';
  end if;

  if old.status = 'pending' and new.status in ('accepted', 'rejected') and v_actor = old.student_id then
    null;
  elsif old.status = 'accepted' and new.status = 'in_progress' and v_actor in (old.company_id, old.student_id) then
    null;
  elsif old.status = 'in_progress' and new.status = 'completed' and v_actor in (old.company_id, old.student_id) then
    null;
  elsif old.status in ('pending', 'accepted', 'in_progress') and new.status = 'cancelled' and v_actor in (old.company_id, old.student_id) then
    null;
  else
    raise exception 'Invalid status transition for this user.';
  end if;

  if new.status = 'accepted' and new.accepted_at is null then
    new.accepted_at := timezone('utc', now());
  end if;

  if new.status = 'completed' and new.completed_at is null then
    new.completed_at := timezone('utc', now());
  end if;

  new.last_status_changed_at := timezone('utc', now());
  return new;
end;
$$;

create or replace function public.log_request_status_history()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if tg_op = 'INSERT' then
    insert into public.request_status_history (request_id, changed_by, old_status, new_status)
    values (new.id, auth.uid(), null, new.status);
    return new;
  end if;

  if old.status is distinct from new.status then
    insert into public.request_status_history (request_id, changed_by, old_status, new_status)
    values (new.id, auth.uid(), old.status, new.status);
  end if;

  return new;
end;
$$;

create or replace function public.validate_review()
returns trigger
language plpgsql
as $$
declare
  v_request public.service_requests%rowtype;
begin
  select * into v_request
  from public.service_requests
  where id = new.request_id;

  if v_request.id is null then
    raise exception 'Request not found.';
  end if;

  if v_request.status <> 'completed' then
    raise exception 'Only completed requests can be reviewed.';
  end if;

  if new.reviewer_company_id <> v_request.company_id
     or new.reviewed_student_id <> v_request.student_id
     or new.service_id <> v_request.service_id then
    raise exception 'Review data must match the completed request.';
  end if;

  return new;
end;
$$;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_role public.app_role;
  v_display_name text;
  v_semester smallint;
begin
  v_role := case lower(coalesce(new.raw_user_meta_data ->> 'role', 'student'))
    when 'company' then 'company'::public.app_role
    else 'student'::public.app_role
  end;

  v_display_name := coalesce(
    nullif(trim(new.raw_user_meta_data ->> 'display_name'), ''),
    split_part(coalesce(new.email, 'user'), '@', 1)
  );

  if coalesce(new.raw_user_meta_data ->> 'semester', '') ~ '^[0-9]+$' then
    v_semester := (new.raw_user_meta_data ->> 'semester')::smallint;
  else
    v_semester := null;
  end if;

  insert into public.profiles (id, email, role, display_name)
  values (new.id, lower(new.email), v_role, v_display_name)
  on conflict (id) do update
  set email = excluded.email;

  if v_role = 'student' then
    insert into public.student_profiles (
      profile_id,
      university_name,
      degree_program,
      semester
    )
    values (
      new.id,
      nullif(trim(new.raw_user_meta_data ->> 'university_name'), ''),
      nullif(trim(new.raw_user_meta_data ->> 'degree_program'), ''),
      v_semester
    )
    on conflict (profile_id) do nothing;
  elsif v_role = 'company' then
    insert into public.company_profiles (
      profile_id,
      business_name,
      industry,
      contact_person_name
    )
    values (
      new.id,
      coalesce(nullif(trim(new.raw_user_meta_data ->> 'business_name'), ''), v_display_name),
      nullif(trim(new.raw_user_meta_data ->> 'industry'), ''),
      coalesce(nullif(trim(new.raw_user_meta_data ->> 'contact_person_name'), ''), v_display_name)
    )
    on conflict (profile_id) do nothing;
  end if;

  perform public.refresh_profile_completion(new.id);
  return new;
end;
$$;

create or replace function public.sync_profile_from_auth()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.profiles
  set email = lower(new.email)
  where id = new.id;

  return new;
end;
$$;

create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

create trigger student_profiles_set_updated_at
before update on public.student_profiles
for each row execute function public.set_updated_at();

create trigger company_profiles_set_updated_at
before update on public.company_profiles
for each row execute function public.set_updated_at();

create trigger skills_set_updated_at
before update on public.skills
for each row execute function public.set_updated_at();

create trigger categories_set_updated_at
before update on public.categories
for each row execute function public.set_updated_at();

create trigger services_set_updated_at
before update on public.services
for each row execute function public.set_updated_at();

create trigger assets_set_updated_at
before update on public.assets
for each row execute function public.set_updated_at();

create trigger portfolio_items_set_updated_at
before update on public.portfolio_items
for each row execute function public.set_updated_at();

create trigger service_requests_set_updated_at
before update on public.service_requests
for each row execute function public.set_updated_at();

create trigger reviews_set_updated_at
before update on public.reviews
for each row execute function public.set_updated_at();

create trigger profiles_prevent_role_change
before update on public.profiles
for each row execute function public.prevent_role_change();

create trigger student_profiles_validate_role
before insert or update on public.student_profiles
for each row execute function public.validate_student_profile_role();

create trigger profiles_validate_avatar
before insert or update of avatar_asset_id on public.profiles
for each row execute function public.validate_profile_avatar();

create trigger student_profiles_validate_cv_asset
before insert or update of cv_asset_id on public.student_profiles
for each row execute function public.validate_student_cv_asset();

create trigger company_profiles_validate_role
before insert or update on public.company_profiles
for each row execute function public.validate_company_profile_role();

create trigger company_profiles_validate_logo_asset
before insert or update of logo_asset_id on public.company_profiles
for each row execute function public.validate_company_logo_asset();

create trigger services_validate_owner
before insert or update on public.services
for each row execute function public.validate_service_owner();

create trigger assets_validate_owner_scope
before insert or update on public.assets
for each row execute function public.validate_asset_owner_and_scope();

create trigger service_assets_validate
before insert or update on public.service_assets
for each row execute function public.validate_service_asset();

create trigger portfolio_assets_validate
before insert or update on public.portfolio_assets
for each row execute function public.validate_portfolio_asset();

create trigger request_assets_validate
before insert or update on public.request_assets
for each row execute function public.validate_request_asset();

create trigger service_requests_validate
before insert or update on public.service_requests
for each row execute function public.validate_service_request();

create trigger service_requests_validate_status_transition
before update on public.service_requests
for each row execute function public.validate_service_request_status_transition();

create trigger service_requests_log_status_history
after insert or update on public.service_requests
for each row execute function public.log_request_status_history();

create trigger reviews_validate
before insert or update on public.reviews
for each row execute function public.validate_review();

create trigger profiles_refresh_completion
after insert or update of display_name, bio on public.profiles
for each row execute function public.handle_profile_completion_refresh();

create trigger student_profiles_refresh_completion
after insert or update of university_name, degree_program, semester on public.student_profiles
for each row execute function public.handle_profile_completion_refresh();

create trigger company_profiles_refresh_completion
after insert or update of business_name, description, contact_person_name on public.company_profiles
for each row execute function public.handle_profile_completion_refresh();

create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

create trigger on_auth_user_updated
after update of email on auth.users
for each row execute function public.sync_profile_from_auth();

alter table public.profiles enable row level security;
alter table public.student_profiles enable row level security;
alter table public.company_profiles enable row level security;
alter table public.skills enable row level security;
alter table public.profile_skills enable row level security;
alter table public.categories enable row level security;
alter table public.services enable row level security;
alter table public.assets enable row level security;
alter table public.service_assets enable row level security;
alter table public.portfolio_items enable row level security;
alter table public.portfolio_assets enable row level security;
alter table public.favorite_services enable row level security;
alter table public.service_requests enable row level security;
alter table public.request_assets enable row level security;
alter table public.request_status_history enable row level security;
alter table public.reviews enable row level security;

create policy "profiles_select_visible_or_own"
on public.profiles
for select
to authenticated
using (
  id = auth.uid()
  or public.is_admin()
  or public.is_visible_profile(id)
);

create policy "profiles_update_own"
on public.profiles
for update
to authenticated
using (id = auth.uid() or public.is_admin())
with check (id = auth.uid() or public.is_admin());

create policy "student_profiles_select_visible_or_own"
on public.student_profiles
for select
to authenticated
using (
  profile_id = auth.uid()
  or public.is_admin()
  or public.is_visible_profile(profile_id)
);

create policy "student_profiles_upsert_own"
on public.student_profiles
for all
to authenticated
using (profile_id = auth.uid() or public.is_admin())
with check (profile_id = auth.uid() or public.is_admin());

create policy "company_profiles_select_visible_or_own"
on public.company_profiles
for select
to authenticated
using (
  profile_id = auth.uid()
  or public.is_admin()
  or public.is_visible_profile(profile_id)
);

create policy "company_profiles_upsert_own"
on public.company_profiles
for all
to authenticated
using (profile_id = auth.uid() or public.is_admin())
with check (profile_id = auth.uid() or public.is_admin());

create policy "skills_read_authenticated"
on public.skills
for select
to authenticated
using (is_active = true or public.is_admin());

create policy "skills_manage_admin"
on public.skills
for all
to authenticated
using (public.is_admin())
with check (public.is_admin());

create policy "profile_skills_select_visible_or_own"
on public.profile_skills
for select
to authenticated
using (
  profile_id = auth.uid()
  or public.is_admin()
  or public.is_visible_profile(profile_id)
);

create policy "profile_skills_manage_own"
on public.profile_skills
for all
to authenticated
using (profile_id = auth.uid() or public.is_admin())
with check (profile_id = auth.uid() or public.is_admin());

create policy "categories_read_authenticated"
on public.categories
for select
to authenticated
using (is_active = true or public.is_admin());

create policy "categories_manage_admin"
on public.categories
for all
to authenticated
using (public.is_admin())
with check (public.is_admin());

create policy "services_select_visible_or_owner"
on public.services
for select
to authenticated
using (
  student_id = auth.uid()
  or public.is_admin()
  or public.is_visible_service(id)
);

create policy "services_insert_owner"
on public.services
for insert
to authenticated
with check (
  student_id = auth.uid()
  and public.is_student()
);

create policy "services_update_owner"
on public.services
for update
to authenticated
using (student_id = auth.uid() or public.is_admin())
with check (student_id = auth.uid() or public.is_admin());

create policy "services_delete_owner"
on public.services
for delete
to authenticated
using (student_id = auth.uid() or public.is_admin());

create policy "assets_select_visible_to_owner_or_linked_users"
on public.assets
for select
to authenticated
using (
  owner_id = auth.uid()
  or public.is_admin()
  or public.can_read_asset(id)
);

create policy "assets_insert_owner"
on public.assets
for insert
to authenticated
with check (
  owner_id = auth.uid()
  or public.is_admin()
);

create policy "assets_update_owner"
on public.assets
for update
to authenticated
using (owner_id = auth.uid() or public.is_admin())
with check (owner_id = auth.uid() or public.is_admin());

create policy "assets_delete_owner"
on public.assets
for delete
to authenticated
using (owner_id = auth.uid() or public.is_admin());

create policy "service_assets_select"
on public.service_assets
for select
to authenticated
using (
  exists (
    select 1
    from public.services s
    where s.id = service_id
      and (
        s.student_id = auth.uid()
        or public.is_admin()
        or public.is_visible_service(s.id)
      )
  )
);

create policy "service_assets_manage_owner"
on public.service_assets
for all
to authenticated
using (
  exists (
    select 1
    from public.services s
    where s.id = service_id
      and (s.student_id = auth.uid() or public.is_admin())
  )
)
with check (
  exists (
    select 1
    from public.services s
    where s.id = service_id
      and (s.student_id = auth.uid() or public.is_admin())
  )
);

create policy "portfolio_items_select_visible_or_owner"
on public.portfolio_items
for select
to authenticated
using (
  student_id = auth.uid()
  or public.is_admin()
  or (
    visibility = 'public'
    and public.is_visible_profile(student_id)
  )
);

create policy "portfolio_items_manage_owner"
on public.portfolio_items
for all
to authenticated
using (student_id = auth.uid() or public.is_admin())
with check (student_id = auth.uid() or public.is_admin());

create policy "portfolio_assets_select"
on public.portfolio_assets
for select
to authenticated
using (
  exists (
    select 1
    from public.portfolio_items p
    where p.id = portfolio_item_id
      and (
        p.student_id = auth.uid()
        or public.is_admin()
        or (p.visibility = 'public' and public.is_visible_profile(p.student_id))
      )
  )
);

create policy "portfolio_assets_manage_owner"
on public.portfolio_assets
for all
to authenticated
using (
  exists (
    select 1
    from public.portfolio_items p
    where p.id = portfolio_item_id
      and (p.student_id = auth.uid() or public.is_admin())
  )
)
with check (
  exists (
    select 1
    from public.portfolio_items p
    where p.id = portfolio_item_id
      and (p.student_id = auth.uid() or public.is_admin())
  )
);

create policy "favorite_services_select_own"
on public.favorite_services
for select
to authenticated
using (company_id = auth.uid() or public.is_admin());

create policy "favorite_services_manage_own"
on public.favorite_services
for all
to authenticated
using (company_id = auth.uid() or public.is_admin())
with check (
  (company_id = auth.uid() and public.is_company())
  or public.is_admin()
);

create policy "service_requests_select_participants"
on public.service_requests
for select
to authenticated
using (
  company_id = auth.uid()
  or student_id = auth.uid()
  or public.is_admin()
);

create policy "service_requests_insert_company"
on public.service_requests
for insert
to authenticated
with check (
  company_id = auth.uid()
  and public.is_company()
);

create policy "service_requests_update_participants"
on public.service_requests
for update
to authenticated
using (
  company_id = auth.uid()
  or student_id = auth.uid()
  or public.is_admin()
)
with check (
  company_id = auth.uid()
  or student_id = auth.uid()
  or public.is_admin()
);

create policy "request_assets_select_participants"
on public.request_assets
for select
to authenticated
using (public.can_access_request(request_id));

create policy "request_assets_manage_participants"
on public.request_assets
for all
to authenticated
using (public.can_access_request(request_id))
with check (public.can_access_request(request_id));

create policy "request_status_history_select_participants"
on public.request_status_history
for select
to authenticated
using (public.can_access_request(request_id));

create policy "reviews_select_authenticated"
on public.reviews
for select
to authenticated
using (true);

create policy "reviews_insert_company_owner"
on public.reviews
for insert
to authenticated
with check (
  reviewer_company_id = auth.uid()
  and public.is_company()
);

create policy "reviews_update_reviewer"
on public.reviews
for update
to authenticated
using (reviewer_company_id = auth.uid() or public.is_admin())
with check (reviewer_company_id = auth.uid() or public.is_admin());

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
  (
    'public-media',
    'public-media',
    true,
    10485760,
    array[
      'image/jpeg',
      'image/png',
      'image/webp',
      'image/heic'
    ]
  ),
  (
    'private-documents',
    'private-documents',
    false,
    15728640,
    array[
      'image/jpeg',
      'image/png',
      'image/webp',
      'application/pdf'
    ]
  )
on conflict (id) do nothing;

create policy "public_media_read_authenticated"
on storage.objects
for select
to authenticated
using (bucket_id = 'public-media');

create policy "public_media_upload_own_folder"
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'public-media'
  and public.is_asset_path_owned_by_user(name)
);

create policy "public_media_update_own_folder"
on storage.objects
for update
to authenticated
using (
  bucket_id = 'public-media'
  and public.is_asset_path_owned_by_user(name)
)
with check (
  bucket_id = 'public-media'
  and public.is_asset_path_owned_by_user(name)
);

create policy "public_media_delete_own_folder"
on storage.objects
for delete
to authenticated
using (
  bucket_id = 'public-media'
  and public.is_asset_path_owned_by_user(name)
);

create policy "private_documents_read_authorized"
on storage.objects
for select
to authenticated
using (
  bucket_id = 'private-documents'
  and public.can_read_storage_object(bucket_id, name)
);

create policy "private_documents_upload_own_folder"
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'private-documents'
  and public.is_asset_path_owned_by_user(name)
);

create policy "private_documents_update_own_folder"
on storage.objects
for update
to authenticated
using (
  bucket_id = 'private-documents'
  and public.is_asset_path_owned_by_user(name)
)
with check (
  bucket_id = 'private-documents'
  and public.is_asset_path_owned_by_user(name)
);

create policy "private_documents_delete_own_folder"
on storage.objects
for delete
to authenticated
using (
  bucket_id = 'private-documents'
  and public.is_asset_path_owned_by_user(name)
);

insert into public.categories (name, slug, description, sort_order)
values
  ('Programacion', 'programacion', 'Desarrollo de software, web, movil y automatizacion.', 10),
  ('Diseno Grafico', 'diseno-grafico', 'Branding, piezas visuales, identidad y contenido grafico.', 20),
  ('UI UX', 'ui-ux', 'Diseno de interfaces, experiencia de usuario y prototipado.', 30),
  ('Marketing Digital', 'marketing-digital', 'Redes sociales, campañas, pauta y contenido.', 40),
  ('Creacion de Contenido', 'creacion-de-contenido', 'Video, fotografia, copy y contenido creativo.', 50),
  ('Asesorias Academicas', 'asesorias-academicas', 'Tutorias, regularizacion y apoyo en materias.', 60),
  ('Soporte Tecnico', 'soporte-tecnico', 'Instalacion, mantenimiento y soporte a equipos y sistemas.', 70),
  ('Analisis de Datos', 'analisis-de-datos', 'Dashboards, reportes, limpieza y analitica.', 80),
  ('Administracion', 'administracion', 'Procesos, organizacion, captura y apoyo administrativo.', 90),
  ('Legal Basico', 'legal-basico', 'Apoyo operativo y documental para despachos y tramites.', 100)
on conflict (slug) do nothing;

insert into public.skills (name, slug)
values
  ('Kotlin', 'kotlin'),
  ('Java', 'java'),
  ('Python', 'python'),
  ('Excel', 'excel'),
  ('SQL', 'sql'),
  ('Figma', 'figma'),
  ('Adobe Photoshop', 'adobe-photoshop'),
  ('Canva', 'canva'),
  ('Marketing en Redes Sociales', 'marketing-en-redes-sociales'),
  ('Diseno UX', 'diseno-ux'),
  ('Soporte TI', 'soporte-ti'),
  ('Redaccion', 'redaccion')
on conflict (slug) do nothing;

commit;
