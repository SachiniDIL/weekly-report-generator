export interface RegistrationForm {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export type RegistrationErrors = Partial<Record<keyof RegistrationForm, string>>;

/**
 * Client-side checks only: required fields and the password-match confirmation.
 * confirmPassword is never sent to the backend; the backend re-validates name/email/password.
 */
export function validateRegistration({
  name,
  email,
  password,
  confirmPassword,
}: RegistrationForm): RegistrationErrors {
  const errors: RegistrationErrors = {};
  if (!name.trim()) {
    errors.name = "Name is required";
  }
  if (!email.trim()) {
    errors.email = "Email is required";
  }
  if (!password) {
    errors.password = "Password is required";
  }
  if (confirmPassword !== password) {
    errors.confirmPassword = "Passwords do not match";
  }
  return errors;
}
