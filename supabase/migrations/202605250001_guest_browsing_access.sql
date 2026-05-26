drop policy if exists "profiles_select_visible_or_own" on public.profiles;
create policy "profiles_select_visible_or_own"
on public.profiles
for select
to anon, authenticated
using (
  (auth.uid() is not null and id = auth.uid())
  or public.is_admin()
  or public.is_visible_profile(id)
);

drop policy if exists "student_profiles_select_visible_or_own" on public.student_profiles;
create policy "student_profiles_select_visible_or_own"
on public.student_profiles
for select
to anon, authenticated
using (
  (auth.uid() is not null and profile_id = auth.uid())
  or public.is_admin()
  or public.is_visible_profile(profile_id)
);

drop policy if exists "company_profiles_select_visible_or_own" on public.company_profiles;
create policy "company_profiles_select_visible_or_own"
on public.company_profiles
for select
to anon, authenticated
using (
  (auth.uid() is not null and profile_id = auth.uid())
  or public.is_admin()
  or public.is_visible_profile(profile_id)
);

drop policy if exists "categories_read_authenticated" on public.categories;
create policy "categories_read_public"
on public.categories
for select
to anon, authenticated
using (is_active = true or public.is_admin());

drop policy if exists "services_select_visible_or_owner" on public.services;
create policy "services_select_visible_or_owner"
on public.services
for select
to anon, authenticated
using (
  (auth.uid() is not null and student_id = auth.uid())
  or public.is_admin()
  or public.is_visible_service(id)
);
