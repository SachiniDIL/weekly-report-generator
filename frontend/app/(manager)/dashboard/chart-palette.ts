/** Dusk Glass chart colours — accent-light leads, indigo shades follow. */
export const CHART_ACCENT = "#a5b4fc";

export const CHART_PALETTE = [
  "#a5b4fc",
  "#818cf8",
  "#6366f1",
  "#c4b5fd",
  "#4ade80",
  "#fbbf24",
];

export const CHART_GRID_STROKE = "rgba(99, 120, 200, 0.15)";

export const CHART_AXIS_STROKE = "rgba(99, 120, 200, 0.3)";

export const CHART_TICK = { fill: "#6272a4", fontSize: 12 } as const;

export const CHART_TOOLTIP_STYLE = {
  background: "#1e2950",
  border: "1px solid rgba(99, 120, 200, 0.3)",
  borderRadius: 8,
  color: "#e0e7ff",
} as const;

export const CHART_TOOLTIP_LABEL_STYLE = { color: "#a5b4fc" } as const;

export const CHART_TOOLTIP_ITEM_STYLE = { color: "#e0e7ff" } as const;
