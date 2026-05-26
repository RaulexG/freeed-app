begin;

-- FreeEd v1 simplification:
-- - Keep the current relational model.
-- - Treat reviews and verification as future/optional features.
-- - Open the public browsing surface for guests (anon) so the app can
--   behave like a normal product: explore first, authenticate later.

-- Public profiles
drop policy if exists "profiles_select_visible_or_own" on public.profiles;
create policy "profiles_select_visible_or_own"
on public.profiles
for select
to anon, authenticated
using (
  ((select auth.uid()) is not null and id = (select auth.uid()))
  or (select public.is_admin())
  or public.is_visible_profile(id)
);

drop policy if exists "student_profiles_select_visible_or_own" on public.student_profiles;
create policy "student_profiles_select_visible_or_own"
on public.student_profiles
for select
to anon, authenticated
using (
  ((select auth.uid()) is not null and profile_id = (select auth.uid()))
  or (select public.is_admin())
  or public.is_visible_profile(profile_id)
);

drop policy if exists "company_profiles_select_visible_or_own" on public.company_profiles;
create policy "company_profiles_select_visible_or_own"
on public.company_profiles
for select
to anon, authenticated
using (
  ((select auth.uid()) is not null and profile_id = (select auth.uid()))
  or (select public.is_admin())
  or public.is_visible_profile(profile_id)
);

-- Public catalogs and skills for browsing
drop policy if exists "categories_read_authenticated" on public.categories;
drop policy if exists "categories_read_public" on public.categories;
create policy "categories_read_public"
on public.categories
for select
to anon, authenticated
using (is_active = true or (select public.is_admin()));

drop policy if exists "skills_read_authenticated" on public.skills;
drop policy if exists "skills_read_public" on public.skills;
create policy "skills_read_public"
on public.skills
for select
to anon, authenticated
using (is_active = true or (select public.is_admin()));

drop policy if exists "profile_skills_select_visible_or_own" on public.profile_skills;
create policy "profile_skills_select_visible_or_own"
on public.profile_skills
for select
to anon, authenticated
using (
  ((select auth.uid()) is not null and profile_id = (select auth.uid()))
  or (select public.is_admin())
  or public.is_visible_profile(profile_id)
);

-- Public services and media
drop policy if exists "services_select_visible_or_owner" on public.services;
create policy "services_select_visible_or_owner"
on public.services
for select
to anon, authenticated
using (
  ((select auth.uid()) is not null and student_id = (select auth.uid()))
  or (select public.is_admin())
  or public.is_visible_service(id)
);

drop policy if exists "service_assets_select" on public.service_assets;
create policy "service_assets_select"
on public.service_assets
for select
to anon, authenticated
using (
  exists (
    select 1
    from public.services s
    where s.id = service_id
      and (
        (((select auth.uid()) is not null) and s.student_id = (select auth.uid()))
        or (select public.is_admin())
        or public.is_visible_service(s.id)
      )
  )
);

-- Public student portfolio
drop policy if exists "portfolio_items_select_visible_or_owner" on public.portfolio_items;
create policy "portfolio_items_select_visible_or_owner"
on public.portfolio_items
for select
to anon, authenticated
using (
  (((select auth.uid()) is not null) and student_id = (select auth.uid()))
  or (select public.is_admin())
  or (
    visibility = 'public'
    and public.is_visible_profile(student_id)
  )
);

drop policy if exists "portfolio_assets_select" on public.portfolio_assets;
create policy "portfolio_assets_select"
on public.portfolio_assets
for select
to anon, authenticated
using (
  exists (
    select 1
    from public.portfolio_items p
    where p.id = portfolio_item_id
      and (
        (((select auth.uid()) is not null) and p.student_id = (select auth.uid()))
        or (select public.is_admin())
        or (p.visibility = 'public' and public.is_visible_profile(p.student_id))
      )
  )
);

-- Public media bucket must be readable without session so guests can
-- actually see profile, service and portfolio images.
drop policy if exists "public_media_read_authenticated" on storage.objects;
drop policy if exists "public_media_read_public" on storage.objects;
create policy "public_media_read_public"
on storage.objects
for select
to anon, authenticated
using (bucket_id = 'public-media');

commit;
