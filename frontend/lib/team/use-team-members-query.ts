import { useQuery } from "@tanstack/react-query";
import { listUsers, type UserBasicView } from "@/lib/api/users";

/** Active MEMBER-role users, for the manager's team index. */
export function useTeamMembersQuery() {
  return useQuery<UserBasicView[]>({
    queryKey: ["users", "team-members"],
    queryFn: () => listUsers("MEMBER"),
  });
}
