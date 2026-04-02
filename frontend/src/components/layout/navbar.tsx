import { useAuth } from '@/hooks/use-auth';
import { getLogoutUrl } from '@/lib/api';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Link } from 'react-router-dom';

export function Navbar() {
  const { user, isAdmin } = useAuth();

  const initials = user?.displayName
    ?.split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  return (
    <header className="border-b bg-background">
      <div className="flex h-14 items-center justify-between px-4">
        <Link to="/" className="text-lg font-semibold tracking-tight">
          Soccer Predictions
        </Link>

        <div className="flex items-center gap-2">
          {isAdmin && (
            <Button variant="ghost" size="sm" render={<Link to="/admin" />}>
              Admin
            </Button>
          )}

          <DropdownMenu>
            <DropdownMenuTrigger>
              <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                <Avatar className="h-8 w-8">
                  <AvatarImage src={user?.pictureUrl ?? undefined} alt={user?.displayName} />
                  <AvatarFallback>{initials}</AvatarFallback>
                </Avatar>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <div className="flex items-center gap-2 p-2">
                <div className="flex flex-col space-y-1">
                  <p className="text-sm font-medium">{user?.displayName}</p>
                  <p className="text-xs text-muted-foreground">{user?.email}</p>
                </div>
              </div>
              <DropdownMenuSeparator />
              <DropdownMenuItem>
                <a href={getLogoutUrl()}>Log out</a>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  );
}
