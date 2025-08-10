import { createContext, useEffect, useRef, useState, type ReactNode } from 'react';
import keycloak from './keycloak';

interface AuthContextType {
    isAuthenticated: boolean;
    token?: string;
    logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [token, setToken] = useState<string | undefined>(undefined);
    const didInit = useRef(false);

    useEffect(() => {
        if (didInit.current) return;
        didInit.current = true;

        keycloak
            .init({ onLoad: 'login-required' })
            .then((auth) => {
                if (auth) {
                    setIsAuthenticated(true);
                    setToken(keycloak.token);
                }
            })
            .catch((err) => console.error('Keycloak init failed', err));
    }, []);

    const logout = () => keycloak.logout();

    return (
        <AuthContext.Provider value={{ isAuthenticated, token, logout }}>
            {children}
        </AuthContext.Provider>
    );
}
