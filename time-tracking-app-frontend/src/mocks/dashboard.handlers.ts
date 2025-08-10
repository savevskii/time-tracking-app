import { http, HttpResponse } from 'msw';
import type { Summary } from '@/types';

let summary: Summary = { totalProjects: 2, weekHours: 12, todayHours: 3 };
export const resetDashboard = () => { summary = { totalProjects: 2, weekHours: 12, todayHours: 3 }; };

export const dashboardHandlers = [
    http.get('*/api/summary', () => HttpResponse.json(summary)),
];
