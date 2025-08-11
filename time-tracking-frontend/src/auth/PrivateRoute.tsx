import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import useAuth from '@/hooks/useAuth';
import type { Role } from '@/types';

type Props = {
    children: ReactNode;
    allowedRoles?: Role[];
};

export default function PrivateRoute({ children, allowedRoles }: Props) {
    const { isAuthenticated, roles } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    if (allowedRoles && allowedRoles.length > 0) {
        const hasAnyAllowed = roles.some((r) => allowedRoles.includes(r as Role));
        if (!hasAnyAllowed) return <Navigate to="/not-authorized" replace />;
    }

    return <>{children}</>;
}
