interface GraphErrorStateProps {
  message: string;
  onRetry: () => void;
}

export function GraphErrorState({ message, onRetry }: GraphErrorStateProps) {
  return (
    <div className="border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-800" role="alert">
      <p className="font-medium">Could not load the knowledge graph.</p>
      <p className="mt-1">{message}</p>
      <button
        type="button"
        onClick={onRetry}
        className="focus-ring mt-4 border border-red-300 bg-white px-3 py-2 text-sm font-medium text-red-800"
      >
        Try again
      </button>
    </div>
  );
}
