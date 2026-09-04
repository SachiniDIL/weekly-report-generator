export interface Credentials {
  email: string;
  password: string;
}

export type CredentialErrors = Partial<Record<keyof Credentials, string>>;

export function validateCredentials({ email, password }: Credentials): CredentialErrors {
  const errors: CredentialErrors = {};
  if (!email.trim()) {
    errors.email = "Email is required";
  }
  if (!password) {
    errors.password = "Password is required";
  }
  return errors;
}
