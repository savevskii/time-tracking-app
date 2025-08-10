export interface TimeEntry {
    id: string;
    description: string;
    start: string;
    end: string | null;
}

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