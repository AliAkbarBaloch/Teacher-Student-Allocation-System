
import { InternshipTypeService, type InternshipType } from "@/features/internship-types";
import { useQuery } from "@tanstack/react-query";

const useInternshipTypes = () => useQuery<InternshipType[]>({
  queryKey: ["internshipTypes"],
  queryFn: () => InternshipTypeService.getAll(),
  staleTime: 5 * 60 * 1000, // 1 minute before data becomes stale
  gcTime: 10 * 60 * 1000, // 5 minutes cache time (previously cacheTime)
  refetchOnWindowFocus: true, // refetch when window regains focus
  refetchOnMount: true, // refetch when component mounts
  retry: 3
});

export default useInternshipTypes;
