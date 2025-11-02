// src/pages/Home.test.tsx
import { screen } from '@testing-library/react';
import { renderWithProviders } from '@/test/render';
import Home from './Home';

// Create a mutable auth stub used by the mock
const mockAuth = {
    isAuthenticated: false,
    isAdmin: false,
    roles: [] as string[],
    hasRole: (role: string) => (role === 'admin' ? mockAuth.isAdmin : mockAuth.roles.includes(role)),
    logout: vi.fn(),
};

// Mock the hook to return our mutable stub
vi.mock('@/hooks/useAuth', () => ({
    default: () => mockAuth,
}));

describe('Home (role-aware)', () => {
    beforeEach(() => {
        // default to unauthenticated
        mockAuth.isAuthenticated = false;
        mockAuth.isAdmin = false;
        mockAuth.roles = [];
        mockAuth.logout = vi.fn();
    });

    it('shows "Log Time" for regular users', async () => {
        mockAuth.isAuthenticated = true;
        mockAuth.isAdmin = false;

        renderWithProviders(<Home />);

        expect(screen.getByRole('button', { name: /log time/i })).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /admin dashboard/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /manage projects/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /get started/i })).not.toBeInTheDocument();
    });

    it('shows admin CTAs when user is admin', async () => {
        mockAuth.isAuthenticated = true;
        mockAuth.isAdmin = true;

        renderWithProviders(<Home />);

        expect(screen.getByRole('button', { name: /log time/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /admin dashboard/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /manage projects/i })).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /get started/i })).not.toBeInTheDocument();
    });
});
