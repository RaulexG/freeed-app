begin;

create or replace function public.current_app_role()
returns public.app_role
language sql
stable
security definer
set search_path = public, pg_temp
as $$
  select p.role
  from public.profiles p
  where p.id = auth.uid()
$$;

create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public, pg_temp
as $$
  select coalesce(public.current_app_role() = 'admin', false)
$$;

create or replace function public.is_student()
returns boolean
language sql
stable
security definer
set search_path = public, pg_temp
as $$
  select coalesce(public.current_app_role() = 'student', false)
$$;

create or replace function public.is_company()
returns boolean
language sql
stable
security definer
set search_path = public, pg_temp
as $$
  select coalesce(public.current_app_role() = 'company', false)
$$;

create or replace function public.is_visible_profile(target_profile_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public, pg_temp
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
security definer
set search_path = public, pg_temp
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

commit;
