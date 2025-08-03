import axios from "axios";
import useKeycloak from '../hooks/useKeycloak';

const apiBaseUrl = "/api"; // adjust to match backend path

export const useApiClient = () => {
    const { keycloak } = useKeycloak();

    const instance = axios.create({
        baseURL: apiBaseUrl,
        headers: {
            "Content-Type": "application/json",
        },
    });

    instance.interceptors.request.use((config) => {
        if (keycloak?.token) {
            config.headers["Authorization"] = `Bearer ${keycloak.token}`;
        }
        return config;
    });

    return instance;
};