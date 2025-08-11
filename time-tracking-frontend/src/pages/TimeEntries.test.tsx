import { screen, waitFor, within } from '@testing-library/react';
import { renderWithProviders } from '@/test/render';
import userEvent from '@testing-library/user-event';
import TimeEntries from './TimeEntries';
import { server } from '@/mocks/server';
import { http, HttpResponse } from 'msw';
import { resetTimeEntries } from '@/mocks/handlers';

// Ensure MSW is started in your test setup file.
// If you reset per test file:
beforeEach(() => {
    resetTimeEntries();
});

describe('TimeEntries page', () => {
    it('renders list and can create a new entry', async () => {
        renderWithProviders(<TimeEntries />, '/entries');

        // initial item
        expect(await screen.findByText(/Initial setup/i)).toBeInTheDocument();

        // fill form
        await userEvent.selectOptions(screen.getByRole('combobox'), '1'); // TrackLight
        await userEvent.type(screen.getByLabelText(/Title/i), 'Implement cards');
        await userEvent.type(screen.getByLabelText(/^Start$/i), '2025-08-10T11:00');
        await userEvent.type(screen.getByLabelText(/^End$/i), '2025-08-10T13:30');

        await userEvent.click(screen.getByRole('button', { name: /add entry/i }));

        // new entry shows up
        await screen.findByText(/Implement cards/i);
        expect(screen.getAllByText(/TrackLight/)[0]).toBeInTheDocument();
    });

    it('validates end after start (button disabled)', async () => {
        renderWithProviders(<TimeEntries />, '/entries');

        await screen.findByText(/Initial setup/i);

        await userEvent.selectOptions(screen.getByRole('combobox'), '1');
        await userEvent.type(screen.getByLabelText(/Title/i), 'Bad timing');
        await userEvent.type(screen.getByLabelText(/^Start$/i), '2025-08-10T14:00');
        await userEvent.type(screen.getByLabelText(/^End$/i), '2025-08-10T13:00');

        const btn = screen.getByRole('button', { name: /add entry/i });
        expect(btn).toBeDisabled();
    });

    it('can delete an entry', async () => {
        vi.spyOn(window, 'confirm').mockReturnValue(true);

        renderWithProviders(<TimeEntries />, '/entries');

        const row = await screen.findByText(/Initial setup/i);
        const item = row.closest('li') ?? row.parentElement!;
        const del = within(item).getByRole('button', { name: /delete/i });

        await userEvent.click(del);

        await waitFor(() => {
            expect(screen.queryByText(/Initial setup/i)).not.toBeInTheDocument();
        });
    });

    it('shows error if list fails', async () => {
        // override the list handler for this test only
        server.use(
            http.get('*/api/time-entries', () => HttpResponse.error())
        );

        renderWithProviders(<TimeEntries />, '/entries');

        expect(await screen.findByText(/Failed to load time entries/i)).toBeInTheDocument();
    });
});
