export type EmailErrors = { email?: string };

// Loose "looks like an email" check for fast feedback only; the backend does the real validation.
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateEmail(email: string): EmailErrors {
  const trimmed = email.trim();
  if (!trimmed) {
    return { email: "Email is required" };
  }
  if (!EMAIL_PATTERN.test(trimmed)) {
    return { email: "Enter a valid email address" };
  }
  return {};
}
