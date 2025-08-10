import { ReactElement } from 'react';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '@/auth/AuthProvider';

// Minimal provider wrapper; add more providers as your app grows
export function renderWithProviders(ui: ReactElement, route = '/') {
    return render(
        <AuthProvider>
            <MemoryRouter initialEntries={[route]}>{ui}</MemoryRouter>
        </AuthProvider>
    );
}