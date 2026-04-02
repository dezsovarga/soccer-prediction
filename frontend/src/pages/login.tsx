import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/use-auth';
import { getLoginUrl } from '@/lib/api';
import { buttonVariants } from '@/components/ui/button';
import { cn } from '@/lib/utils';

export function LoginPage() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-muted-foreground">Loading...</div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <div className="mx-4 w-full max-w-sm space-y-6 text-center">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Soccer Predictions</h1>
          <p className="text-muted-foreground">
            Predict match scores and compete with your friends.
          </p>
        </div>
        <a
          href={getLoginUrl()}
          className={cn(buttonVariants({ size: 'lg' }), 'w-full')}
        >
          Sign in with Google
        </a>
      </div>
    </div>
  );
}
