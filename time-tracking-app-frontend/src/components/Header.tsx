import { Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

export default function Header() {
    const { logout } = useAuth();

    return (
        <header className="flex items-center justify-between bg-blue-700 text-white px-6 py-4">
            <h1 className="text-xl font-bold">SolidTime</h1>
            <nav className="space-x-4">
                <Link to="/" className="hover:underline">
                    Home
                </Link>
                <Link to="/dashboard" className="hover:underline">
                    Dashboard
                </Link>
                <Link to="/projects" className="hover:underline">
                    Projects
                </Link>
                <button
                    onClick={logout}
                    className="ml-4 bg-blue-500 px-3 py-1 rounded hover:bg-blue-600"
                >
                    Logout
                </button>
            </nav>
        </header>
    );
}
