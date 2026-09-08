"use client";

import { LogOut, Menu, X } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";
import type { AuthUser } from "@/lib/api-client";
import { Avatar } from "@/lib/avatar";
import { useAuth } from "@/lib/auth-context";
import { BrandMark } from "@/lib/brand";
import { greeting } from "./greeting";
import {
  ADMIN_NAV,
  MANAGER_NAV,
  MEMBER_NAV,
  type AppShellVariant,
  type NavItem,
} from "./nav-config";
import { pageTitleForPath } from "./page-title";
import { useNavBadges, type NavBadge } from "./use-nav-badges";

const NAV_BY_VARIANT: Record<AppShellVariant, NavItem[]> = {
  member: MEMBER_NAV,
  manager: MANAGER_NAV,
  admin: ADMIN_NAV,
};

export type { AppShellVariant };

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
  const badges = useNavBadges(variant);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  // Close the mobile drawer whenever the route changes.
  useEffect(() => {
    setMobileNavOpen(false);
  }, [pathname]);

  const sidebar = (
    <SidebarContent
      nav={nav}
      badges={badges}
      pathname={pathname}
      user={user}
      onSignOut={logout}
      onNavigate={() => setMobileNavOpen(false)}
    />
  );

  return (
    <div className="min-h-screen">
      <aside className="dusk-sidebar fixed inset-y-0 left-0 z-40 hidden w-[220px] flex-col md:flex">
        {sidebar}
      </aside>

      <MobileNavDrawer
        open={mobileNavOpen}
        onClose={() => setMobileNavOpen(false)}
      >
        {sidebar}
      </MobileNavDrawer>

      <div className="flex min-h-screen min-w-0 flex-col md:pl-[220px]">
        <Topbar
          title={pageTitleForPath(pathname)}
          greetingText={greeting(user?.name)}
          onOpenNav={() => setMobileNavOpen(true)}
        />
        <div
          key={pathname ?? "page"}
          className="dusk-page min-w-0 flex-1 px-4 py-5 sm:px-6 sm:py-6 md:px-8 md:py-7"
        >
          {children}
        </div>
      </div>
    </div>
  );
}

function MobileNavDrawer({
  open,
  onClose,
  children,
}: {
  open: boolean;
  onClose: () => void;
  children: ReactNode;
}) {
  useEffect(() => {
    if (!open) {
      return;
    }
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose();
      }
    }
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [open, onClose]);

  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 md:hidden">
      <div
        className="absolute inset-0"
        style={{ background: "rgba(57, 65, 90, 0.35)" }}
        onClick={onClose}
        aria-hidden
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Menu"
        className="dusk-sidebar absolute inset-y-0 left-0 flex w-[248px] max-w-[82vw] flex-col"
      >
        <button
          type="button"
          onClick={onClose}
          aria-label="Close menu"
          className="absolute right-2 top-2 rounded-md p-1 text-dusk-secondary hover:text-dusk-primary"
        >
          <X size={18} aria-hidden />
        </button>
        {children}
      </div>
    </div>
  );
}

function SidebarContent({
  nav,
  badges,
  pathname,
  user,
  onSignOut,
  onNavigate,
}: {
  nav: NavItem[];
  badges: Record<string, NavBadge>;
  pathname: string | null;
  user: AuthUser | null;
  onSignOut: () => void;
  onNavigate: () => void;
}) {
  return (
    <>
      <div className="px-4 py-4 text-base">
        <Link href="/" onClick={onNavigate}>
          <BrandMark />
        </Link>
      </div>

      <nav
        className="flex flex-1 flex-col gap-0.5 overflow-y-auto px-2 py-2"
        aria-label="Primary"
      >
        {nav.map((item) => {
          const active = isNavActive(pathname, item.href);
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              onClick={onNavigate}
              aria-current={active ? "page" : undefined}
              className={`dusk-nav-item flex items-center gap-2.5 rounded-r-md px-3 py-2 text-sm ${
                active ? "is-active" : ""
              }`}
            >
              <Icon size={16} aria-hidden />
              <span className="flex-1">{item.label}</span>
              <NavBadgeMarker badge={badges[item.href]} label={item.label} />
            </Link>
          );
        })}
      </nav>

      {user ? <UserFooter user={user} onSignOut={onSignOut} /> : null}
    </>
  );
}

function NavBadgeMarker({
  badge,
  label,
}: {
  badge: NavBadge | undefined;
  label: string;
}) {
  if (!badge) {
    return null;
  }
  if (badge.count != null) {
    return (
      <span
        className="dusk-nav-count"
        aria-label={`${label}: ${badge.count} waiting`}
      >
        {badge.count}
      </span>
    );
  }
  if (badge.dot) {
    return (
      <>
        <span className="dusk-nav-dot" aria-hidden />
        <span className="sr-only">— changes requested</span>
      </>
    );
  }
  return null;
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
  onOpenNav,
}: {
  title: string;
  greetingText: string;
  onOpenNav: () => void;
}) {
  return (
    <header
      className="sticky top-0 z-30 flex h-14 shrink-0 items-center gap-3 px-4 sm:px-6 md:px-8"
      style={{
        background: "var(--nm-bg)",
        boxShadow: "0 8px 18px -12px rgba(57, 65, 90, 0.4)",
      }}
    >
      <button
        type="button"
        onClick={onOpenNav}
        aria-label="Open menu"
        className="-ml-1 shrink-0 rounded-md p-1.5 text-dusk-secondary hover:text-dusk-primary md:hidden"
      >
        <Menu size={20} aria-hidden />
      </button>
      <h1 className="min-w-0 flex-1 truncate text-sm font-semibold text-dusk-primary">
        {title}
      </h1>
      <p className="hidden shrink-0 text-xs text-dusk-secondary sm:block">
        {greetingText}
      </p>
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
