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
    projectName?: string;
    date: string;         // ISO date: "2025-08-10"
    start: string;        // "HH:mm"
    end: string;          // "HH:mm"
    durationMinutes: number;
    description?: string;
}

export type NewTimeEntry = Omit<TimeEntry, 'id' | 'durationMinutes' | 'projectName'> & {
    durationMinutes?: number; // optional if backend computes it
};