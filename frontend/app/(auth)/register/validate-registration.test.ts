import { validateRegistration } from "./validate-registration";

const validForm = {
  name: "Ada Lovelace",
  email: "ada@example.com",
  password: "password1",
  confirmPassword: "password1",
};

describe("validateRegistration", () => {
  it("flags every empty required field", () => {
    expect(
      validateRegistration({ name: " ", email: "", password: "", confirmPassword: "" }),
    ).toEqual({
      name: "Name is required",
      email: "Email is required",
      password: "Password is required",
    });
  });

  it("flags a confirm-password mismatch", () => {
    expect(validateRegistration({ ...validForm, confirmPassword: "different" })).toEqual({
      confirmPassword: "Passwords do not match",
    });
  });

  it("returns no errors for a well-formed matching form", () => {
    expect(validateRegistration(validForm)).toEqual({});
  });
});
