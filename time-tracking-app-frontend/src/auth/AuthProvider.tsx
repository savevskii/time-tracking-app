import React, { createContext, useContext, useEffect, useState } from "react";
import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: "http://localhost:8080/auth",
    realm: "your-realm",
    clientId: "your-client-id",
});

const AuthContext = createContext({
    keycloak: keycloak,
    authenticated: false,
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {
        keycloak.init({ onLoad: "login-required" }).then((auth) => {
            setAuthenticated(auth);
        });
    }, []);

    return (
        <AuthContext.Provider value={{ keycloak, authenticated }}>
            {authenticated ? children : <div className="p-4">Loading...</div>}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);