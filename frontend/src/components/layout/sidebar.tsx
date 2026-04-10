import { NavLink } from 'react-router-dom';
import { cn } from '@/lib/utils';

const adminLinks = [
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/leagues', label: 'Leagues' },
];

interface SidebarProps {
  open?: boolean;
  onNavigate?: () => void;
}

export function Sidebar({ open, onNavigate }: SidebarProps) {
  return (
    <aside
      className={cn(
        'fixed inset-y-0 left-0 z-40 mt-14 w-56 shrink-0 border-r bg-background p-4 transition-transform duration-200 md:static md:z-auto md:mt-0 md:translate-x-0 md:bg-muted/40',
        open ? 'translate-x-0' : '-translate-x-full'
      )}
    >
      <nav className="flex flex-col gap-1">
        <p className="mb-2 text-xs font-semibold uppercase text-muted-foreground">
          Admin
        </p>
        {adminLinks.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            onClick={onNavigate}
            className={({ isActive }) =>
              cn(
                'rounded-md px-3 py-2 text-sm transition-colors hover:bg-accent',
                isActive && 'bg-accent font-medium'
              )
            }
          >
            {link.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
