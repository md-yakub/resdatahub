export function EmptyState({ title, message }: { title: string; message: string }) {
  return (
    <div className="border border-dashed border-line bg-panel px-6 py-10 text-center">
      <h2 className="text-base font-semibold text-ink">{title}</h2>
      <p className="mt-2 text-sm text-muted">{message}</p>
    </div>
  );
}
