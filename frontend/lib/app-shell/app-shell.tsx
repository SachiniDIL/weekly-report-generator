"use client";

import { LogOut } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import type { AuthUser } from "@/lib/api-client";
import { Avatar } from "@/lib/avatar";
import { useAuth } from "@/lib/auth-context";
import { BrandMark } from "@/lib/brand";
import { greeting } from "./greeting";
import { ADMIN_NAV, MANAGER_NAV, MEMBER_NAV, type NavItem } from "./nav-config";
import { pageTitleForPath } from "./page-title";

const NAV_BY_VARIANT: Record<AppShellVariant, NavItem[]> = {
  member: MEMBER_NAV,
  manager: MANAGER_NAV,
  admin: ADMIN_NAV,
};

export type AppShellVariant = "member" | "manager" | "admin";

export function AppShell({
  variant,
  children,
}: {
  variant: AppShellVariant;
  children: ReactNode;
}) {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const nav = NAV_BY_VARIANT[variant];

  return (
    <div className="min-h-screen">
      <Sidebar nav={nav} pathname={pathname} user={user} onSignOut={logout} />

      <div className="flex min-h-screen flex-col md:pl-[220px]">
        <Topbar
          title={pageTitleForPath(pathname)}
          greetingText={greeting(user?.name)}
        />
        <div key={pathname ?? "page"} className="dusk-page flex-1 px-8 py-7">
          {children}
        </div>
      </div>
    </div>
  );
}

function Sidebar({
  nav,
  pathname,
  user,
  onSignOut,
}: {
  nav: NavItem[];
  pathname: string | null;
  user: AuthUser | null;
  onSignOut: () => void;
}) {
  return (
    <aside className="dusk-sidebar fixed inset-y-0 left-0 z-40 hidden w-[220px] flex-col md:flex">
      <div className="px-4 py-4 text-base">
        <Link href="/">
          <BrandMark />
        </Link>
      </div>

      <nav
        className="flex flex-1 flex-col gap-0.5 px-2 py-2"
        aria-label="Primary"
      >
        {nav.map((item) => {
          const active = isNavActive(pathname, item.href);
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-current={active ? "page" : undefined}
              className={`dusk-nav-item flex items-center gap-2.5 rounded-r-md px-3 py-2 text-sm ${
                active ? "is-active" : ""
              }`}
            >
              <Icon size={16} aria-hidden />
              {item.label}
            </Link>
          );
        })}
      </nav>

      {user ? <UserFooter user={user} onSignOut={onSignOut} /> : null}
    </aside>
  );
}

function UserFooter({
  user,
  onSignOut,
}: {
  user: AuthUser;
  onSignOut: () => void;
}) {
  return (
    <div className="border-t border-white/10 px-3 py-3">
      <div className="flex items-center gap-2">
        <Avatar name={user.name} size={30} />
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium text-dusk-primary">
            {user.name}
          </p>
          <span className="dusk-badge mt-0.5" data-status={user.role}>
            {user.role}
          </span>
        </div>
      </div>
      <button type="button" onClick={onSignOut} className="dusk-back-link mt-2">
        <LogOut size={14} aria-hidden />
        Sign out
      </button>
    </div>
  );
}

function Topbar({
  title,
  greetingText,
}: {
  title: string;
  greetingText: string;
}) {
  return (
    <header
      className="sticky top-0 z-30 flex h-14 shrink-0 items-center justify-between gap-4 border-b px-8"
      style={{
        borderColor: "var(--border-subtle)",
        background: "rgba(13, 18, 41, 0.72)",
        backdropFilter: "blur(8px)",
        WebkitBackdropFilter: "blur(8px)",
      }}
    >
      <h1 className="truncate text-sm font-semibold text-dusk-primary">
        {title}
      </h1>
      <p className="shrink-0 text-xs text-dusk-secondary">{greetingText}</p>
    </header>
  );
}

function isNavActive(pathname: string | null, href: string): boolean {
  if (!pathname) {
    return false;
  }
  if (href === "/reports/new") {
    return pathname === "/reports/new";
  }
  if (href === "/reports") {
    return (
      pathname === "/reports" ||
      (pathname.startsWith("/reports/") && pathname !== "/reports/new")
    );
  }
  return pathname === href || pathname.startsWith(`${href}/`);
}
