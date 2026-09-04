import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth-context";
import type { Credentials } from "./validate-credentials";

const LANDING_ROUTE = "/dashboard";

export function useLoginMutation() {
  const { login } = useAuth();
  const router = useRouter();

  return useMutation<void, Error, Credentials>({
    mutationFn: ({ email, password }) => login(email, password),
    onSuccess: () => router.push(LANDING_ROUTE),
  });
}
