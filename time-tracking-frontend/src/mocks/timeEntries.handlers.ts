// timeEntries.handlers.ts
import { http, HttpResponse } from 'msw';
import type { CreateTimeEntryRequest, TimeEntry, Project } from '@/types';

let seq = 1;
let projects: Project[] = [
    { id: 1, name: 'TrackLight' },
    { id: 2, name: 'SolidTime' },
];
let entries: TimeEntry[] = [
    {
        id: seq++,
        projectId: 1,
        projectName: 'TrackLight',
        title: 'Initial setup',
        startTime: '2025-08-10T09:00',
        endTime: '2025-08-10T10:30',
        durationMinutes: 90,
        description: null,
    },
];

export const timeEntriesHandlers = [
    // Specific, happy-path handlers first
    http.get('*/api/projects', () => HttpResponse.json(projects)),

    http.get('*/api/time-entries', () => {
        const sorted = [...entries].sort((a, b) => (a.startTime < b.startTime ? 1 : -1));
        return HttpResponse.json(sorted);
    }),

    http.post('*/api/time-entries', async ({ request }) => {
        const body = (await request.json()) as CreateTimeEntryRequest;
        const proj = projects.find(p => p.id === body.projectId);
        const duration = Math.max(
            0,
            Math.round((+new Date(body.endTime) - +new Date(body.startTime)) / 60000)
        );
        const newEntry: TimeEntry = {
            id: seq++,
            projectId: body.projectId,
            projectName: proj?.name ?? 'Unknown',
            title: body.title,
            startTime: body.startTime,
            endTime: body.endTime,
            durationMinutes: duration,
            description: body.description ?? null,
        };
        entries = [newEntry, ...entries];
        return HttpResponse.json(newEntry, { status: 200 });
    }),

    http.delete('*/api/time-entries/:id', ({ params }) => {
        const id = Number(params.id);
        entries = entries.filter(e => e.id !== id);
        return new HttpResponse(null, { status: 204 });
    }),
];

// Reset helper for tests
export function resetTimeEntries() {
    seq = 1;
    projects = [
        { id: 1, name: 'TrackLight' },
        { id: 2, name: 'SolidTime' },
    ];
    entries = [
        {
            id: seq++,
            projectId: 1,
            projectName: 'TrackLight',
            title: 'Initial setup',
            startTime: '2025-08-10T09:00',
            endTime: '2025-08-10T10:30',
            durationMinutes: 90,
            description: null,
        },
    ];
}