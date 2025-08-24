import { screen } from '@testing-library/react';
import { renderWithProviders } from '@/test/render';
import Dashboard from './Dashboard';
import { server } from '@/mocks/server';
import { http, HttpResponse } from 'msw';

describe('Dashboard (admin)', () => {
    it('shows KPI cards and table', async () => {
        renderWithProviders(<Dashboard />);

        // KPI cards
        expect(await screen.findByText(/Total Projects/i)).toBeInTheDocument();
        expect(await screen.findByText(/Hours Today/i)).toBeInTheDocument();
        expect(await screen.findByText(/Hours This Week/i)).toBeInTheDocument();

        // Table & rows
        expect(await screen.findByText(/Projects overview/i)).toBeInTheDocument();
        expect(await screen.findByText('TrackLight')).toBeInTheDocument();
        expect(screen.getByText('SolidTime')).toBeInTheDocument();
    });

    it('shows error when overview fails', async () => {
        // Make ONLY /api/admin/reports/overview fail; summary table can still load or not—doesn’t matter
        server.use(http.get('*/api/admin/reports/overview', () => HttpResponse.error()));

        renderWithProviders(<Dashboard />);

        expect(await screen.findByRole('alert')).toHaveTextContent(/Failed to load admin dashboard/i);
    });
});
