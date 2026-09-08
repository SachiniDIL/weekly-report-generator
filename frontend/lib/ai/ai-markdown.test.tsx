import { render, screen, within } from "@testing-library/react";
import { AiMarkdown } from "./ai-markdown";

it("renders ### lines as headings with the marker stripped", () => {
  render(<AiMarkdown text={"### Completed work\nShipped the thing."} />);

  const heading = screen.getByRole("heading", { name: "Completed work" });
  expect(heading).toBeInTheDocument();
  expect(heading.textContent).not.toContain("#");
  expect(screen.getByText("Shipped the thing.")).toBeInTheDocument();
});

it("groups consecutive bullet lines into one list", () => {
  render(
    <AiMarkdown text={"* First blocker\n* Second blocker\n- Third blocker"} />,
  );

  const list = screen.getByRole("list");
  const items = within(list).getAllByRole("listitem");
  expect(items.map((li) => li.textContent)).toEqual([
    "First blocker",
    "Second blocker",
    "Third blocker",
  ]);
  expect(list.tagName).toBe("UL");
});

it("renders numbered lines as an ordered list", () => {
  render(<AiMarkdown text={"1. Do this\n2. Then that"} />);
  expect(screen.getByRole("list").tagName).toBe("OL");
});

it("renders **bold** and `code` inline without the syntax", () => {
  render(<AiMarkdown text={"Team **Client A** shipped `checkout`."} />);

  const strong = screen.getByText("Client A");
  expect(strong.tagName).toBe("STRONG");
  expect(screen.getByText("checkout").tagName).toBe("CODE");
  expect(screen.queryByText(/\*\*/)).not.toBeInTheDocument();
});

it("separates blocks split by a blank line into paragraphs", () => {
  render(<AiMarkdown text={"First para.\n\nSecond para."} />);
  expect(screen.getByText("First para.").tagName).toBe("P");
  expect(screen.getByText("Second para.").tagName).toBe("P");
});

it("drops a '---' thematic break instead of printing it", () => {
  render(<AiMarkdown text={"### A\nfirst\n\n---\n\n### B\nsecond"} />);
  expect(screen.queryByText("---")).not.toBeInTheDocument();
  expect(screen.getByRole("heading", { name: "A" })).toBeInTheDocument();
  expect(screen.getByRole("heading", { name: "B" })).toBeInTheDocument();
});
