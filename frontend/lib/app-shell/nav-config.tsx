import {
  ClipboardCheck,
  FileText,
  FolderKanban,
  LayoutDashboard,
  PlusCircle,
  Shield,
  Users,
  type LucideIcon,
} from "lucide-react";

export interface NavItem {
  href: string;
  label: string;
  icon: LucideIcon;
}

export const MEMBER_NAV: NavItem[] = [
  { href: "/reports", label: "Reports", icon: FileText },
  { href: "/reports/new", label: "New Report", icon: PlusCircle },
];

export const MANAGER_NAV: NavItem[] = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/projects", label: "Projects", icon: FolderKanban },
  { href: "/team", label: "Team", icon: Users },
  { href: "/review", label: "Review Queue", icon: ClipboardCheck },
];

export const ADMIN_NAV: NavItem[] = [
  { href: "/admin/users", label: "Users", icon: Shield },
];
