import { screen } from '@testing-library/react';
import { renderWithProviders } from '@/test/render';
import Dashboard from './Dashboard';

describe('Dashboard', () => {
    it('shows summary cards', async () => {
        renderWithProviders(<Dashboard />, '/dashboard');

        expect(await screen.findByText(/Total Projects/i)).toBeInTheDocument();
        expect(screen.getByText(/Hours this week/i)).toBeInTheDocument();
        expect(screen.getByText(/Hours today/i)).toBeInTheDocument();

        expect(screen.getByText('2')).toBeInTheDocument();
        expect(screen.getByText('12')).toBeInTheDocument();
        expect(screen.getByText('3')).toBeInTheDocument();
    });
});