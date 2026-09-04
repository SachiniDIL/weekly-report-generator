import { useMutation } from "@tanstack/react-query";
import { forgotPassword, type MessageResponse } from "@/lib/api-client";

export function useForgotPasswordMutation() {
  return useMutation<MessageResponse, Error, string>({
    mutationFn: (email) => forgotPassword({ email }),
  });
}
