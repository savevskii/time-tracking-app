import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Home from './Home';

test('renders welcome text', () => {
    render(
        <BrowserRouter>
            <Home />
        </BrowserRouter>
    );

    expect(screen.getByText(/Welcome to SolidTime/i)).toBeInTheDocument();
});