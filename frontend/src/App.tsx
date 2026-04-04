import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '@/components/protected-route';
import { AdminRoute } from '@/components/admin-route';
import { Layout } from '@/components/layout/layout';
import { AdminLayout } from '@/components/layout/admin-layout';
import { LoginPage } from '@/pages/login';
import { DashboardPage } from '@/pages/dashboard';
import { JoinLeaguePage } from '@/pages/join-league';
import { LeagueViewPage } from '@/pages/league-view';
import { AdminLeaguesPage } from '@/pages/admin-leagues';
import { NotFoundPage } from '@/pages/not-found';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/join" element={<JoinLeaguePage />} />
            <Route path="/leagues/:id" element={<LeagueViewPage />} />
          </Route>
        </Route>

        <Route element={<AdminRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin" element={<div>Admin Dashboard</div>} />
            <Route path="/admin/users" element={<div>User Management</div>} />
            <Route path="/admin/leagues" element={<AdminLeaguesPage />} />
          </Route>
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
