export interface PasswordResetFields {
  newPassword: string;
  confirmPassword: string;
}

export type PasswordResetErrors = Partial<Record<keyof PasswordResetFields, string>>;

/**
 * Client-side checks only: a non-empty password and the match confirmation.
 * confirmPassword is never sent; the backend enforces the length rule.
 */
export function validateNewPassword({
  newPassword,
  confirmPassword,
}: PasswordResetFields): PasswordResetErrors {
  const errors: PasswordResetErrors = {};
  if (!newPassword) {
    errors.newPassword = "Password is required";
  }
  if (confirmPassword !== newPassword) {
    errors.confirmPassword = "Passwords do not match";
  }
  return errors;
}
