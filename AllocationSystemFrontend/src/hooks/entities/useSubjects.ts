
import { SubjectService, type Subject } from "@/features/subjects";
import { useQuery } from "@tanstack/react-query";

const useSubjects = () => useQuery<Subject[]>({
  queryKey: ["subjects"],
  queryFn: () => SubjectService.getAll(),
  staleTime: 5 * 60 * 1000, // 1 minute before data becomes stale
  gcTime: 10 * 60 * 1000, // 5 minutes cache time (previously cacheTime)
  refetchOnWindowFocus: true, // refetch when window regains focus
  refetchOnMount: true, // refetch when component mounts
  retry: 3
});

export default useSubjects;
