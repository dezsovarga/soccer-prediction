import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useJoinLeague } from '@/hooks/use-leagues';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ApiError } from '@/lib/api';

export function JoinLeaguePage() {
  const [joinCode, setJoinCode] = useState('');
  const navigate = useNavigate();
  const joinMutation = useJoinLeague();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!joinCode.trim()) return;

    joinMutation.mutate(joinCode.trim(), {
      onSuccess: (league) => {
        navigate(`/leagues/${league.id}`);
      },
    });
  };

  const errorMessage = joinMutation.error instanceof ApiError && joinMutation.error.status === 404
    ? 'Invalid join code. Please check and try again.'
    : joinMutation.error
      ? 'Something went wrong. Please try again.'
      : null;

  return (
    <div className="mx-auto max-w-md">
      <Card>
        <CardHeader>
          <CardTitle>Join a League</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="joinCode">Join Code</Label>
              <Input
                id="joinCode"
                placeholder="Enter the league join code"
                value={joinCode}
                onChange={(e) => setJoinCode(e.target.value)}
                disabled={joinMutation.isPending}
              />
            </div>
            {errorMessage && (
              <p className="text-sm text-destructive">{errorMessage}</p>
            )}
            <Button type="submit" className="w-full" disabled={joinMutation.isPending || !joinCode.trim()}>
              {joinMutation.isPending ? 'Joining...' : 'Join League'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
