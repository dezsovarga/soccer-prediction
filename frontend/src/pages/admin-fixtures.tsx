import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTeams } from '@/hooks/use-admin-teams';
import { useAdminFixtures, useCreateFixture, useDeleteFixture, useEnterResult } from '@/hooks/use-admin-fixtures';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button, buttonVariants } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { PageSpinner } from '@/components/spinner';
import { ErrorAlert } from '@/components/error-alert';
import { EmptyState } from '@/components/empty-state';
import { useToast } from '@/components/toast';

export function AdminFixturesPage() {
  const { id } = useParams<{ id: string }>();
  const { data: teams } = useTeams(id!);
  const { data: fixtures, isLoading, error, refetch } = useAdminFixtures(id!);
  const createFixture = useCreateFixture(id!);
  const deleteFixture = useDeleteFixture(id!);
  const enterResultMutation = useEnterResult(id!);
  const { toast } = useToast();

  const [homeTeamId, setHomeTeamId] = useState('');
  const [awayTeamId, setAwayTeamId] = useState('');
  const [kickoff, setKickoff] = useState('');
  const [round, setRound] = useState('');
  const [matchday, setMatchday] = useState('1');

  // Result entry state
  const [resultFixtureId, setResultFixtureId] = useState<string | null>(null);
  const [homeScore, setHomeScore] = useState('');
  const [awayScore, setAwayScore] = useState('');

  const handleCreate = () => {
    if (!homeTeamId || !awayTeamId || !kickoff) return;
    createFixture.mutate(
      {
        homeTeamId,
        awayTeamId,
        kickoff: new Date(kickoff).toISOString(),
        round: round.trim() || undefined,
        matchday: parseInt(matchday, 10) || 1,
      },
      {
        onSuccess: () => {
          setHomeTeamId('');
          setAwayTeamId('');
          setKickoff('');
          setRound('');
          toast('success', 'Fixture created');
        },
        onError: () => toast('error', 'Failed to create fixture'),
      }
    );
  };

  const handleEnterResult = (fixtureId: string) => {
    const h = parseInt(homeScore, 10);
    const a = parseInt(awayScore, 10);
    if (isNaN(h) || isNaN(a) || h < 0 || a < 0) return;
    enterResultMutation.mutate(
      { fixtureId, request: { homeScore: h, awayScore: a } },
      {
        onSuccess: () => {
          setResultFixtureId(null);
          setHomeScore('');
          setAwayScore('');
          toast('success', 'Result entered');
        },
        onError: () => toast('error', 'Failed to enter result'),
      }
    );
  };

  const handleDelete = (fixtureId: string) => {
    deleteFixture.mutate(fixtureId, {
      onSuccess: () => toast('success', 'Fixture deleted'),
      onError: () => toast('error', 'Failed to delete fixture'),
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-3">
        <Link to="/admin/leagues" className={buttonVariants({ variant: 'ghost', size: 'sm' })}>
          &larr; Leagues
        </Link>
        <h1 className="text-2xl font-bold tracking-tight">Fixture Management</h1>
        <Link to={`/admin/leagues/${id}/teams`} className={buttonVariants({ variant: 'outline', size: 'sm' })}>
          Teams
        </Link>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Create Fixture</CardTitle>
        </CardHeader>
        <CardContent>
          {(!teams || teams.length < 2) && (
            <p className="text-muted-foreground">Add at least 2 teams before creating fixtures.</p>
          )}
          {teams && teams.length >= 2 && (
            <div className="space-y-3">
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="space-y-1">
                  <Label>Home Team</Label>
                  <select
                    className="h-8 w-full rounded-lg border border-input bg-transparent px-2.5 py-1 text-sm"
                    value={homeTeamId}
                    onChange={(e) => setHomeTeamId(e.target.value)}
                  >
                    <option value="">Select...</option>
                    {teams.map((t) => (
                      <option key={t.id} value={t.id}>{t.name}</option>
                    ))}
                  </select>
                </div>
                <div className="space-y-1">
                  <Label>Away Team</Label>
                  <select
                    className="h-8 w-full rounded-lg border border-input bg-transparent px-2.5 py-1 text-sm"
                    value={awayTeamId}
                    onChange={(e) => setAwayTeamId(e.target.value)}
                  >
                    <option value="">Select...</option>
                    {teams.map((t) => (
                      <option key={t.id} value={t.id}>{t.name}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="flex flex-col gap-3 md:flex-row md:items-end">
                <div className="flex-1 space-y-1">
                  <Label>Kickoff</Label>
                  <Input
                    type="datetime-local"
                    value={kickoff}
                    onChange={(e) => setKickoff(e.target.value)}
                  />
                </div>
                <div className="flex items-end gap-3">
                  <div className="w-32 space-y-1">
                    <Label>Round</Label>
                    <Input
                      placeholder="Group A"
                      value={round}
                      onChange={(e) => setRound(e.target.value)}
                    />
                  </div>
                  <div className="w-24 space-y-1">
                    <Label>Matchday</Label>
                    <Input
                      type="number"
                      min={1}
                      value={matchday}
                      onChange={(e) => setMatchday(e.target.value)}
                    />
                  </div>
                  <Button
                    onClick={handleCreate}
                    disabled={createFixture.isPending || !homeTeamId || !awayTeamId || !kickoff}
                  >
                    {createFixture.isPending ? 'Creating...' : 'Create'}
                  </Button>
                </div>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Fixtures {fixtures ? `(${fixtures.length})` : ''}</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading && <PageSpinner message="Loading fixtures..." />}
          {error && <ErrorAlert message="Failed to load fixtures." onRetry={() => refetch()} />}
          {!isLoading && !error && fixtures && fixtures.length === 0 && (
            <EmptyState message="No fixtures created yet." />
          )}
          {fixtures && fixtures.length > 0 && (
            <>
              {/* Mobile card list */}
              <div className="space-y-3 md:hidden">
                {fixtures.map((fixture) => (
                  <div key={fixture.id} className="rounded-lg border p-3">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-1">
                        {fixture.homeTeamLogo && <img src={fixture.homeTeamLogo} alt="" className="h-4 w-6 object-contain" />}
                        <span className="font-medium">{fixture.homeTeam}</span>
                        <span className="text-muted-foreground">vs</span>
                        <span className="font-medium">{fixture.awayTeam}</span>
                        {fixture.awayTeamLogo && <img src={fixture.awayTeamLogo} alt="" className="h-4 w-6 object-contain" />}
                      </div>
                      {fixture.homeScore !== null && fixture.awayScore !== null
                        ? <span className="font-bold">{fixture.homeScore} - {fixture.awayScore}</span>
                        : <Badge variant={fixture.status === 'FINISHED' ? 'secondary' : 'outline'}>{fixture.status}</Badge>}
                    </div>
                    <div className="mt-1.5 flex items-center gap-2 text-xs text-muted-foreground">
                      <span>
                        {new Date(fixture.kickoff).toLocaleDateString(undefined, {
                          month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
                        })}
                      </span>
                      {fixture.round && <Badge variant="outline" className="text-xs">{fixture.round}</Badge>}
                    </div>
                    {resultFixtureId === fixture.id ? (
                      <div className="mt-2 flex items-center gap-1">
                        <Input
                          type="number"
                          min={0}
                          className="w-14 text-center"
                          placeholder="H"
                          value={homeScore}
                          onChange={(e) => setHomeScore(e.target.value)}
                        />
                        <span>-</span>
                        <Input
                          type="number"
                          min={0}
                          className="w-14 text-center"
                          placeholder="A"
                          value={awayScore}
                          onChange={(e) => setAwayScore(e.target.value)}
                        />
                        <Button
                          size="sm"
                          onClick={() => handleEnterResult(fixture.id)}
                          disabled={enterResultMutation.isPending}
                        >
                          Save
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => setResultFixtureId(null)}
                        >
                          Cancel
                        </Button>
                      </div>
                    ) : (
                      <div className="mt-2 flex gap-2">
                        {fixture.status !== 'FINISHED' && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => {
                              setResultFixtureId(fixture.id);
                              setHomeScore('');
                              setAwayScore('');
                            }}
                          >
                            Enter Result
                          </Button>
                        )}
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleDelete(fixture.id)}
                          disabled={deleteFixture.isPending}
                        >
                          Delete
                        </Button>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              {/* Desktop table */}
              <div className="hidden md:block">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Match</TableHead>
                      <TableHead>Kickoff</TableHead>
                      <TableHead>Round</TableHead>
                      <TableHead className="text-center">Score</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {fixtures.map((fixture) => (
                      <TableRow key={fixture.id}>
                        <TableCell>
                          <div className="flex items-center gap-1">
                            {fixture.homeTeamLogo && <img src={fixture.homeTeamLogo} alt="" className="h-4 w-6 object-contain" />}
                            <span className="font-medium">{fixture.homeTeam}</span>
                            <span className="text-muted-foreground">vs</span>
                            <span className="font-medium">{fixture.awayTeam}</span>
                            {fixture.awayTeamLogo && <img src={fixture.awayTeamLogo} alt="" className="h-4 w-6 object-contain" />}
                          </div>
                        </TableCell>
                        <TableCell className="text-sm">
                          {new Date(fixture.kickoff).toLocaleDateString(undefined, {
                            month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
                          })}
                        </TableCell>
                        <TableCell>{fixture.round && <Badge variant="outline">{fixture.round}</Badge>}</TableCell>
                        <TableCell className="text-center">
                          {fixture.homeScore !== null && fixture.awayScore !== null
                            ? <span className="font-bold">{fixture.homeScore} - {fixture.awayScore}</span>
                            : <span className="text-muted-foreground">-</span>}
                        </TableCell>
                        <TableCell>
                          <Badge variant={fixture.status === 'FINISHED' ? 'secondary' : 'outline'}>
                            {fixture.status}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            {fixture.status !== 'FINISHED' && (
                              <>
                                {resultFixtureId === fixture.id ? (
                                  <div className="flex items-center gap-1">
                                    <Input
                                      type="number"
                                      min={0}
                                      className="w-12 text-center"
                                      placeholder="H"
                                      value={homeScore}
                                      onChange={(e) => setHomeScore(e.target.value)}
                                    />
                                    <span>-</span>
                                    <Input
                                      type="number"
                                      min={0}
                                      className="w-12 text-center"
                                      placeholder="A"
                                      value={awayScore}
                                      onChange={(e) => setAwayScore(e.target.value)}
                                    />
                                    <Button
                                      size="xs"
                                      onClick={() => handleEnterResult(fixture.id)}
                                      disabled={enterResultMutation.isPending}
                                    >
                                      Save
                                    </Button>
                                    <Button
                                      size="xs"
                                      variant="ghost"
                                      onClick={() => setResultFixtureId(null)}
                                    >
                                      Cancel
                                    </Button>
                                  </div>
                                ) : (
                                  <Button
                                    variant="outline"
                                    size="xs"
                                    onClick={() => {
                                      setResultFixtureId(fixture.id);
                                      setHomeScore('');
                                      setAwayScore('');
                                    }}
                                  >
                                    Enter Result
                                  </Button>
                                )}
                              </>
                            )}
                            <Button
                              variant="destructive"
                              size="xs"
                              onClick={() => handleDelete(fixture.id)}
                              disabled={deleteFixture.isPending}
                            >
                              Delete
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
