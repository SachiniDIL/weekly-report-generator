import { validateNewPassword } from "./validate-new-password";

describe("validateNewPassword", () => {
  it("requires a new password", () => {
    expect(validateNewPassword({ newPassword: "", confirmPassword: "" })).toEqual({
      newPassword: "Password is required",
    });
  });

  it("flags a confirmation mismatch", () => {
    expect(
      validateNewPassword({ newPassword: "new-secret-1", confirmPassword: "different" }),
    ).toEqual({ confirmPassword: "Passwords do not match" });
  });

  it("returns no errors when the password is set and confirmed", () => {
    expect(
      validateNewPassword({ newPassword: "new-secret-1", confirmPassword: "new-secret-1" }),
    ).toEqual({});
  });
});
