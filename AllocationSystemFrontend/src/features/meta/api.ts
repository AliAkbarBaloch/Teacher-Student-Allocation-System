export async function fetchSchoolTypes()
{
    const res = await fetch("/api/meta/school-type");
    if(!res.ok) throw new Error("Failed to load school types");
    return res.json();
}