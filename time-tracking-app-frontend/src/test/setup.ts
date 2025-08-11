import '@testing-library/jest-dom/vitest';
import { server } from '@/mocks/server';
import { resetProjects, resetDashboard } from '@/mocks/handlers';
import { vi } from 'vitest';
import { keycloakMock, resetKeycloakMock } from './keycloak.mock';
import ResizeObserver from 'resize-observer-polyfill';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
    server.resetHandlers();
    resetProjects();
    resetDashboard();
    resetKeycloakMock();
});
afterAll(() => server.close());

// Mock Keycloak
vi.mock('@/auth/keycloak', () => ({ default: keycloakMock }))

// --- Polyfill ResizeObserver for Recharts ---
vi.stubGlobal('ResizeObserver', ResizeObserver as unknown as typeof globalThis.ResizeObserver);