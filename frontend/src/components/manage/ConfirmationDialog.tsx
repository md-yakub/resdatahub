"use client";

interface ConfirmationDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel: string;
  busy?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmationDialog({
  open,
  title,
  message,
  confirmLabel,
  busy,
  onConfirm,
  onCancel
}: ConfirmationDialogProps) {
  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4">
      <div className="w-full max-w-md border border-line bg-white p-6 shadow-lg">
        <h2 className="text-lg font-semibold text-ink">{title}</h2>
        <p className="mt-3 text-sm leading-6 text-muted">{message}</p>
        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={busy}
            className="border border-line px-4 py-2 text-sm text-muted hover:text-ink disabled:opacity-60"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={busy}
            className="bg-ink px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {busy ? "Working..." : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
