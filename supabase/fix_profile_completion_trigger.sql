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
