import api from './apiClient';
import type { Project } from '@/types';

export async function listProjects(): Promise<Project[]> {
    const { data } = await api.get<Project[]>('/api/projects');
    return data;
}