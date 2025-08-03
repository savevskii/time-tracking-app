import React from 'react'
import useKeycloak from '../hooks/useKeycloak';

const HomePage: React.FC = () => {
    const { keycloak, authenticated } = useKeycloak();

    if (!authenticated || !keycloak?.idTokenParsed) {
        return (
            <div>
                <h1>Welcome to the Home Page!</h1>
                <p>Please log in to access your personalized content.</p>
            </div>
        );
    }

    return (
        <div>
            <h1>Welcome to the Home Page!</h1>
            <p>Hello, {keycloak.idTokenParsed.preferred_username}!</p>
        </div>
    );
}

export default HomePage