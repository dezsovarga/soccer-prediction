import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Navbar } from './navbar';
import { Sidebar } from './sidebar';

export function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Close sidebar on navigation
  const handleNavigate = () => setSidebarOpen(false);

  return (
    <div className="flex min-h-screen flex-col">
      <Navbar onMenuToggle={() => setSidebarOpen((o) => !o)} showMenuButton />
      <div className="flex flex-1">
        {/* Mobile overlay */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 z-30 bg-black/40 md:hidden"
            onClick={() => setSidebarOpen(false)}
          />
        )}
        <Sidebar open={sidebarOpen} onNavigate={handleNavigate} />
        <main className="flex-1 overflow-x-hidden p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
