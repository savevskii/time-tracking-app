import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import useAuth from '../hooks/useAuth';

export default function PrivateRoute({ children }: { children: ReactNode }) {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
}
