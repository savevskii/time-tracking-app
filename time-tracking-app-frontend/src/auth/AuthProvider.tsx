import { createContext, useEffect, useRef, useState, type ReactNode } from 'react';
import keycloak from './keycloak';

interface AuthContextType {
    isAuthenticated: boolean;
    token: string | undefined;
    logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [token, setToken] = useState<string | undefined>(undefined);
    const hasInitialized = useRef(false);

    useEffect(() => {
        if (!hasInitialized.current) {
            hasInitialized.current = true;

            keycloak
                .init({ onLoad: 'login-required' })
                .then((authenticated) => {
                    if (authenticated) {
                        setIsAuthenticated(true);
                        setToken(keycloak.token);
                    }
                })
                .catch((err) => {
                    console.error('Keycloak init failed', err);
                });
        }
    }, []);

    const logout = () => keycloak.logout();

    return (
        <AuthContext.Provider value={{ isAuthenticated, token, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
