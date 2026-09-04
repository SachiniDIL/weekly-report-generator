import { useMutation } from "@tanstack/react-query";
import { register, type MessageResponse, type RegisterPayload } from "@/lib/api-client";

export function useRegisterMutation() {
  return useMutation<MessageResponse, Error, RegisterPayload>({
    mutationFn: register,
  });
}
