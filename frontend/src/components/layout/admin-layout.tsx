import { Outlet } from 'react-router-dom';
import { Navbar } from './navbar';
import { Sidebar } from './sidebar';

export function AdminLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <div className="flex flex-1">
        <Sidebar />
        <main className="flex-1 p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
