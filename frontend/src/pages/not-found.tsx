import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="space-y-4 text-center">
        <h1 className="text-4xl font-bold">404</h1>
        <p className="text-muted-foreground">Page not found.</p>
        <Button render={<Link to="/" />}>Go home</Button>
      </div>
    </div>
  );
}
