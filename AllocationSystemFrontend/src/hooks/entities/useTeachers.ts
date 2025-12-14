
import { TeacherService, type Teacher } from "@/features/teachers";
import { useQuery } from "@tanstack/react-query";

const useTeachers = () => useQuery<Teacher[]>({
  queryKey: ["teachers"],
  queryFn: () => TeacherService.getAll(),
  staleTime: 5 * 60 * 1000, // 1 minute before data becomes stale
  gcTime: 10 * 60 * 1000, // 5 minutes cache time (previously cacheTime)
  refetchOnWindowFocus: true, // refetch when window regains focus
  refetchOnMount: true, // refetch when component mounts
  retry: 3
});

export default useTeachers;
