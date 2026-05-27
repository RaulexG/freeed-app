begin;

-- Allow authenticated users to propose/create new skills from the app.
-- Existing admin update/delete policies remain unchanged.
drop policy if exists "skills_insert_authenticated" on public.skills;
create policy "skills_insert_authenticated"
on public.skills
for insert
to authenticated
with check (
  coalesce(char_length(name), 0) between 2 and 80
  and coalesce(char_length(slug), 0) between 2 and 120
);

commit;
