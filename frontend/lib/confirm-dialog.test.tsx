import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { useState } from "react";
import { ConfirmProvider, useConfirm } from "./confirm-dialog";

function Harness() {
  const confirm = useConfirm();
  const [result, setResult] = useState<string>("idle");

  return (
    <>
      <button
        type="button"
        onClick={async () => {
          const ok = await confirm({
            title: "Delete it?",
            message: "This cannot be undone.",
            confirmLabel: "Delete",
            tone: "danger",
          });
          setResult(ok ? "confirmed" : "cancelled");
        }}
      >
        act
      </button>
      <output>{result}</output>
    </>
  );
}

function renderHarness() {
  return render(
    <ConfirmProvider>
      <Harness />
    </ConfirmProvider>,
  );
}

test("resolves true when the user confirms, then closes", async () => {
  renderHarness();

  fireEvent.click(screen.getByRole("button", { name: "act" }));
  const dialog = await screen.findByRole("alertdialog");
  fireEvent.click(screen.getByRole("button", { name: "Delete" }));

  await waitFor(() =>
    expect(screen.getByText("confirmed")).toBeInTheDocument(),
  );
  expect(dialog).not.toBeInTheDocument();
});

test("resolves false when the user cancels", async () => {
  renderHarness();

  fireEvent.click(screen.getByRole("button", { name: "act" }));
  await screen.findByRole("alertdialog");
  fireEvent.click(screen.getByRole("button", { name: "Cancel" }));

  await waitFor(() =>
    expect(screen.getByText("cancelled")).toBeInTheDocument(),
  );
  expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
});

test("useConfirm outside a provider resolves false without throwing", async () => {
  render(<Harness />);

  fireEvent.click(screen.getByRole("button", { name: "act" }));

  await waitFor(() =>
    expect(screen.getByText("cancelled")).toBeInTheDocument(),
  );
  expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
});
