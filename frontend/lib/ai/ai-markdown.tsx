import type { ReactNode } from "react";

/**
 * A tiny Markdown renderer for the small, predictable output the AI assistant produces —
 * headings, bullet/numbered lists, paragraphs, and inline bold/italic/code. Not a general
 * Markdown parser; just enough to turn the model's `### heading` / `* item` / `**bold**` text
 * into something readable without pulling in a dependency.
 */
export function AiMarkdown({ text }: { text: string }) {
  return <div className="flex flex-col gap-2">{renderBlocks(text)}</div>;
}

type ListKind = "ul" | "ol";

function renderBlocks(text: string): ReactNode[] {
  const lines = text.replace(/\r\n/g, "\n").split("\n");
  const blocks: ReactNode[] = [];

  let paragraph: string[] = [];
  let list: { kind: ListKind; items: string[] } | null = null;

  const flushParagraph = () => {
    if (paragraph.length > 0) {
      blocks.push(
        <p key={`p-${blocks.length}`} className="leading-relaxed">
          {inline(paragraph.join(" "))}
        </p>,
      );
      paragraph = [];
    }
  };

  const flushList = () => {
    if (list && list.items.length > 0) {
      const items = list.items.map((item, index) => (
        <li key={index}>{inline(item)}</li>
      ));
      blocks.push(
        list.kind === "ol" ? (
          <ol
            key={`l-${blocks.length}`}
            className="ml-5 flex list-decimal flex-col gap-1"
          >
            {items}
          </ol>
        ) : (
          <ul
            key={`l-${blocks.length}`}
            className="ml-5 flex list-disc flex-col gap-1 marker:text-dusk-muted"
          >
            {items}
          </ul>
        ),
      );
    }
    list = null;
  };

  for (const rawLine of lines) {
    const line = rawLine.trimEnd();

    // A "---" / "***" thematic break — headings already separate sections, so drop it.
    if (/^\s*([-*_])\1{2,}\s*$/.test(line)) {
      flushParagraph();
      flushList();
      continue;
    }

    const heading = line.match(/^(#{1,6})\s+(.*)$/);
    if (heading) {
      flushParagraph();
      flushList();
      const level = Math.min(heading[1].length, 3);
      const Tag = (level <= 2 ? "h3" : "h4") as "h3" | "h4";
      blocks.push(
        <Tag
          key={`h-${blocks.length}`}
          className="mt-1 text-xs font-semibold uppercase tracking-wide text-dusk-accent-light first:mt-0"
        >
          {inline(heading[2])}
        </Tag>,
      );
      continue;
    }

    const bullet = line.match(/^\s*[*\-+]\s+(.*)$/);
    const numbered = line.match(/^\s*\d+[.)]\s+(.*)$/);
    if (bullet || numbered) {
      flushParagraph();
      const kind: ListKind = numbered ? "ol" : "ul";
      if (!list || list.kind !== kind) {
        flushList();
        list = { kind, items: [] };
      }
      list.items.push((bullet ?? numbered)![1]);
      continue;
    }

    if (line.trim() === "") {
      flushParagraph();
      flushList();
      continue;
    }

    flushList();
    paragraph.push(line.trim());
  }

  flushParagraph();
  flushList();
  return blocks;
}

const INLINE = /(\*\*([^*]+)\*\*|__([^_]+)__|`([^`]+)`|\*([^*]+)\*|_([^_]+)_)/g;

/** Turns `**bold**`, `*italic*`, `_italic_`, and `` `code` `` into elements; leaves the rest as text. */
function inline(text: string): ReactNode[] {
  const nodes: ReactNode[] = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;
  let key = 0;

  INLINE.lastIndex = 0;
  while ((match = INLINE.exec(text)) !== null) {
    if (match.index > lastIndex) {
      nodes.push(text.slice(lastIndex, match.index));
    }
    const bold = match[2] ?? match[3];
    const code = match[4];
    const italic = match[5] ?? match[6];
    if (bold != null) {
      nodes.push(
        <strong key={key++} className="font-semibold text-dusk-primary">
          {bold}
        </strong>,
      );
    } else if (code != null) {
      nodes.push(
        <code
          key={key++}
          className="rounded bg-black/[0.06] px-1 py-0.5 font-mono text-[0.85em]"
        >
          {code}
        </code>,
      );
    } else if (italic != null) {
      nodes.push(<em key={key++}>{italic}</em>);
    }
    lastIndex = match.index + match[0].length;
  }
  if (lastIndex < text.length) {
    nodes.push(text.slice(lastIndex));
  }
  return nodes;
}
