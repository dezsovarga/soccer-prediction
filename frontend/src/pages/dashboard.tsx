import { Link } from 'react-router-dom';
import { useAuth } from '@/hooks/use-auth';
import { useMyLeagues } from '@/hooks/use-leagues';
import { useDashboardData } from '@/hooks/use-dashboard';
import type { UpcomingFixture, RecentResult, LeagueWithStats } from '@/hooks/use-dashboard';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { buttonVariants } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { PageSpinner } from '@/components/spinner';
import { ErrorAlert } from '@/components/error-alert';
import { EmptyState } from '@/components/empty-state';

function formatKickoff(kickoff: string): string {
  const date = new Date(kickoff);
  const now = new Date();
  const diffMs = date.getTime() - now.getTime();
  const diffHours = diffMs / (1000 * 60 * 60);

  if (diffHours < 24 && diffHours > 0) {
    const hours = Math.floor(diffHours);
    const minutes = Math.floor((diffMs / (1000 * 60)) % 60);
    if (hours === 0) return `${minutes}m`;
    return `${hours}h ${minutes}m`;
  }

  return date.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function UpcomingFixtureCard({ item }: { item: UpcomingFixture }) {
  const { fixture, leagueId, leagueName, predicted } = item;
  return (
    <Link to={`/leagues/${leagueId}`}>
      <div className="flex flex-col gap-1.5 rounded-lg border p-3 transition-colors hover:bg-muted/50">
        <div className="flex items-center justify-between">
          <span className="text-xs text-muted-foreground">{leagueName}</span>
          <span className="text-xs text-muted-foreground">{formatKickoff(fixture.kickoff)}</span>
        </div>
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-1.5 min-w-0">
            {fixture.homeTeamLogo && <img src={fixture.homeTeamLogo} alt="" className="h-5 w-7 shrink-0 object-contain" />}
            <span className="text-sm font-medium truncate">{fixture.homeTeam}</span>
          </div>
          <span className="text-xs text-muted-foreground shrink-0">vs</span>
          <div className="flex items-center gap-1.5 min-w-0 justify-end">
            <span className="text-sm font-medium truncate">{fixture.awayTeam}</span>
            {fixture.awayTeamLogo && <img src={fixture.awayTeamLogo} alt="" className="h-5 w-7 shrink-0 object-contain" />}
          </div>
        </div>
        {predicted ? (
          <Badge variant="secondary" className="self-start text-xs">Predicted</Badge>
        ) : (
          <Badge variant="destructive" className="self-start text-xs">Needs prediction</Badge>
        )}
      </div>
    </Link>
  );
}

function RecentResultCard({ item }: { item: RecentResult }) {
  const { prediction, leagueId, leagueName } = item;
  const points = prediction.pointsEarned ?? 0;
  const isExact = prediction.homeScore === prediction.fixtureHomeScore && prediction.awayScore === prediction.fixtureAwayScore;

  return (
    <Link to={`/leagues/${leagueId}`}>
      <div className="flex flex-col gap-1.5 rounded-lg border p-3 transition-colors hover:bg-muted/50">
        <div className="flex items-center justify-between">
          <span className="text-xs text-muted-foreground">{leagueName}</span>
          {isExact && points > 0 && (
            <Badge className="bg-green-600 text-xs">Exact!</Badge>
          )}
        </div>
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-1.5 min-w-0">
            {prediction.fixtureHomeTeamLogo && <img src={prediction.fixtureHomeTeamLogo} alt="" className="h-5 w-7 shrink-0 object-contain" />}
            <span className="text-sm font-medium truncate">{prediction.fixtureHomeTeam}</span>
          </div>
          <div className="flex flex-col items-center shrink-0">
            <span className="text-sm font-bold">
              {prediction.fixtureHomeScore} - {prediction.fixtureAwayScore}
            </span>
            <span className="text-[10px] text-muted-foreground">
              You: {prediction.homeScore} - {prediction.awayScore}
            </span>
          </div>
          <div className="flex items-center gap-1.5 min-w-0 justify-end">
            <span className="text-sm font-medium truncate">{prediction.fixtureAwayTeam}</span>
            {prediction.fixtureAwayTeamLogo && <img src={prediction.fixtureAwayTeamLogo} alt="" className="h-5 w-7 shrink-0 object-contain" />}
          </div>
        </div>
        <div className="flex items-center justify-between">
          <span className={`text-xs font-semibold ${points > 0 ? 'text-green-600 dark:text-green-400' : 'text-muted-foreground'}`}>
            +{points} pts
          </span>
        </div>
      </div>
    </Link>
  );
}

function LeagueCard({ stat }: { stat: LeagueWithStats }) {
  const { league, rank, totalPoints, upcomingCount } = stat;
  return (
    <Link to={`/leagues/${league.id}`}>
      <Card className="transition-shadow hover:shadow-md h-full">
        <CardHeader className="pb-2">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg font-semibold">{league.name}</CardTitle>
            <Badge variant="secondary">{league.season}</Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="grid grid-cols-3 gap-2 text-center">
            <div>
              <p className="text-2xl font-bold">{rank ?? '-'}</p>
              <p className="text-xs text-muted-foreground">Rank</p>
            </div>
            <div>
              <p className="text-2xl font-bold">{totalPoints}</p>
              <p className="text-xs text-muted-foreground">Points</p>
            </div>
            <div>
              <p className="text-2xl font-bold">{league.memberCount}</p>
              <p className="text-xs text-muted-foreground">Members</p>
            </div>
          </div>
          {upcomingCount > 0 && (
            <p className="text-xs text-amber-600 dark:text-amber-400 font-medium">
              {upcomingCount} {upcomingCount === 1 ? 'match' : 'matches'} awaiting prediction
            </p>
          )}
        </CardContent>
      </Card>
    </Link>
  );
}

export function DashboardPage() {
  const { user } = useAuth();
  const { data: leagues, isLoading, error, refetch } = useMyLeagues();
  const { upcoming, recent, leagueStats, isLoading: dashLoading } = useDashboardData(leagues, user?.id);

  if (isLoading) return <PageSpinner message="Loading dashboard..." />;
  if (error) return <ErrorAlert message="Failed to load your leagues." onRetry={() => refetch()} />;

  if (!leagues || leagues.length === 0) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold tracking-tight">Welcome, {user?.displayName}</h1>
        <EmptyState
          message="You haven't joined any leagues yet."
          action={
            <Link to="/join" className={buttonVariants({ variant: 'outline' })}>
              Join your first league
            </Link>
          }
        />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight">
          Welcome, {user?.displayName}
        </h1>
        <Link to="/join" className={buttonVariants({ variant: 'default', size: 'sm' })}>
          Join League
        </Link>
      </div>

      {dashLoading && <PageSpinner message="Loading dashboard data..." />}

      {!dashLoading && (
        <>
          {upcoming.length > 0 && (
            <section className="space-y-3">
              <h2 className="text-lg font-semibold tracking-tight">Upcoming Matches</h2>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {upcoming.map((item) => (
                  <UpcomingFixtureCard key={`${item.leagueId}-${item.fixture.id}`} item={item} />
                ))}
              </div>
            </section>
          )}

          <section className="space-y-3">
            <h2 className="text-lg font-semibold tracking-tight">Your Leagues</h2>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {leagueStats.map((stat) => (
                <LeagueCard key={stat.league.id} stat={stat} />
              ))}
            </div>
          </section>

          {recent.length > 0 && (
            <section className="space-y-3">
              <h2 className="text-lg font-semibold tracking-tight">Recent Results</h2>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {recent.map((item) => (
                  <RecentResultCard key={`${item.leagueId}-${item.prediction.id}`} item={item} />
                ))}
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}
