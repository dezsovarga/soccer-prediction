import { NavLink } from 'react-router-dom';
import { cn } from '@/lib/utils';

const adminLinks = [
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/leagues', label: 'Leagues' },
];

export function Sidebar() {
  return (
    <aside className="w-56 shrink-0 border-r bg-muted/40 p-4">
      <nav className="flex flex-col gap-1">
        <p className="mb-2 text-xs font-semibold uppercase text-muted-foreground">
          Admin
        </p>
        {adminLinks.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
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
