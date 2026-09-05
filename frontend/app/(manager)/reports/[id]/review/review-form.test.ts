import { validateReviewInput } from "./review-form";

describe("validateReviewInput", () => {
  it("requires a comment when requesting changes", () => {
    expect(validateReviewInput({ action: "CHANGES_REQUESTED", comment: "" })).toBe(
      "Explain what needs to change before requesting changes.",
    );
  });

  it("treats a whitespace-only comment as missing when requesting changes", () => {
    expect(validateReviewInput({ action: "CHANGES_REQUESTED", comment: "   " })).not.toBeNull();
  });

  it("accepts a real comment when requesting changes", () => {
    expect(validateReviewInput({ action: "CHANGES_REQUESTED", comment: "Add the hours" })).toBeNull();
  });

  it("allows an empty comment when approving", () => {
    expect(validateReviewInput({ action: "APPROVED", comment: "" })).toBeNull();
  });

  it("allows a comment when approving", () => {
    expect(validateReviewInput({ action: "APPROVED", comment: "Nice work" })).toBeNull();
  });
});
