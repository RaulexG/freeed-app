begin;

-- Storage object paths use '/' but the original schema used '\' inside
-- split_part(). That made `is_asset_path_owned_by_user(name)` and the
-- read helper always compare auth.uid() against the full path, which is
-- never equal, so every authenticated upload to public-media is rejected
-- by RLS. We rewrite both helpers to split on '/'.

create or replace function public.is_asset_path_owned_by_user(target_object_path text)
returns boolean
language sql
stable
set search_path = public, pg_temp
as $$
  select split_part(target_object_path, '/', 1) = auth.uid()::text
$$;

create or replace function public.can_read_storage_object(target_bucket_id text, target_object_path text)
returns boolean
language sql
stable
set search_path = public, pg_temp
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

commit;
