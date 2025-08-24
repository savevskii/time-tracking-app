import { http, HttpResponse } from 'msw';

export const dashboardHandlers = [
    http.get('*/api/admin/reports/overview', () =>
        HttpResponse.json({
            totalProjects: 12,
            hoursToday: 18.5,
            hoursThisWeek: 240.0,
            topProjectsThisWeek: [
                { projectId: 3, projectName: 'TrackLight', hours: 42.5 },
                { projectId: 7, projectName: 'SolidTime', hours: 31.0 },
            ],
        })
    ),

    http.get('*/api/admin/reports/projects', () => {
        // could inspect searchParams if you like
        return HttpResponse.json([
            {
                projectId: 3,
                projectName: 'TrackLight',
                hoursWeek: 42.5,
                hoursMonth: 156.0,
                entriesWeek: 38,
                lastEntryAt: '2025-08-10T16:45:00',
            },
            {
                projectId: 7,
                projectName: 'SolidTime',
                hoursWeek: 31.0,
                hoursMonth: 120.5,
                entriesWeek: 22,
                lastEntryAt: '2025-08-10T15:10:00',
            },
        ]);
    }),
];

export function resetDashboard() {
    // nothing stateful yet; left for symmetry
}