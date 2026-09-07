interface TitleRule {
  pattern: RegExp;
  title: string;
}

/** Most specific first. */
const RULES: TitleRule[] = [
  { pattern: /^\/reports\/new$/, title: "New Report" },
  { pattern: /^\/reports\/[^/]+\/review$/, title: "Review Report" },
  { pattern: /^\/reports\/[^/]+\/view$/, title: "Report" },
  { pattern: /^\/reports\/[^/]+$/, title: "Edit Report" },
  { pattern: /^\/reports$/, title: "My Reports" },
  { pattern: /^\/dashboard$/, title: "Dashboard" },
  { pattern: /^\/projects$/, title: "Projects" },
  { pattern: /^\/review$/, title: "Review Queue" },
  { pattern: /^\/team\/[^/]+$/, title: "Team Member" },
  { pattern: /^\/team$/, title: "Team" },
  { pattern: /^\/admin\/users$/, title: "User Management" },
];

export function pageTitleForPath(pathname: string | null | undefined): string {
  if (!pathname) {
    return "";
  }
  return RULES.find((rule) => rule.pattern.test(pathname))?.title ?? "";
}
