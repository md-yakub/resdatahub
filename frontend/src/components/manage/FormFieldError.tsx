export function FormFieldError({ message }: { message?: string | null }) {
  if (!message) {
    return null;
  }

  return <p className="mt-2 text-sm text-red-700">{message}</p>;
}
