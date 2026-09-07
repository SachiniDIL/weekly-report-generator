/** 26 indigo/violet/cyan gradients — one per initial letter, so the same
 * person always gets the same colour. */
const GRADIENTS = [
  "linear-gradient(135deg, #6366f1, #a5b4fc)",
  "linear-gradient(135deg, #818cf8, #c4b5fd)",
  "linear-gradient(135deg, #4f46e5, #818cf8)",
  "linear-gradient(135deg, #7c3aed, #a78bfa)",
  "linear-gradient(135deg, #4338ca, #6366f1)",
  "linear-gradient(135deg, #6d28d9, #8b5cf6)",
  "linear-gradient(135deg, #5b21b6, #a855f7)",
  "linear-gradient(135deg, #2563eb, #60a5fa)",
  "linear-gradient(135deg, #1d4ed8, #818cf8)",
  "linear-gradient(135deg, #0e7490, #22d3ee)",
  "linear-gradient(135deg, #0891b2, #67e8f9)",
  "linear-gradient(135deg, #4338ca, #38bdf8)",
  "linear-gradient(135deg, #6366f1, #34d399)",
  "linear-gradient(135deg, #7c3aed, #f0abfc)",
  "linear-gradient(135deg, #4f46e5, #c084fc)",
  "linear-gradient(135deg, #3730a3, #6366f1)",
  "linear-gradient(135deg, #8b5cf6, #d8b4fe)",
  "linear-gradient(135deg, #6366f1, #93c5fd)",
  "linear-gradient(135deg, #5145cd, #a5b4fc)",
  "linear-gradient(135deg, #7e22ce, #c4b5fd)",
  "linear-gradient(135deg, #4c1d95, #7c3aed)",
  "linear-gradient(135deg, #1e40af, #6366f1)",
  "linear-gradient(135deg, #6366f1, #818cf8)",
  "linear-gradient(135deg, #9333ea, #c084fc)",
  "linear-gradient(135deg, #4338ca, #a78bfa)",
  "linear-gradient(135deg, #0369a1, #38bdf8)",
];

function initialAndGradient(name: string): {
  initial: string;
  gradient: string;
} {
  const trimmed = name.trim();
  const initial = (trimmed[0] ?? "?").toUpperCase();
  const code = initial.charCodeAt(0);
  const index = code >= 65 && code <= 90 ? code - 65 : code % GRADIENTS.length;
  return { initial, gradient: GRADIENTS[index % GRADIENTS.length] };
}

export function Avatar({ name, size = 24 }: { name: string; size?: number }) {
  const { initial, gradient } = initialAndGradient(name);
  return (
    <span
      aria-hidden
      className="inline-flex shrink-0 items-center justify-center rounded-full font-semibold text-white"
      style={{
        width: size,
        height: size,
        background: gradient,
        fontSize: Math.round(size * 0.42),
      }}
    >
      {initial}
    </span>
  );
}
