import '@testing-library/jest-dom/vitest';
import { server } from '@/mocks/server';
import { resetProjects } from '@/mocks/handlers';
import { vi } from 'vitest';

// MSW lifecycle
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
    server.resetHandlers();
    resetProjects();               // <- reset in-memory DB after each test
});
afterAll(() => server.close());

// Mock Keycloak module so axios interceptor doesn't blow up in tests
vi.mock('@/auth/keycloak', () => ({
    default: { authenticated: false, token: undefined },
}));