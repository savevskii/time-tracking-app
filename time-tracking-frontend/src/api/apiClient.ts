import axios from 'axios';
import keycloak from '@/auth/keycloak';

const api = axios.create({
    baseURL: 'http://localhost:9191',
    headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
    if (keycloak?.authenticated && keycloak.token) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${keycloak.token}`;
    }
    return config;
});

export default api;