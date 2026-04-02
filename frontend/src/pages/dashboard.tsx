import { useAuth } from '@/hooks/use-auth';

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold tracking-tight">
        Welcome, {user?.displayName}
      </h1>
      <p className="text-muted-foreground">
        Your leagues and predictions will appear here.
      </p>
    </div>
  );
}
