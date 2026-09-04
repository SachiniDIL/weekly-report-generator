import { validateCredentials } from "./validate-credentials";

describe("validateCredentials", () => {
  it("flags both fields when they are empty or whitespace", () => {
    expect(validateCredentials({ email: "   ", password: "" })).toEqual({
      email: "Email is required",
      password: "Password is required",
    });
  });

  it("returns no errors when both fields are filled", () => {
    expect(validateCredentials({ email: "a@b.com", password: "secret" })).toEqual({});
  });
});
