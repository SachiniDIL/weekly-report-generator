import { useQuery } from "@tanstack/react-query";
import { listUsers, type UserBasicView } from "@/lib/api/users";

/**
 * Users a manager may add to a project. The backend already rejects assigning an ADMIN, so
 * they're filtered out here too rather than offered as a dead option.
 */
export function useAssignableUsersQuery() {
  return useQuery<UserBasicView[]>({
    queryKey: ["users", "assignable"],
    queryFn: () => listUsers(),
    select: (users) => users.filter((user) => user.role !== "ADMIN"),
  });
}
