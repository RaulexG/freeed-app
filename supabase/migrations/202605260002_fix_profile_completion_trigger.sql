begin;

-- The original handle_profile_completion_refresh() used
-- coalesce(new.id, new.profile_id, ...) but the trigger fires on three
-- tables: profiles (column id) and student_profiles/company_profiles
-- (column profile_id). PL/pgSQL evaluates each argument before coalesce,
-- so referencing new.id on student_profiles/company_profiles raises
-- `record "new" has no field "id"` and breaks every onboarding UPDATE.
-- We branch on tg_table_name so each invocation only reads columns that
-- actually exist on the row being modified.

create or replace function public.handle_profile_completion_refresh()
returns trigger
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
  target_profile_id uuid;
begin
  if tg_table_name = 'profiles' then
    target_profile_id := coalesce(new.id, old.id);
  else
    target_profile_id := coalesce(new.profile_id, old.profile_id);
  end if;

  perform public.refresh_profile_completion(target_profile_id);
  return coalesce(new, old);
end;
$$;

commit;
