import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Home from './Home';

describe('HomePage', () => {
    it('renders a welcome text', () => {
        render(
            <MemoryRouter initialEntries={['/']}>
                <Home />
            </MemoryRouter>
        );
        expect(screen.getByText(/Welcome to TrackLight/i)).toBeInTheDocument();
    });
});