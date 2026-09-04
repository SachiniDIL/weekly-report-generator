import { validateEmail } from "./validate-email";

describe("validateEmail", () => {
  it("requires a non-blank value", () => {
    expect(validateEmail("   ")).toEqual({ email: "Email is required" });
  });

  it("rejects a value that is not shaped like an email", () => {
    expect(validateEmail("not-an-email")).toEqual({ email: "Enter a valid email address" });
  });

  it("accepts a plausible email address", () => {
    expect(validateEmail("ada@example.com")).toEqual({});
  });
});
