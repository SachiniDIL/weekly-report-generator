import { render, screen } from "@testing-library/react";
import Home from "@/app/page";

describe("Home page", () => {
  it("renders the getting-started instructions", () => {
    render(<Home />);

    expect(screen.getByText(/get started by editing/i)).toBeInTheDocument();
  });
});
