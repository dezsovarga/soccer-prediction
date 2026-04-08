import { Navigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '@/hooks/use-auth';
import { getLoginUrl } from '@/lib/api';
import { buttonVariants } from '@/components/ui/button';
import { FullPageSpinner } from '@/components/spinner';
import { cn } from '@/lib/utils';

const errorMessages: Record<string, string> = {
  account_deactivated: 'Your account has been deactivated. Please contact an administrator.',
  auth_failed: 'Authentication failed. Please try again.',
};

export function LoginPage() {
  const { isAuthenticated, isLoading } = useAuth();
  const [searchParams] = useSearchParams();
  const error = searchParams.get('error');

  if (isLoading) {
    return <FullPageSpinner />;
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
        {error && (
          <p className="text-sm text-destructive">
            {errorMessages[error] ?? errorMessages.auth_failed}
          </p>
        )}
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
