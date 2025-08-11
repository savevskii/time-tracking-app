import { vi } from 'vitest';

export const keycloakMock = {
    authenticated: true,
    token: 'test-token',
    init: vi.fn().mockResolvedValue(true),
    logout: vi.fn(),
    updateToken: vi.fn(),
};

export function resetKeycloakMock() {
    keycloakMock.authenticated = true;
    keycloakMock.token = 'test-token';
    keycloakMock.init.mockResolvedValue(true);
    keycloakMock.logout.mockReset();
    keycloakMock.updateToken.mockReset();
}