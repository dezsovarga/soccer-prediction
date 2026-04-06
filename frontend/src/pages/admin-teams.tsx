import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTeams, useCreateTeam, useDeleteTeam } from '@/hooks/use-admin-teams';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button, buttonVariants } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';

export function AdminTeamsPage() {
  const { id } = useParams<{ id: string }>();
  const { data: teams, isLoading } = useTeams(id!);
  const createTeam = useCreateTeam(id!);
  const deleteTeam = useDeleteTeam(id!);

  const [name, setName] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [groupName, setGroupName] = useState('');

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
        },
      }
    );
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

      <Card>
        <CardHeader>
          <CardTitle>Add Team</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-end gap-3">
            <div className="flex-1 space-y-1">
              <Label htmlFor="team-name">Team Name</Label>
              <Input
                id="team-name"
                placeholder="e.g. Brazil"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div className="w-24 space-y-1">
              <Label htmlFor="country-code">Code</Label>
              <Input
                id="country-code"
                placeholder="br"
                maxLength={2}
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
          {isLoading && <p className="text-muted-foreground">Loading...</p>}
          {teams && teams.length === 0 && (
            <p className="text-muted-foreground">No teams added yet.</p>
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
                        onClick={() => deleteTeam.mutate(team.id)}
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
