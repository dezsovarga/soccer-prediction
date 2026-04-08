import { useState, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTeams, useCreateTeam, useDeleteTeam } from '@/hooks/use-admin-teams';
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
import { FIFA_TEAMS } from '@/lib/fifa-teams';

export function AdminTeamsPage() {
  const { id } = useParams<{ id: string }>();
  const { data: teams, isLoading, error, refetch } = useTeams(id!);
  const createTeam = useCreateTeam(id!);
  const deleteTeam = useDeleteTeam(id!);
  const { toast } = useToast();

  const [name, setName] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [groupName, setGroupName] = useState('');
  const [showSuggestions, setShowSuggestions] = useState(false);
  const nameInputRef = useRef<HTMLInputElement>(null);

  const suggestions = name.length >= 1
    ? FIFA_TEAMS.filter((t) => t.name.toLowerCase().includes(name.toLowerCase())).slice(0, 10)
    : [];

  function selectTeam(teamName: string, teamCode: string) {
    setName(teamName);
    setCountryCode(teamCode);
    setShowSuggestions(false);
  }

  const handleCreate = () => {
    if (!name.trim()) return;
    createTeam.mutate(
      {
        name: name.trim(),
        countryCode: countryCode.trim() || undefined,
        groupName: groupName.trim() || undefined,
      },
      {
        onSuccess: () => {
          setName('');
          setCountryCode('');
          setGroupName('');
          toast('success', 'Team added');
        },
        onError: () => toast('error', 'Failed to add team'),
      }
    );
  };

  const handleDelete = (teamId: string, teamName: string) => {
    deleteTeam.mutate(teamId, {
      onSuccess: () => toast('success', `${teamName} deleted`),
      onError: () => toast('error', `Failed to delete ${teamName}`),
    });
  };

  const groups = teams
    ? [...new Set(teams.map((t) => t.groupName).filter(Boolean))].sort()
    : [];

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link to="/admin/leagues" className={buttonVariants({ variant: 'ghost', size: 'sm' })}>
          &larr; Leagues
        </Link>
        <h1 className="text-2xl font-bold tracking-tight">Team Management</h1>
        <Link to={`/admin/leagues/${id}/fixtures`} className={buttonVariants({ variant: 'outline', size: 'sm' })}>
          Fixtures
        </Link>
      </div>

      <Card className="overflow-visible">
        <CardHeader>
          <CardTitle>Add Team</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-end gap-3">
            <div className="relative flex-1 space-y-1">
              <Label htmlFor="team-name">Team Name</Label>
              <Input
                ref={nameInputRef}
                id="team-name"
                placeholder="e.g. Brazil"
                autoComplete="off"
                value={name}
                onChange={(e) => {
                  setName(e.target.value);
                  setCountryCode('');
                  setShowSuggestions(true);
                }}
                onFocus={() => setShowSuggestions(true)}
                onBlur={() => {
                  // Delay to allow click on suggestion
                  setTimeout(() => setShowSuggestions(false), 150);
                }}
              />
              {showSuggestions && suggestions.length > 0 && (
                <div className="absolute z-10 mt-1 max-h-56 w-full overflow-y-auto rounded-md border bg-popover shadow-lg">
                  {suggestions.map((team) => (
                    <button
                      key={team.code}
                      type="button"
                      className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm hover:bg-muted"
                      onMouseDown={(e) => e.preventDefault()}
                      onClick={() => selectTeam(team.name, team.code)}
                    >
                      <img
                        src={`https://flagcdn.com/w40/${team.code}.png`}
                        alt=""
                        className="h-4 w-6 object-contain"
                      />
                      <span>{team.name}</span>
                      <span className="ml-auto text-xs text-muted-foreground">{team.code}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
            <div className="w-24 space-y-1">
              <Label htmlFor="country-code">Code</Label>
              <Input
                id="country-code"
                placeholder="br"
                value={countryCode}
                onChange={(e) => setCountryCode(e.target.value)}
              />
            </div>
            <div className="w-24 space-y-1">
              <Label htmlFor="group">Group</Label>
              <Input
                id="group"
                placeholder="A"
                value={groupName}
                onChange={(e) => setGroupName(e.target.value)}
              />
            </div>
            <Button onClick={handleCreate} disabled={createTeam.isPending || !name.trim()}>
              {createTeam.isPending ? 'Adding...' : 'Add'}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Teams {teams ? `(${teams.length})` : ''}</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading && <PageSpinner message="Loading teams..." />}
          {error && <ErrorAlert message="Failed to load teams." onRetry={() => refetch()} />}
          {!isLoading && !error && teams && teams.length === 0 && (
            <EmptyState message="No teams added yet." />
          )}
          {teams && teams.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-12">Flag</TableHead>
                  <TableHead>Name</TableHead>
                  <TableHead>Code</TableHead>
                  <TableHead>Group</TableHead>
                  <TableHead className="w-20">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {teams.map((team) => (
                  <TableRow key={team.id}>
                    <TableCell>
                      {team.logoUrl && <img src={team.logoUrl} alt="" className="h-6 w-9 object-contain" />}
                    </TableCell>
                    <TableCell className="font-medium">{team.name}</TableCell>
                    <TableCell>
                      {team.countryCode && (
                        <code className="rounded bg-muted px-1.5 py-0.5 text-xs">{team.countryCode}</code>
                      )}
                    </TableCell>
                    <TableCell>
                      {team.groupName && <Badge variant="outline">Group {team.groupName}</Badge>}
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="destructive"
                        size="xs"
                        onClick={() => handleDelete(team.id, team.name)}
                        disabled={deleteTeam.isPending}
                      >
                        Delete
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}

          {groups.length > 0 && (
            <div className="mt-4 flex gap-2">
              <span className="text-sm text-muted-foreground">Groups:</span>
              {groups.map((g) => (
                <Badge key={g} variant="secondary">
                  {g} ({teams?.filter((t) => t.groupName === g).length})
                </Badge>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
