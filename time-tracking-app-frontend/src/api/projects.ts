import api from './apiClient';
import type { Project } from '@/types';

export async function listProjects(): Promise<Project[]> {
    const { data } = await api.get<Project[]>('/api/projects');
    return data;
}

export async function createProject(name: string, description: string): Promise<void> {
    await api.post('/api/projects', { name, description });
}

export async function deleteProject(id: number): Promise<void> {
    await api.delete(`/api/projects/${id}`);
}