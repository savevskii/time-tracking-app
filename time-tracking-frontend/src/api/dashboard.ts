import api from './apiClient';
import type { AdminOverviewResponse, ProjectSummaryRow } from '@/types';

export async function fetchAdminOverview(tz?: string): Promise<AdminOverviewResponse> {
    const { data } = await api.get<AdminOverviewResponse>('/api/admin/overview', {
        params: tz ? { tz } : undefined,
    });
    return data;
}

export async function fetchAdminProjectSummary(params?: {
    tz?: string;
    from?: string;
    to?: string;
}): Promise<ProjectSummaryRow[]> {
    const { data } = await api.get<ProjectSummaryRow[]>('/api/admin/projects/summary', {
        params,
    });
    return data;
}
