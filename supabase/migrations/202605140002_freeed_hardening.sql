begin;

create index if not exists profiles_avatar_asset_idx
  on public.profiles (avatar_asset_id)
  where avatar_asset_id is not null;

create index if not exists student_profiles_cv_asset_idx
  on public.student_profiles (cv_asset_id)
  where cv_asset_id is not null;

create index if not exists company_profiles_logo_asset_idx
  on public.company_profiles (logo_asset_id)
  where logo_asset_id is not null;

create index if not exists profile_skills_skill_idx
  on public.profile_skills (skill_id);

create index if not exists service_assets_asset_idx
  on public.service_assets (asset_id);

create index if not exists portfolio_assets_asset_idx
  on public.portfolio_assets (asset_id);

create index if not exists favorite_services_service_idx
  on public.favorite_services (service_id);

create index if not exists request_assets_asset_idx
  on public.request_assets (asset_id);

create index if not exists request_status_history_changed_by_idx
  on public.request_status_history (changed_by)
  where changed_by is not null;

create index if not exists reviews_reviewer_company_idx
  on public.reviews (reviewer_company_id);

alter function public.set_updated_at() set search_path = public, pg_temp;
alter function public.current_app_role() set search_path = public, pg_temp;
alter function public.is_admin() set search_path = public, pg_temp;
alter function public.is_student() set search_path = public, pg_temp;
alter function public.is_company() set search_path = public, pg_temp;
alter function public.is_visible_profile(uuid) set search_path = public, pg_temp;
alter function public.is_visible_service(uuid) set search_path = public, pg_temp;
alter function public.can_access_request(uuid) set search_path = public, pg_temp;
alter function public.can_read_asset(uuid) set search_path = public, pg_temp;
alter function public.can_read_storage_object(text, text) set search_path = public, pg_temp;
alter function public.is_asset_path_owned_by_user(text) set search_path = public, pg_temp;
alter function public.refresh_profile_completion(uuid) set search_path = public, pg_temp;
alter function public.handle_profile_completion_refresh() set search_path = public, pg_temp;
alter function public.prevent_role_change() set search_path = public, pg_temp;
alter function public.validate_student_profile_role() set search_path = public, pg_temp;
alter function public.validate_profile_avatar() set search_path = public, pg_temp;
alter function public.validate_student_cv_asset() set search_path = public, pg_temp;
alter function public.validate_company_profile_role() set search_path = public, pg_temp;
alter function public.validate_company_logo_asset() set search_path = public, pg_temp;
alter function public.validate_service_owner() set search_path = public, pg_temp;
alter function public.validate_asset_owner_and_scope() set search_path = public, pg_temp;
alter function public.validate_service_asset() set search_path = public, pg_temp;
alter function public.validate_portfolio_asset() set search_path = public, pg_temp;
alter function public.validate_request_asset() set search_path = public, pg_temp;
alter function public.validate_service_request() set search_path = public, pg_temp;
alter function public.validate_service_request_status_transition() set search_path = public, pg_temp;
alter function public.log_request_status_history() set search_path = public, pg_temp;
alter function public.validate_review() set search_path = public, pg_temp;
alter function public.handle_new_user() set search_path = public, pg_temp;
alter function public.sync_profile_from_auth() set search_path = public, pg_temp;

revoke all on function public.refresh_profile_completion(uuid) from public, anon, authenticated;
revoke all on function public.handle_profile_completion_refresh() from public, anon, authenticated;
revoke all on function public.log_request_status_history() from public, anon, authenticated;
revoke all on function public.handle_new_user() from public, anon, authenticated;
revoke all on function public.sync_profile_from_auth() from public, anon, authenticated;

drop policy if exists "public_media_read_authenticated" on storage.objects;

drop policy if exists "profiles_select_visible_or_own" on public.profiles;
create policy "profiles_select_visible_or_own"
on public.profiles
for select
to authenticated
using (
  id = (select auth.uid())
  or (select public.is_admin())
  or public.is_visible_profile(id)
);

drop policy if exists "profiles_update_own" on public.profiles;
create policy "profiles_update_own"
on public.profiles
for update
to authenticated
using (id = (select auth.uid()) or (select public.is_admin()))
with check (id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "student_profiles_select_visible_or_own" on public.student_profiles;
create policy "student_profiles_select_visible_or_own"
on public.student_profiles
for select
to authenticated
using (
  profile_id = (select auth.uid())
  or (select public.is_admin())
  or public.is_visible_profile(profile_id)
);

drop policy if exists "student_profiles_upsert_own" on public.student_profiles;
create policy "student_profiles_insert_own"
on public.student_profiles
for insert
to authenticated
with check (profile_id = (select auth.uid()) or (select public.is_admin()));

create policy "student_profiles_update_own"
on public.student_profiles
for update
to authenticated
using (profile_id = (select auth.uid()) or (select public.is_admin()))
with check (profile_id = (select auth.uid()) or (select public.is_admin()));

create policy "student_profiles_delete_own"
on public.student_profiles
for delete
to authenticated
using (profile_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "company_profiles_select_visible_or_own" on public.company_profiles;
create policy "company_profiles_select_visible_or_own"
on public.company_profiles
for select
to authenticated
using (
  profile_id = (select auth.uid())
  or (select public.is_admin())
  or public.is_visible_profile(profile_id)
);

drop policy if exists "company_profiles_upsert_own" on public.company_profiles;
create policy "company_profiles_insert_own"
on public.company_profiles
for insert
to authenticated
with check (profile_id = (select auth.uid()) or (select public.is_admin()));

create policy "company_profiles_update_own"
on public.company_profiles
for update
to authenticated
using (profile_id = (select auth.uid()) or (select public.is_admin()))
with check (profile_id = (select auth.uid()) or (select public.is_admin()));

create policy "company_profiles_delete_own"
on public.company_profiles
for delete
to authenticated
using (profile_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "skills_read_authenticated" on public.skills;
create policy "skills_read_authenticated"
on public.skills
for select
to authenticated
using (is_active = true or (select public.is_admin()));

drop policy if exists "skills_manage_admin" on public.skills;
create policy "skills_insert_admin"
on public.skills
for insert
to authenticated
with check ((select public.is_admin()));

create policy "skills_update_admin"
on public.skills
for update
to authenticated
using ((select public.is_admin()))
with check ((select public.is_admin()));

create policy "skills_delete_admin"
on public.skills
for delete
to authenticated
using ((select public.is_admin()));

drop policy if exists "profile_skills_select_visible_or_own" on public.profile_skills;
create policy "profile_skills_select_visible_or_own"
on public.profile_skills
for select
to authenticated
using (
  profile_id = (select auth.uid())
  or (select public.is_admin())
  or public.is_visible_profile(profile_id)
);

drop policy if exists "profile_skills_manage_own" on public.profile_skills;
create policy "profile_skills_insert_own"
on public.profile_skills
for insert
to authenticated
with check (profile_id = (select auth.uid()) or (select public.is_admin()));

create policy "profile_skills_update_own"
on public.profile_skills
for update
to authenticated
using (profile_id = (select auth.uid()) or (select public.is_admin()))
with check (profile_id = (select auth.uid()) or (select public.is_admin()));

create policy "profile_skills_delete_own"
on public.profile_skills
for delete
to authenticated
using (profile_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "categories_read_authenticated" on public.categories;
create policy "categories_read_authenticated"
on public.categories
for select
to authenticated
using (is_active = true or (select public.is_admin()));

drop policy if exists "categories_manage_admin" on public.categories;
create policy "categories_insert_admin"
on public.categories
for insert
to authenticated
with check ((select public.is_admin()));

create policy "categories_update_admin"
on public.categories
for update
to authenticated
using ((select public.is_admin()))
with check ((select public.is_admin()));

create policy "categories_delete_admin"
on public.categories
for delete
to authenticated
using ((select public.is_admin()));

drop policy if exists "services_select_visible_or_owner" on public.services;
create policy "services_select_visible_or_owner"
on public.services
for select
to authenticated
using (
  student_id = (select auth.uid())
  or (select public.is_admin())
  or public.is_visible_service(id)
);

drop policy if exists "services_insert_owner" on public.services;
create policy "services_insert_owner"
on public.services
for insert
to authenticated
with check (
  student_id = (select auth.uid())
  and (select public.is_student())
);

drop policy if exists "services_update_owner" on public.services;
create policy "services_update_owner"
on public.services
for update
to authenticated
using (student_id = (select auth.uid()) or (select public.is_admin()))
with check (student_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "services_delete_owner" on public.services;
create policy "services_delete_owner"
on public.services
for delete
to authenticated
using (student_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "assets_select_visible_to_owner_or_linked_users" on public.assets;
create policy "assets_select_visible_to_owner_or_linked_users"
on public.assets
for select
to authenticated
using (
  owner_id = (select auth.uid())
  or (select public.is_admin())
  or public.can_read_asset(id)
);

drop policy if exists "assets_insert_owner" on public.assets;
create policy "assets_insert_owner"
on public.assets
for insert
to authenticated
with check (
  owner_id = (select auth.uid())
  or (select public.is_admin())
);

drop policy if exists "assets_update_owner" on public.assets;
create policy "assets_update_owner"
on public.assets
for update
to authenticated
using (owner_id = (select auth.uid()) or (select public.is_admin()))
with check (owner_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "assets_delete_owner" on public.assets;
create policy "assets_delete_owner"
on public.assets
for delete
to authenticated
using (owner_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "service_assets_select" on public.service_assets;
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
        s.student_id = (select auth.uid())
        or (select public.is_admin())
        or public.is_visible_service(s.id)
      )
  )
);

drop policy if exists "service_assets_manage_owner" on public.service_assets;
create policy "service_assets_insert_owner"
on public.service_assets
for insert
to authenticated
with check (
  exists (
    select 1
    from public.services s
    where s.id = service_id
      and (s.student_id = (select auth.uid()) or (select public.is_admin()))
  )
);

create policy "service_assets_update_owner"
on public.service_assets
for update
to authenticated
using (
  exists (
    select 1
    from public.services s
    where s.id = service_id
      and (s.student_id = (select auth.uid()) or (select public.is_admin()))
  )
)
with check (
  exists (
    select 1
    from public.services s
    where s.id = service_id
      and (s.student_id = (select auth.uid()) or (select public.is_admin()))
  )
);

create policy "service_assets_delete_owner"
on public.service_assets
for delete
to authenticated
using (
  exists (
    select 1
    from public.services s
    where s.id = service_id
      and (s.student_id = (select auth.uid()) or (select public.is_admin()))
  )
);

drop policy if exists "portfolio_items_select_visible_or_owner" on public.portfolio_items;
create policy "portfolio_items_select_visible_or_owner"
on public.portfolio_items
for select
to authenticated
using (
  student_id = (select auth.uid())
  or (select public.is_admin())
  or (
    visibility = 'public'
    and public.is_visible_profile(student_id)
  )
);

drop policy if exists "portfolio_items_manage_owner" on public.portfolio_items;
create policy "portfolio_items_insert_owner"
on public.portfolio_items
for insert
to authenticated
with check (student_id = (select auth.uid()) or (select public.is_admin()));

create policy "portfolio_items_update_owner"
on public.portfolio_items
for update
to authenticated
using (student_id = (select auth.uid()) or (select public.is_admin()))
with check (student_id = (select auth.uid()) or (select public.is_admin()));

create policy "portfolio_items_delete_owner"
on public.portfolio_items
for delete
to authenticated
using (student_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "portfolio_assets_select" on public.portfolio_assets;
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
        p.student_id = (select auth.uid())
        or (select public.is_admin())
        or (p.visibility = 'public' and public.is_visible_profile(p.student_id))
      )
  )
);

drop policy if exists "portfolio_assets_manage_owner" on public.portfolio_assets;
create policy "portfolio_assets_insert_owner"
on public.portfolio_assets
for insert
to authenticated
with check (
  exists (
    select 1
    from public.portfolio_items p
    where p.id = portfolio_item_id
      and (p.student_id = (select auth.uid()) or (select public.is_admin()))
  )
);

create policy "portfolio_assets_update_owner"
on public.portfolio_assets
for update
to authenticated
using (
  exists (
    select 1
    from public.portfolio_items p
    where p.id = portfolio_item_id
      and (p.student_id = (select auth.uid()) or (select public.is_admin()))
  )
)
with check (
  exists (
    select 1
    from public.portfolio_items p
    where p.id = portfolio_item_id
      and (p.student_id = (select auth.uid()) or (select public.is_admin()))
  )
);

create policy "portfolio_assets_delete_owner"
on public.portfolio_assets
for delete
to authenticated
using (
  exists (
    select 1
    from public.portfolio_items p
    where p.id = portfolio_item_id
      and (p.student_id = (select auth.uid()) or (select public.is_admin()))
  )
);

drop policy if exists "favorite_services_select_own" on public.favorite_services;
create policy "favorite_services_select_own"
on public.favorite_services
for select
to authenticated
using (company_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "favorite_services_manage_own" on public.favorite_services;
create policy "favorite_services_insert_own"
on public.favorite_services
for insert
to authenticated
with check (
  (company_id = (select auth.uid()) and (select public.is_company()))
  or (select public.is_admin())
);

create policy "favorite_services_delete_own"
on public.favorite_services
for delete
to authenticated
using (company_id = (select auth.uid()) or (select public.is_admin()));

drop policy if exists "service_requests_select_participants" on public.service_requests;
create policy "service_requests_select_participants"
on public.service_requests
for select
to authenticated
using (
  company_id = (select auth.uid())
  or student_id = (select auth.uid())
  or (select public.is_admin())
);

drop policy if exists "service_requests_insert_company" on public.service_requests;
create policy "service_requests_insert_company"
on public.service_requests
for insert
to authenticated
with check (
  company_id = (select auth.uid())
  and (select public.is_company())
);

drop policy if exists "service_requests_update_participants" on public.service_requests;
create policy "service_requests_update_participants"
on public.service_requests
for update
to authenticated
using (
  company_id = (select auth.uid())
  or student_id = (select auth.uid())
  or (select public.is_admin())
)
with check (
  company_id = (select auth.uid())
  or student_id = (select auth.uid())
  or (select public.is_admin())
);

drop policy if exists "request_assets_select_participants" on public.request_assets;
create policy "request_assets_select_participants"
on public.request_assets
for select
to authenticated
using (public.can_access_request(request_id));

drop policy if exists "request_assets_manage_participants" on public.request_assets;
create policy "request_assets_insert_participants"
on public.request_assets
for insert
to authenticated
with check (public.can_access_request(request_id));

create policy "request_assets_update_participants"
on public.request_assets
for update
to authenticated
using (public.can_access_request(request_id))
with check (public.can_access_request(request_id));

create policy "request_assets_delete_participants"
on public.request_assets
for delete
to authenticated
using (public.can_access_request(request_id));

drop policy if exists "request_status_history_select_participants" on public.request_status_history;
create policy "request_status_history_select_participants"
on public.request_status_history
for select
to authenticated
using (public.can_access_request(request_id));

drop policy if exists "reviews_select_authenticated" on public.reviews;
create policy "reviews_select_authenticated"
on public.reviews
for select
to authenticated
using (true);

drop policy if exists "reviews_insert_company_owner" on public.reviews;
create policy "reviews_insert_company_owner"
on public.reviews
for insert
to authenticated
with check (
  reviewer_company_id = (select auth.uid())
  and (select public.is_company())
);

drop policy if exists "reviews_update_reviewer" on public.reviews;
create policy "reviews_update_reviewer"
on public.reviews
for update
to authenticated
using (reviewer_company_id = (select auth.uid()) or (select public.is_admin()))
with check (reviewer_company_id = (select auth.uid()) or (select public.is_admin()));

commit;
