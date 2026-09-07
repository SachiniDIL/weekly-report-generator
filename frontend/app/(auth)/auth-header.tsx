import { BrandMark, BrandRule } from "@/lib/brand";

/** Wordmark + gradient rule + the form's own title, shown at the top of every auth card. */
export function AuthHeader({ title }: { title: string }) {
  return (
    <div className="flex flex-col gap-2">
      <BrandMark className="text-lg" />
      <BrandRule />
      <h1 className="mt-1 text-xl font-bold text-dusk-primary">{title}</h1>
    </div>
  );
}
