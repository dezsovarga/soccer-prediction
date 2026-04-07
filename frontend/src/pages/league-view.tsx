import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useLeague, useFixtures, useStandings, useLeaderboard } from '@/hooks/use-leagues';
import {
  useMyPredictions,
  useSavePrediction,
  usePlayers,
  useTopScorerPick,
  useSaveTopScorerPick,
  useLeagueWinnerPick,
  useSaveLeagueWinnerPick,
} from '@/hooks/use-predictions';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button, buttonVariants } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type { FixtureDto, PredictionDto, LeaderboardEntryDto } from '@/lib/types';

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

function FixtureCard({
  fixture,
  prediction,
  onSavePrediction,
  isSaving,
}: {
  fixture: FixtureDto;
  prediction: PredictionDto | undefined;
  onSavePrediction: (fixtureId: string, homeScore: number, awayScore: number) => void;
  isSaving: boolean;
}) {
  const [homeScore, setHomeScore] = useState<string>(prediction?.homeScore?.toString() ?? '');
  const [awayScore, setAwayScore] = useState<string>(prediction?.awayScore?.toString() ?? '');
  const [dirty, setDirty] = useState(false);

  const kickoffPassed = new Date(fixture.kickoff) <= new Date();
  const canPredict = !kickoffPassed && fixture.status === 'SCHEDULED';

  function handleSave() {
    const h = parseInt(homeScore, 10);
    const a = parseInt(awayScore, 10);
    if (isNaN(h) || isNaN(a) || h < 0 || a < 0) return;
    onSavePrediction(fixture.id, h, a);
    setDirty(false);
  }

  return (
    <div className="flex flex-col gap-2 rounded-md border p-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {fixture.homeTeamLogo && <img src={fixture.homeTeamLogo} alt="" className="h-5 w-5" />}
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
          <Badge variant={statusBadgeVariant(fixture.status)}>{fixture.status}</Badge>
        </div>
        <div className="flex items-center gap-2">
          <span className="font-medium">{fixture.awayTeam}</span>
          {fixture.awayTeamLogo && <img src={fixture.awayTeamLogo} alt="" className="h-5 w-5" />}
        </div>
      </div>

      {canPredict && (
        <div className="flex items-center justify-center gap-2 pt-1">
          <span className="text-xs text-muted-foreground">Your prediction:</span>
          <Input
            type="number"
            min={0}
            className="w-14 text-center"
            placeholder="-"
            value={homeScore}
            onChange={(e) => { setHomeScore(e.target.value); setDirty(true); }}
            aria-label={`Home score prediction for ${fixture.homeTeam} vs ${fixture.awayTeam}`}
          />
          <span className="text-sm font-medium">-</span>
          <Input
            type="number"
            min={0}
            className="w-14 text-center"
            placeholder="-"
            value={awayScore}
            onChange={(e) => { setAwayScore(e.target.value); setDirty(true); }}
            aria-label={`Away score prediction for ${fixture.homeTeam} vs ${fixture.awayTeam}`}
          />
          <Button
            size="sm"
            disabled={!dirty || isSaving || homeScore === '' || awayScore === ''}
            onClick={handleSave}
          >
            {prediction ? 'Update' : 'Save'}
          </Button>
        </div>
      )}

      {!canPredict && prediction && (
        <div className="flex items-center justify-center gap-2 pt-1 text-sm">
          <span className="text-muted-foreground">Your prediction:</span>
          <span className="font-medium">{prediction.homeScore} - {prediction.awayScore}</span>
          {prediction.pointsEarned !== null && (
            <Badge variant={prediction.pointsEarned > 0 ? 'default' : 'outline'}>
              {prediction.pointsEarned} pts
            </Badge>
          )}
        </div>
      )}
    </div>
  );
}

function PicksSection({ leagueId, leagueMode }: { leagueId: string; leagueMode: string }) {
  const { data: players, isLoading: playersLoading } = usePlayers(leagueId);
  const { data: standings } = useStandings(leagueId);
  const { data: topScorerPick } = useTopScorerPick(leagueId);
  const { data: leagueWinnerPick } = useLeagueWinnerPick(leagueId);
  const saveTopScorer = useSaveTopScorerPick(leagueId);
  const saveLeagueWinner = useSaveLeagueWinnerPick(leagueId);

  const [topScorerSearch, setTopScorerSearch] = useState('');
  const [topScorerName, setTopScorerName] = useState('');

  const isManual = leagueMode === 'MANUAL';

  const filteredPlayers = players?.filter(
    (p) => p.name.toLowerCase().includes(topScorerSearch.toLowerCase())
  ) ?? [];

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Top Scorer Pick</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {topScorerPick && (
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">Current pick:</span>
              <span className="font-medium">{topScorerPick.playerName}</span>
              {topScorerPick.pointsEarned !== null && (
                <Badge variant={topScorerPick.pointsEarned > 0 ? 'default' : 'outline'}>
                  {topScorerPick.pointsEarned} pts
                </Badge>
              )}
            </div>
          )}
          {isManual ? (
            <div className="flex items-center gap-2">
              <Input
                placeholder="Enter player name..."
                value={topScorerName}
                onChange={(e) => setTopScorerName(e.target.value)}
                aria-label="Top scorer player name"
              />
              <Button
                size="sm"
                disabled={!topScorerName.trim()}
                onClick={() => {
                  saveTopScorer.mutate({ playerName: topScorerName.trim() });
                  setTopScorerName('');
                }}
              >
                Save
              </Button>
            </div>
          ) : (
            <div className="space-y-2">
              <Input
                placeholder="Search players..."
                value={topScorerSearch}
                onChange={(e) => setTopScorerSearch(e.target.value)}
                aria-label="Search players for top scorer pick"
              />
              {playersLoading && <p className="text-sm text-muted-foreground">Loading players...</p>}
              {topScorerSearch.length >= 2 && filteredPlayers.length > 0 && (
                <div className="max-h-48 overflow-y-auto rounded-md border">
                  {filteredPlayers.slice(0, 20).map((player) => (
                    <button
                      key={player.apiPlayerId}
                      className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm hover:bg-muted"
                      onClick={() => {
                        saveTopScorer.mutate({
                          playerName: player.name,
                          apiPlayerId: player.apiPlayerId,
                        });
                        setTopScorerSearch('');
                      }}
                    >
                      {player.photoUrl && <img src={player.photoUrl} alt="" className="h-6 w-6 rounded-full" />}
                      <span>{player.name}</span>
                      {player.position && (
                        <span className="text-xs text-muted-foreground">({player.position})</span>
                      )}
                    </button>
                  ))}
                </div>
              )}
              {topScorerSearch.length >= 2 && filteredPlayers.length === 0 && !playersLoading && (
                <p className="text-sm text-muted-foreground">No players found.</p>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">League Winner Pick</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {leagueWinnerPick && (
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">Current pick:</span>
              <span className="font-medium">{leagueWinnerPick.teamName}</span>
              {leagueWinnerPick.pointsEarned !== null && (
                <Badge variant={leagueWinnerPick.pointsEarned > 0 ? 'default' : 'outline'}>
                  {leagueWinnerPick.pointsEarned} pts
                </Badge>
              )}
            </div>
          )}
          {standings && standings.length > 0 && (
            <div className="max-h-48 overflow-y-auto rounded-md border">
              {standings.map((team) => (
                <button
                  key={team.id}
                  className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm hover:bg-muted"
                  onClick={() => {
                    saveLeagueWinner.mutate({
                      teamName: team.teamName,
                      apiTeamId: team.apiTeamId ?? undefined,
                    });
                  }}
                >
                  {team.teamLogo && <img src={team.teamLogo} alt="" className="h-5 w-5" />}
                  <span>{team.teamName}</span>
                </button>
              ))}
            </div>
          )}
          {(!standings || standings.length === 0) && (
            <p className="text-sm text-muted-foreground">No teams available yet.</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function PredictionsTable({ predictions }: { predictions: PredictionDto[] }) {
  const sorted = [...predictions].sort(
    (a, b) => a.matchday - b.matchday || new Date(a.fixtureKickoff).getTime() - new Date(b.fixtureKickoff).getTime()
  );

  if (sorted.length === 0) {
    return <p className="text-muted-foreground">No predictions yet. Go to Fixtures to make your predictions.</p>;
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>MD</TableHead>
          <TableHead>Match</TableHead>
          <TableHead className="text-center">Score</TableHead>
          <TableHead className="text-center">Your Prediction</TableHead>
          <TableHead className="text-center">Points</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {sorted.map((p) => (
          <TableRow key={p.id}>
            <TableCell>{p.matchday}</TableCell>
            <TableCell>{p.fixtureHomeTeam} vs {p.fixtureAwayTeam}</TableCell>
            <TableCell className="text-center">
              {p.fixtureHomeScore !== null && p.fixtureAwayScore !== null
                ? `${p.fixtureHomeScore} - ${p.fixtureAwayScore}`
                : <span className="text-muted-foreground">-</span>}
            </TableCell>
            <TableCell className="text-center font-medium">{p.homeScore} - {p.awayScore}</TableCell>
            <TableCell className="text-center">
              {p.pointsEarned !== null ? (
                <Badge variant={p.pointsEarned > 0 ? 'default' : 'outline'}>
                  {p.pointsEarned}
                </Badge>
              ) : (
                <span className="text-muted-foreground">-</span>
              )}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}

function LeaderboardTable({ entries, isLoading }: { entries: LeaderboardEntryDto[] | undefined; isLoading: boolean }) {
  if (isLoading) return <p className="text-muted-foreground">Loading leaderboard...</p>;
  if (!entries || entries.length === 0) {
    return <p className="text-muted-foreground">No leaderboard data yet. Points will appear once matches are scored.</p>;
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead className="w-10">#</TableHead>
          <TableHead>Player</TableHead>
          <TableHead className="text-center">Exact</TableHead>
          <TableHead className="text-center">Correct</TableHead>
          <TableHead className="text-center">Top Scorer</TableHead>
          <TableHead className="text-center">Winner</TableHead>
          <TableHead className="text-center">Total</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {entries.map((entry) => (
          <TableRow key={entry.userId}>
            <TableCell className="font-medium">{entry.rank}</TableCell>
            <TableCell>
              <div className="flex items-center gap-2">
                {entry.pictureUrl && (
                  <img src={entry.pictureUrl} alt="" className="h-6 w-6 rounded-full" />
                )}
                <span>{entry.displayName}</span>
              </div>
            </TableCell>
            <TableCell className="text-center">{entry.correctScores}</TableCell>
            <TableCell className="text-center">{entry.correctOutcomes}</TableCell>
            <TableCell className="text-center">
              {entry.topScorerPoints !== null ? entry.topScorerPoints : '-'}
            </TableCell>
            <TableCell className="text-center">
              {entry.leagueWinnerPoints !== null ? entry.leagueWinnerPoints : '-'}
            </TableCell>
            <TableCell className="text-center font-bold">{entry.totalPoints}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}

export function LeagueViewPage() {
  const { id } = useParams<{ id: string }>();
  const { data: league, isLoading: leagueLoading } = useLeague(id!);
  const { data: fixtures, isLoading: fixturesLoading } = useFixtures(id!);
  const { data: standings, isLoading: standingsLoading } = useStandings(id!);
  const { data: predictions } = useMyPredictions(id!);
  const { data: leaderboard, isLoading: leaderboardLoading } = useLeaderboard(id!);
  const savePrediction = useSavePrediction(id!);

  if (leagueLoading) return <p className="text-muted-foreground">Loading...</p>;
  if (!league) return <p>League not found.</p>;

  const matchdays = fixtures ? groupByMatchday(fixtures) : new Map();
  const predictionsByFixture = new Map(predictions?.map((p) => [p.fixtureId, p]));

  function handleSavePrediction(fixtureId: string, homeScore: number, awayScore: number) {
    savePrediction.mutate({ fixtureId, request: { homeScore, awayScore } });
  }

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
          <TabsTrigger value="predictions">My Predictions</TabsTrigger>
          <TabsTrigger value="picks">Picks</TabsTrigger>
          <TabsTrigger value="leaderboard">Leaderboard</TabsTrigger>
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
                    <FixtureCard
                      key={fixture.id}
                      fixture={fixture}
                      prediction={predictionsByFixture.get(fixture.id)}
                      onSavePrediction={handleSavePrediction}
                      isSaving={savePrediction.isPending}
                    />
                  ))}
                </CardContent>
              </Card>
            ))}
        </TabsContent>

        <TabsContent value="predictions">
          <PredictionsTable predictions={predictions ?? []} />
        </TabsContent>

        <TabsContent value="picks">
          <PicksSection leagueId={id!} leagueMode={league.mode} />
        </TabsContent>

        <TabsContent value="leaderboard">
          <LeaderboardTable entries={leaderboard} isLoading={leaderboardLoading} />
        </TabsContent>

        <TabsContent value="standings" className="space-y-4">
          {standingsLoading && <p className="text-muted-foreground">Loading standings...</p>}
          {standings && standings.length === 0 && (
            <p className="text-muted-foreground">No standings yet. Data will appear after sync.</p>
          )}
          {standings && standings.length > 0 && (() => {
            const groups = new Map<string, typeof standings>();
            for (const s of standings) {
              const key = s.groupName ?? '';
              const group = groups.get(key) ?? [];
              group.push(s);
              groups.set(key, group);
            }
            const sortedGroups = [...groups.entries()].sort(([a], [b]) => a.localeCompare(b));

            return sortedGroups.map(([groupName, groupStandings]) => (
              <Card key={groupName || '__all'}>
                {groupName && (
                  <CardHeader className="pb-2">
                    <CardTitle className="text-base">Group {groupName}</CardTitle>
                  </CardHeader>
                )}
                <CardContent className={groupName ? '' : 'pt-6'}>
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
                      {groupStandings.map((s) => (
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
                </CardContent>
              </Card>
            ));
          })()}
        </TabsContent>
      </Tabs>
    </div>
  );
}
