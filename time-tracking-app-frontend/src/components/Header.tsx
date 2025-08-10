import { Link, useLocation } from 'react-router-dom';
import useAuth from '@/hooks/useAuth';

export default function Header() {
    const { logout } = useAuth();
    const { pathname } = useLocation();

    const linkBase =
        'hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/70 rounded px-1';
    const isActive = (to: string) => (pathname === to ? 'underline underline-offset-4' : '');

    return (
        <header className="flex items-center justify-between bg-blue-700 text-white px-6 py-4">
            <h1 className="text-xl font-bold">TrackLight</h1>
            <nav className="space-x-4">
                <Link to="/" className={`${linkBase} ${isActive('/')}`}>Home</Link>
                <Link to="/projects" className={`${linkBase} ${isActive('/projects')}`}>Projects</Link>
                <Link to="/entries" className={`${linkBase} ${isActive('/entries')}`}>Entries</Link>
                <Link to="/dashboard" className={`${linkBase} ${isActive('/dashboard')}`}>Dashboard</Link>
                <button
                    onClick={logout}
                    className="ml-4 bg-blue-500 px-3 py-1 rounded hover:bg-blue-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/70"
                >
                    Logout
                </button>
            </nav>
        </header>
    );
}