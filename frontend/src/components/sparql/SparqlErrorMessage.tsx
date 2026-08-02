"use client";

interface SparqlErrorMessageProps {
  message: string;
}

export function SparqlErrorMessage({ message }: SparqlErrorMessageProps) {
  const title = getTitle(message);

  return (
    <div className="border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
      <p className="font-medium">{title}</p>
      <p className="mt-1 leading-6">{message}</p>
    </div>
  );
}

function getTitle(message: string) {
  const normalized = message.toLowerCase();

  if (normalized.includes("408") || normalized.includes("timed out")) {
    return "Query timed out";
  }

  if (normalized.includes("only sparql select") || normalized.includes("update operations") || normalized.includes("service clauses")) {
    return "Unsupported query type";
  }

  if (normalized.includes("400") || normalized.includes("invalid sparql")) {
    return "Invalid SPARQL query";
  }

  return "Could not run query";
}
