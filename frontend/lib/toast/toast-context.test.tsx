import { act, render, screen } from "@testing-library/react";
import { ToastProvider, useToast } from "./toast-context";
import { ToastViewport } from "./toast-viewport";

function Harness() {
  const toast = useToast();
  return (
    <>
      <button type="button" onClick={() => toast.success("Saved.")}>
        ok
      </button>
      <button type="button" onClick={() => toast.error("Nope.")}>
        fail
      </button>
      <ToastViewport />
    </>
  );
}

function renderHarness() {
  return render(
    <ToastProvider>
      <Harness />
    </ToastProvider>,
  );
}

beforeEach(() => jest.useFakeTimers());
afterEach(() => jest.useRealTimers());

test("shows a success toast and auto-dismisses it", () => {
  renderHarness();

  act(() => {
    screen.getByRole("button", { name: "ok" }).click();
  });
  expect(screen.getByRole("status")).toHaveTextContent("Saved.");

  act(() => {
    jest.advanceTimersByTime(4000);
  });
  expect(screen.queryByText("Saved.")).not.toBeInTheDocument();
});

test("an error toast uses role=alert and can be dismissed manually", () => {
  renderHarness();

  act(() => {
    screen.getByRole("button", { name: "fail" }).click();
  });
  const alert = screen.getByRole("alert");
  expect(alert).toHaveTextContent("Nope.");

  act(() => {
    screen.getByRole("button", { name: "Dismiss" }).click();
  });
  expect(screen.queryByText("Nope.")).not.toBeInTheDocument();
});

test("useToast outside a provider is a safe no-op", () => {
  function Bare() {
    const toast = useToast();
    return (
      <button type="button" onClick={() => toast.success("ignored")}>
        go
      </button>
    );
  }
  render(<Bare />);
  expect(() =>
    screen.getByRole("button", { name: "go" }).click(),
  ).not.toThrow();
});
