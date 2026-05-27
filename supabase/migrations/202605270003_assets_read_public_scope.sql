begin;

-- Allow anonymous/authenticated users to read metadata of assets that are
-- explicitly public_read (or otherwise readable by existing helper rules).
drop policy if exists "assets_select_visible_to_owner_or_linked_users" on public.assets;
create policy "assets_select_visible_to_owner_or_linked_users"
on public.assets
for select
to anon, authenticated
using (
  owner_id = (select auth.uid())
  or (select public.is_admin())
  or public.can_read_asset(id)
);

commit;
