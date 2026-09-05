import { useQuery } from "@tanstack/react-query";
import { listAdminUsers, type AdminUserView } from "@/lib/api/admin-users";

/** Every user, all statuses — the page splits them into pending vs active sections. */
export function useAdminUsersQuery() {
  return useQuery<AdminUserView[]>({
    queryKey: ["admin-users"],
    queryFn: () => listAdminUsers(),
  });
}
