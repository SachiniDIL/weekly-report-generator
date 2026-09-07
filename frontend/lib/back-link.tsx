"use client";

import { ChevronLeft } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";

/** "← My Reports" style link back to a known destination. */
export function BackLink({ href, label }: { href: string; label: string }) {
  return (
    <Link href={href} className="dusk-back-link">
      <ChevronLeft size={14} aria-hidden />
      {label}
    </Link>
  );
}

/** "← Back" — for pages reached from several places; uses browser history. */
export function BackButton({ label = "Back" }: { label?: string }) {
  const router = useRouter();
  return (
    <button
      type="button"
      onClick={() => router.back()}
      className="dusk-back-link"
    >
      <ChevronLeft size={14} aria-hidden />
      {label}
    </button>
  );
}
