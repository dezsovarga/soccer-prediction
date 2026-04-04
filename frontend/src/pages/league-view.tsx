import { useParams, Link } from 'react-router-dom';
import { useLeague, useFixtures, useStandings } from '@/hooks/use-leagues';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { buttonVariants } from '@/components/ui/button';
import type { FixtureDto } from '@/lib/types';

function groupByMatchday(fixtures: FixtureDto[]): Map<number, FixtureDto[]> {
  const map = new Map<number, FixtureDto[]>();
  for (const f of fixtures) {
    const group = map.get(f.matchday) ?? [];
    group.push(f);
    map.set(f.matchday, group);
  }
  return map;
}

function statusBadgeVariant(status: string): 'default' | 'secondary' | 'destructive' | 'outline' {
  switch (status) {
    case 'LIVE': return 'destructive';
    case 'FINISHED': return 'secondary';
    default: return 'outline';
  }
}

export function LeagueViewPage() {
  const { id } = useParams<{ id: string }>();
  const { data: league, isLoading: leagueLoading } = useLeague(id!);
  const { data: fixtures, isLoading: fixturesLoading } = useFixtures(id!);
  const { data: standings, isLoading: standingsLoading } = useStandings(id!);

  if (leagueLoading) return <p className="text-muted-foreground">Loading...</p>;
  if (!league) return <p>League not found.</p>;

  const matchdays = fixtures ? groupByMatchday(fixtures) : new Map();

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link to="/" className={buttonVariants({ variant: 'ghost', size: 'sm' })}>
          &larr; Back
        </Link>
        <h1 className="text-2xl font-bold tracking-tight">{league.name}</h1>
        <Badge variant="secondary">{league.season}</Badge>
      </div>

      <Tabs defaultValue="fixtures">
        <TabsList>
          <TabsTrigger value="fixtures">Fixtures</TabsTrigger>
          <TabsTrigger value="standings">Standings</TabsTrigger>
        </TabsList>

        <TabsContent value="fixtures" className="space-y-4">
          {fixturesLoading && <p className="text-muted-foreground">Loading fixtures...</p>}
          {fixtures && fixtures.length === 0 && (
            <p className="text-muted-foreground">No fixtures yet. Data will appear after sync.</p>
          )}
          {[...matchdays.entries()]
            .sort(([a], [b]) => a - b)
            .map(([matchday, matchdayFixtures]) => (
              <Card key={matchday}>
                <CardHeader className="pb-2">
                  <CardTitle className="text-base">Matchday {matchday}</CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  {matchdayFixtures.map((fixture) => (
                    <div
                      key={fixture.id}
                      className="flex items-center justify-between rounded-md border p-3"
                    >
                      <div className="flex items-center gap-2">
                        {fixture.homeTeamLogo && (
                          <img src={fixture.homeTeamLogo} alt="" className="h-5 w-5" />
                        )}
                        <span className="font-medium">{fixture.homeTeam}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        {fixture.status === 'FINISHED' || fixture.status === 'LIVE' ? (
                          <span className="font-bold">
                            {fixture.homeScore} - {fixture.awayScore}
                          </span>
                        ) : (
                          <span className="text-sm text-muted-foreground">
                            {new Date(fixture.kickoff).toLocaleDateString(undefined, {
                              month: 'short',
                              day: 'numeric',
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                          </span>
                        )}
                        <Badge variant={statusBadgeVariant(fixture.status)}>
                          {fixture.status}
                        </Badge>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="font-medium">{fixture.awayTeam}</span>
                        {fixture.awayTeamLogo && (
                          <img src={fixture.awayTeamLogo} alt="" className="h-5 w-5" />
                        )}
                      </div>
                    </div>
                  ))}
                </CardContent>
              </Card>
            ))}
        </TabsContent>

        <TabsContent value="standings">
          {standingsLoading && <p className="text-muted-foreground">Loading standings...</p>}
          {standings && standings.length === 0 && (
            <p className="text-muted-foreground">No standings yet. Data will appear after sync.</p>
          )}
          {standings && standings.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-10">#</TableHead>
                  <TableHead>Team</TableHead>
                  <TableHead className="text-center">P</TableHead>
                  <TableHead className="text-center">W</TableHead>
                  <TableHead className="text-center">D</TableHead>
                  <TableHead className="text-center">L</TableHead>
                  <TableHead className="text-center">GF</TableHead>
                  <TableHead className="text-center">GA</TableHead>
                  <TableHead className="text-center">GD</TableHead>
                  <TableHead className="text-center">Pts</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {standings.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell>{s.rank}</TableCell>
                    <TableCell className="flex items-center gap-2">
                      {s.teamLogo && <img src={s.teamLogo} alt="" className="h-5 w-5" />}
                      {s.teamName}
                    </TableCell>
                    <TableCell className="text-center">{s.played}</TableCell>
                    <TableCell className="text-center">{s.won}</TableCell>
                    <TableCell className="text-center">{s.drawn}</TableCell>
                    <TableCell className="text-center">{s.lost}</TableCell>
                    <TableCell className="text-center">{s.goalsFor}</TableCell>
                    <TableCell className="text-center">{s.goalsAgainst}</TableCell>
                    <TableCell className="text-center">{s.goalDiff}</TableCell>
                    <TableCell className="text-center font-bold">{s.points}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
