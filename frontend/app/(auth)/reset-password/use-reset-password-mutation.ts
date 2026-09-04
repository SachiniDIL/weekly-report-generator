import { useMutation } from "@tanstack/react-query";
import { resetPassword, type MessageResponse, type ResetPasswordPayload } from "@/lib/api-client";

export function useResetPasswordMutation() {
  return useMutation<MessageResponse, Error, ResetPasswordPayload>({
    mutationFn: resetPassword,
  });
}
