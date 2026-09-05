import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import type { AuthUser } from "@/lib/api-client";
import { useAuth } from "@/lib/auth-context";
import { landingRouteForRole } from "@/lib/landing-route";
import type { Credentials } from "./validate-credentials";

export function useLoginMutation() {
  const { login } = useAuth();
  const router = useRouter();

  return useMutation<AuthUser, Error, Credentials>({
    mutationFn: ({ email, password }) => login(email, password),
    onSuccess: (user) => router.push(landingRouteForRole(user.role)),
  });
}
