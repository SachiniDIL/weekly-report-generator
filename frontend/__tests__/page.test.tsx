import { render, screen } from "@testing-library/react";
import Home from "@/app/page";

describe("Home page", () => {
  it("renders the app name and sign-in links", () => {
    render(<Home />);

    expect(screen.getByText("Weekly Report Generator")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Sign in" })).toHaveAttribute(
      "href",
      "/login",
    );
    expect(
      screen.getByRole("link", { name: "Create an account" }),
    ).toHaveAttribute("href", "/register");
  });
});
