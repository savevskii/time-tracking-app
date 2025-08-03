import React from 'react';
import { Link } from 'react-router-dom';
import useKeycloak from '../hooks/useKeycloak';

interface NavBarProps {}

const NavBar: React.FC<NavBarProps> = () => {
    const { keycloak, authenticated } = useKeycloak();

    const handleLogin = () => {
        keycloak?.login();
    };

    const handleLogout = () => {
        keycloak?.logout();
    };

    return (
        <header className="fixed top-0 left-0 w-full bg-blue-600 text-white shadow z-50">
            <nav className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
                <Link to="/" className="text-xl font-semibold hover:opacity-90">
                    Keycloak POC
                </Link>

                <div className="flex items-center gap-4">
                    {authenticated ? (
                        <>
                            <Link
                                to="/my-account"
                                className="hover:bg-blue-700 px-3 py-1 rounded transition"
                            >
                                My Account
                            </Link>
                            <Link
                                to="/my-items"
                                className="hover:bg-blue-700 px-3 py-1 rounded transition"
                            >
                                My Items
                            </Link>
                            <button
                                onClick={handleLogout}
                                className="hover:bg-blue-700 px-3 py-1 rounded transition"
                            >
                                Logout
                            </button>
                        </>
                    ) : (
                        <button
                            onClick={handleLogin}
                            className="hover:bg-blue-700 px-3 py-1 rounded transition"
                        >
                            Login
                        </button>
                    )}
                </div>
            </nav>
        </header>
    );
};

export default NavBar;