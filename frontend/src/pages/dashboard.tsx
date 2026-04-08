import { Link } from 'react-router-dom';
import { useAuth } from '@/hooks/use-auth';
import { useMyLeagues } from '@/hooks/use-leagues';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { buttonVariants } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { PageSpinner } from '@/components/spinner';
import { ErrorAlert } from '@/components/error-alert';
import { EmptyState } from '@/components/empty-state';

export function DashboardPage() {
  const { user } = useAuth();
  const { data: leagues, isLoading, error, refetch } = useMyLeagues();

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight">
          Welcome, {user?.displayName}
        </h1>
        <Link to="/join" className={buttonVariants({ variant: 'default' })}>
          Join League
        </Link>
      </div>

      {isLoading && <PageSpinner message="Loading leagues..." />}

      {error && (
        <ErrorAlert message="Failed to load your leagues." onRetry={() => refetch()} />
      )}

      {!isLoading && !error && leagues && leagues.length === 0 && (
        <EmptyState
          message="You haven't joined any leagues yet."
          action={
            <Link to="/join" className={buttonVariants({ variant: 'outline' })}>
              Join your first league
            </Link>
          }
        />
      )}

      {leagues && leagues.length > 0 && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {leagues.map((league) => (
            <Link key={league.id} to={`/leagues/${league.id}`}>
              <Card className="transition-shadow hover:shadow-md">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-lg font-semibold">{league.name}</CardTitle>
                  <Badge variant="secondary">{league.season}</Badge>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-muted-foreground">
                    {league.memberCount} {league.memberCount === 1 ? 'member' : 'members'}
                  </p>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
