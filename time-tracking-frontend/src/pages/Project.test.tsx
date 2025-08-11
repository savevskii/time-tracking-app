import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Projects from './Projects';
import { vi } from 'vitest';

const nameInput = () => screen.getByPlaceholderText(/project name/i);
const descInput = () => screen.getByPlaceholderText(/description/i);
const createBtn = () => screen.getByRole('button', { name: /create/i });

describe('Projects', () => {
    it('loads and displays projects from API', async () => {
        render(<Projects />);
        expect(await screen.findByText('Project One')).toBeInTheDocument();
        expect(screen.getByText('Project Two')).toBeInTheDocument();
    });

    it('creates a project and clears the form', async () => {
        render(<Projects />);
        const user = userEvent.setup();

        await screen.findByText('Project One');

        await user.type(nameInput(), 'New Project');
        await user.type(descInput(), 'New Desc');
        await user.click(createBtn());

        await waitFor(() => {
            expect(nameInput()).toHaveValue('');
            expect(descInput()).toHaveValue('');
        });

        expect(await screen.findByText('New Project')).toBeInTheDocument();
    });

    it('deletes a project', async () => {
        render(<Projects />);
        const user = userEvent.setup();

        await screen.findByText('Project One');

        const spyConfirm = vi.spyOn(window, 'confirm').mockReturnValue(true);
        const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
        await user.click(deleteButtons[0]);

        await waitFor(() =>
            expect(screen.queryByText('Project One')).not.toBeInTheDocument()
        );
        spyConfirm.mockRestore();
    });
});
