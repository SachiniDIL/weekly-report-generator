import { fireEvent, screen, waitFor } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import { renderWithQueryClient } from "@/lib/test-render";
import { AiChatWidget } from "./ai-chat-widget";

function json(body: unknown): Response {
  return { ok: true, status: 200, json: async () => body } as unknown as Response;
}

function errorResponse(status: number, message: string): Response {
  return { ok: false, status, json: async () => ({ message }) } as unknown as Response;
}

describe("AiChatWidget", () => {
  let fetchMock: jest.Mock;

  beforeEach(() => {
    fetchMock = jest.fn();
    global.fetch = fetchMock as unknown as typeof fetch;
    setAuthToken(null);
  });

  it("opens and closes the chat panel from the toggle button", () => {
    renderWithQueryClient(<AiChatWidget />);

    expect(screen.queryByRole("region", { name: "AI assistant" })).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Ask AI" }));
    expect(screen.getByRole("region", { name: "AI assistant" })).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Hide assistant" }));
    expect(screen.queryByRole("region", { name: "AI assistant" })).not.toBeInTheDocument();
  });

  it("adds the question to the thread, then the assistant reply", async () => {
    fetchMock.mockResolvedValue(json({ message: "Two people are blocked this week." }));

    renderWithQueryClient(<AiChatWidget />);
    fireEvent.click(screen.getByRole("button", { name: "Ask AI" }));

    fireEvent.change(screen.getByLabelText("Your question"), {
      target: { value: "Who is blocked?" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Send" }));

    expect(screen.getByText("Who is blocked?")).toBeInTheDocument();
    expect(await screen.findByText("Two people are blocked this week.")).toBeInTheDocument();

    const [url, init] = fetchMock.mock.calls[0];
    expect(new URL(url).pathname).toBe("/ai/chat");
    expect(init.method).toBe("POST");
    expect(JSON.parse(init.body as string)).toEqual({ question: "Who is blocked?" });
  });

  it("disables the input and send button while a request is in flight", async () => {
    let resolveFetch: (response: Response) => void = () => {};
    fetchMock.mockImplementation(
      () =>
        new Promise<Response>((resolve) => {
          resolveFetch = resolve;
        }),
    );

    renderWithQueryClient(<AiChatWidget />);
    fireEvent.click(screen.getByRole("button", { name: "Ask AI" }));

    fireEvent.change(screen.getByLabelText("Your question"), {
      target: { value: "Anything pending?" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Send" }));

    await waitFor(() => expect(screen.getByLabelText("Your question")).toBeDisabled());
    expect(screen.getByRole("button", { name: "Send" })).toBeDisabled();

    resolveFetch(json({ message: "All clear." }));

    expect(await screen.findByText("All clear.")).toBeInTheDocument();
    expect(screen.getByLabelText("Your question")).not.toBeDisabled();
  });

  it("shows the backend error message and keeps the question in the thread", async () => {
    fetchMock.mockResolvedValue(errorResponse(400, "Question must not be blank."));

    renderWithQueryClient(<AiChatWidget />);
    fireEvent.click(screen.getByRole("button", { name: "Ask AI" }));

    fireEvent.change(screen.getByLabelText("Your question"), { target: { value: "hello" } });
    fireEvent.click(screen.getByRole("button", { name: "Send" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Question must not be blank.");
    expect(screen.getByText("hello")).toBeInTheDocument();
  });
});
