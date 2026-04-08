import { useAdminUsers, useUpdateUser } from '@/hooks/use-admin-users';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { PageSpinner } from '@/components/spinner';
import { ErrorAlert } from '@/components/error-alert';
import { EmptyState } from '@/components/empty-state';
import { useToast } from '@/components/toast';

export function AdminUsersPage() {
  const { data: users, isLoading, error, refetch } = useAdminUsers();
  const updateMutation = useUpdateUser();
  const { toast } = useToast();

  function handleToggleActive(id: string, currentlyActive: boolean) {
    updateMutation.mutate(
      { id, request: { isActive: !currentlyActive } },
      {
        onSuccess: () => toast('success', `User ${currentlyActive ? 'deactivated' : 'activated'}`),
        onError: () => toast('error', 'Failed to update user'),
      },
    );
  }

  if (isLoading) return <PageSpinner message="Loading users..." />;
  if (error) return <ErrorAlert message="Failed to load users." onRetry={() => refetch()} />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold tracking-tight">User Management</h1>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">
            Registered Users ({users?.length ?? 0})
          </CardTitle>
        </CardHeader>
        <CardContent>
          {users && users.length === 0 && (
            <EmptyState message="No users registered yet." />
          )}
          {users && users.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Joined</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        {user.pictureUrl && (
                          <img src={user.pictureUrl} alt="" className="h-6 w-6 rounded-full" />
                        )}
                        <span>{user.displayName}</span>
                      </div>
                    </TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>
                      <Badge variant={user.role === 'ADMIN' ? 'default' : 'secondary'}>
                        {user.role}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge variant={user.isActive ? 'default' : 'destructive'}>
                        {user.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {new Date(user.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        size="sm"
                        variant={user.isActive ? 'destructive' : 'default'}
                        disabled={user.role === 'ADMIN' || updateMutation.isPending}
                        onClick={() => handleToggleActive(user.id, user.isActive)}
                      >
                        {user.isActive ? 'Deactivate' : 'Activate'}
                      </Button>
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
