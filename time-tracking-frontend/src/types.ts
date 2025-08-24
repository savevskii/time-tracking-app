export interface Project {
    id: number;
    name: string;
    description?: string;
}

export interface Summary {
    totalProjects: number;
    weekHours: number;
    todayHours: number;
}

export interface TimeEntry {
    id: number;
    projectId: number;
    projectName: string;
    title: string;
    startTime: string; // ISO LocalDateTime e.g. '2025-08-10T09:00'
    endTime: string;   // ISO LocalDateTime
    durationMinutes: number;
    description?: string | null;
}

export interface CreateTimeEntryRequest {
    projectId: number;
    title: string;
    startTime: string; // 'YYYY-MM-DDTHH:mm' (from <input type="datetime-local" />)
    endTime: string;
    description?: string | null;
}

export interface OverviewReportResponse {
    totalProjects: number;
    hoursToday: number;
    hoursThisWeek: number;
    topProjectsThisWeek: Array<{ projectId: number; projectName: string; hours: number }>;
}

export interface ProjectsReportResponse {
    projectId: number;
    projectName: string;
    hoursWeek: number;
    hoursMonth: number;
    entriesWeek: number;
    lastEntryAt: string | null; // ISO
}

export type Role = 'admin' | 'user';

export interface TokenParsedMinimal {
    realm_access?: {
        roles?: string[];
    };
}