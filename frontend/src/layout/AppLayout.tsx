import { Outlet } from 'react-router'

export function AppLayout() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <strong>MeetPick</strong>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
