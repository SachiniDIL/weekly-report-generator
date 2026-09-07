/** Soft-UI chart colours — indigo leads, saturated enough to read on the light surface. */
export const CHART_ACCENT = "#5b63e6";

export const CHART_PALETTE = [
  "#5b63e6",
  "#818cf8",
  "#a78bfa",
  "#2f9e5f",
  "#e07b1a",
  "#38bdf8",
];

export const CHART_GRID_STROKE = "rgba(57, 65, 90, 0.08)";

export const CHART_AXIS_STROKE = "rgba(57, 65, 90, 0.15)";

export const CHART_TICK = { fill: "#767f95", fontSize: 12 } as const;

export const CHART_TOOLTIP_STYLE = {
  background: "#ffffff",
  border: "1px solid rgba(57, 65, 90, 0.1)",
  borderRadius: 10,
  color: "#39415a",
} as const;

export const CHART_TOOLTIP_LABEL_STYLE = { color: "#5b63e6" } as const;

export const CHART_TOOLTIP_ITEM_STYLE = { color: "#39415a" } as const;
