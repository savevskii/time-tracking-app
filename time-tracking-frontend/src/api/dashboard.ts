import api from './apiClient';
import type { OverviewReportResponse, ProjectsReportResponse } from '@/types';

export async function fetchAdminOverview(timezone?: string): Promise<OverviewReportResponse> {
    const { data } = await api.get<OverviewReportResponse>('/api/admin/reports/overview', {
        params: timezone ? { timezone } : undefined,
    });
    return data;
}

export async function fetchAdminProjectSummary(params?: {
    timezone?: string;
    startDate?: string;
    endDate?: string;
}): Promise<ProjectsReportResponse[]> {
    const { data } = await api.get<ProjectsReportResponse[]>('/api/admin/reports/projects', {
        params,
    });
    return data;
}
