
import { SchoolService } from "@/features/schools/services/schoolService";
import type { School } from "@/features/schools/types/school.types";
import { useQuery } from "@tanstack/react-query";

const useSchools = () => useQuery<School[]>({
  queryKey: ["schools"],
  queryFn: () => SchoolService.getAll(),
  staleTime: 5 * 60 * 1000, // 1 minute before data becomes stale
  gcTime: 10 * 60 * 1000, // 5 minutes cache time (previously cacheTime)
  refetchOnWindowFocus: true, // refetch when window regains focus
  refetchOnMount: true, // refetch when component mounts
  retry: 3
});

export default useSchools;
