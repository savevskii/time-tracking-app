import { Link } from 'react-router-dom';
import useAuth from "@/hooks/useAuth";

export default function Header() {
    const { logout, isAuthenticated, isAdmin } = useAuth();

    return (
        <header className="flex items-center justify-between bg-blue-700 text-white px-6 py-4">
            <Link to="/" className="text-xl font-bold">
                TrackLight
            </Link>
            <nav className="space-x-4">
                <Link to="/" className="hover:underline">Home</Link>

                {isAuthenticated && (
                    <Link to="/time-entries" className="hover:underline">
                        Time Entries
                    </Link>
                )}

                {isAuthenticated && isAdmin && (
                    <>
                        <Link to="/dashboard" className="hover:underline">Dashboard</Link>
                        <Link to="/projects" className="hover:underline">Projects</Link>
                    </>
                )}

                {isAuthenticated && (
                    <button
                        onClick={logout}
                        className="ml-4 bg-blue-500 px-3 py-1 rounded hover:bg-blue-600"
                    >
                        Logout
                    </button>
                )}
            </nav>
        </header>
    );
}
