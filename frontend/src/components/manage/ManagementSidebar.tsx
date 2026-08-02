"use client";

const steps = ["Basic metadata", "Creators", "Discovery and rights", "Files", "Review and publish"];

interface ManagementSidebarProps {
  currentStep: number;
  highestEnabledStep: number;
  onSelectStep: (step: number) => void;
}

export function ManagementSidebar({ currentStep, highestEnabledStep, onSelectStep }: ManagementSidebarProps) {
  return (
    <nav className="border border-line bg-white p-4">
      <ol className="space-y-2">
        {steps.map((step, index) => {
          const enabled = index <= highestEnabledStep;
          const active = index === currentStep;

          return (
            <li key={step}>
              <button
                type="button"
                disabled={!enabled}
                onClick={() => onSelectStep(index)}
                className={`w-full px-3 py-2 text-left text-sm ${
                  active ? "bg-ink font-medium text-white" : "text-muted hover:bg-panel hover:text-ink"
                } disabled:cursor-not-allowed disabled:opacity-50`}
              >
                <span className="mr-2 text-xs">{index + 1}</span>
                {step}
              </button>
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
