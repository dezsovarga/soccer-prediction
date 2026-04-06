import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAdminLeagues, useSearchApiFootballLeagues, useCreateLeague } from '@/hooks/use-admin-leagues';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import type { ApiFootballLeague, LeagueMode } from '@/lib/types';

export function AdminLeaguesPage() {
  const { data: leagues, isLoading } = useAdminLeagues();
  const [mode, setMode] = useState<LeagueMode>('MANUAL');
  const [searchQuery, setSearchQuery] = useState('');
  const { data: searchResults, isLoading: searching } = useSearchApiFootballLeagues(searchQuery);
  const createMutation = useCreateLeague();
  const [selectedLeague, setSelectedLeague] = useState<ApiFootballLeague | null>(null);
  const [selectedSeason, setSelectedSeason] = useState<number | null>(null);
  const [leagueName, setLeagueName] = useState('');
  const [manualSeason, setManualSeason] = useState(new Date().getFullYear().toString());

  const handleSelect = (league: ApiFootballLeague) => {
    setSelectedLeague(league);
    setLeagueName(league.name);
    const latestSeason = Math.max(...league.seasons);
    setSelectedSeason(latestSeason);
  };

  const handleCreateManual = () => {
    if (!leagueName.trim() || !manualSeason) return;
    createMutation.mutate(
      {
        name: leagueName.trim(),
        mode: 'MANUAL',
        season: parseInt(manualSeason, 10),
      },
      {
        onSuccess: () => {
          setLeagueName('');
          setManualSeason(new Date().getFullYear().toString());
        },
      }
    );
  };

  const handleCreateApiSynced = () => {
    if (!selectedLeague || !selectedSeason || !leagueName.trim()) return;
    createMutation.mutate(
      {
        name: leagueName.trim(),
        mode: 'API_SYNCED',
        apiLeagueId: selectedLeague.leagueId,
        season: selectedSeason,
      },
      {
        onSuccess: () => {
          setSelectedLeague(null);
          setSelectedSeason(null);
          setLeagueName('');
          setSearchQuery('');
        },
      }
    );
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold tracking-tight">League Management</h1>

      <Card>
        <CardHeader>
          <CardTitle>Create League</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex gap-2">
            <Button
              variant={mode === 'MANUAL' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setMode('MANUAL')}
            >
              Manual
            </Button>
            <Button
              variant={mode === 'API_SYNCED' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setMode('API_SYNCED')}
            >
              API-Synced
            </Button>
          </div>

          {mode === 'MANUAL' && (
            <div className="space-y-4 rounded-md border p-4">
              <div className="space-y-2">
                <Label htmlFor="manual-name">League Name</Label>
                <Input
                  id="manual-name"
                  placeholder="e.g. World Cup 2026"
                  value={leagueName}
                  onChange={(e) => setLeagueName(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="manual-season">Season / Year</Label>
                <Input
                  id="manual-season"
                  type="number"
                  value={manualSeason}
                  onChange={(e) => setManualSeason(e.target.value)}
                />
              </div>
              <Button
                onClick={handleCreateManual}
                disabled={createMutation.isPending || !leagueName.trim()}
                className="w-full"
              >
                {createMutation.isPending ? 'Creating...' : 'Create Manual League'}
              </Button>
            </div>
          )}

          {mode === 'API_SYNCED' && (
            <>
              <div className="space-y-2">
                <Label htmlFor="search">Search Football Leagues</Label>
                <Input
                  id="search"
                  placeholder="e.g. Premier League, La Liga..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>

              {searching && <p className="text-sm text-muted-foreground">Searching...</p>}

              {searchResults && searchResults.length > 0 && !selectedLeague && (
                <div className="max-h-60 overflow-y-auto rounded-md border">
                  {searchResults.map((league) => (
                    <button
                      key={league.leagueId}
                      onClick={() => handleSelect(league)}
                      className="flex w-full items-center gap-3 border-b px-4 py-3 text-left hover:bg-accent last:border-b-0"
                    >
                      {league.logo && <img src={league.logo} alt="" className="h-6 w-6" />}
                      <div>
                        <p className="font-medium">{league.name}</p>
                        <p className="text-sm text-muted-foreground">{league.country}</p>
                      </div>
                    </button>
                  ))}
                </div>
              )}

              {selectedLeague && (
                <div className="space-y-4 rounded-md border p-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      {selectedLeague.logo && <img src={selectedLeague.logo} alt="" className="h-6 w-6" />}
                      <span className="font-medium">{selectedLeague.name}</span>
                      <span className="text-sm text-muted-foreground">({selectedLeague.country})</span>
                    </div>
                    <Button variant="ghost" size="sm" onClick={() => setSelectedLeague(null)}>
                      Change
                    </Button>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="name">League Name</Label>
                    <Input
                      id="name"
                      value={leagueName}
                      onChange={(e) => setLeagueName(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label>Season</Label>
                    <div className="flex flex-wrap gap-2">
                      {selectedLeague.seasons
                        .sort((a, b) => b - a)
                        .slice(0, 5)
                        .map((season) => (
                          <Button
                            key={season}
                            variant={selectedSeason === season ? 'default' : 'outline'}
                            size="sm"
                            onClick={() => setSelectedSeason(season)}
                          >
                            {season}
                          </Button>
                        ))}
                    </div>
                  </div>

                  <Button
                    onClick={handleCreateApiSynced}
                    disabled={createMutation.isPending || !leagueName.trim()}
                    className="w-full"
                  >
                    {createMutation.isPending ? 'Creating...' : 'Create League'}
                  </Button>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Existing Leagues</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading && <p className="text-muted-foreground">Loading...</p>}
          {leagues && leagues.length === 0 && (
            <p className="text-muted-foreground">No leagues created yet.</p>
          )}
          {leagues && leagues.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Mode</TableHead>
                  <TableHead>Season</TableHead>
                  <TableHead>Join Code</TableHead>
                  <TableHead>Members</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {leagues.map((league) => (
                  <TableRow key={league.id}>
                    <TableCell className="font-medium">{league.name}</TableCell>
                    <TableCell>
                      <Badge variant={league.mode === 'MANUAL' ? 'default' : 'secondary'}>
                        {league.mode === 'MANUAL' ? 'Manual' : 'API'}
                      </Badge>
                    </TableCell>
                    <TableCell><Badge variant="secondary">{league.season}</Badge></TableCell>
                    <TableCell>
                      <code className="rounded bg-muted px-2 py-1 text-sm">{league.joinCode}</code>
                    </TableCell>
                    <TableCell>{league.memberCount}</TableCell>
                    <TableCell>
                      {league.mode === 'MANUAL' && (
                        <div className="flex gap-2">
                          <Link to={`/admin/leagues/${league.id}/teams`}>
                            <Button variant="outline" size="sm">Teams</Button>
                          </Link>
                          <Link to={`/admin/leagues/${league.id}/fixtures`}>
                            <Button variant="outline" size="sm">Fixtures</Button>
                          </Link>
                        </div>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
