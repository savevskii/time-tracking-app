import api from './apiClient';
import type { CreateTimeEntryRequest, TimeEntry } from '@/types';

export async function listTimeEntries(): Promise<TimeEntry[]> {
    const { data } = await api.get<TimeEntry[]>('/api/time-entries');
    return data;
}

export async function createTimeEntry(req: CreateTimeEntryRequest): Promise<TimeEntry> {
    const { data } = await api.post<TimeEntry>('/api/time-entries', req);
    return data;
}

export async function deleteTimeEntry(id: number): Promise<void> {
    await api.delete(`/api/time-entries/${id}`);
}
