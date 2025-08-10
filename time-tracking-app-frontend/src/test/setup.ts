import '@testing-library/jest-dom/vitest';
import { server } from '@/mocks/server';
import { resetProjects, resetDashboard } from '@/mocks/handlers';
import { vi } from 'vitest';
import { keycloakMock, resetKeycloakMock } from './keycloak.mock';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
    server.resetHandlers();
    resetProjects();
    resetDashboard();
    resetKeycloakMock();
});
afterAll(() => server.close());

// Mock Keycloak
vi.mock('@/auth/keycloak', () => ({ default: keycloakMock }));