import { http, HttpResponse } from 'msw';
import type { Project } from '@/types';

type NewProject = Omit<Project, 'id'>;

export const seedProjects = (): Project[] => ([
    { id: 1, name: 'Project One', description: 'Desc One' },
    { id: 2, name: 'Project Two' }, // exercise optional description
]);

let projects = seedProjects();
export const resetProjects = () => { projects = seedProjects(); };

export const projectHandlers = [
    http.get('*/api/projects', () => HttpResponse.json(projects)),
    http.post('*/api/projects', async ({ request }) => {
        const body = (await request.json()) as NewProject;
        const newProject: Project = {
            id: Math.max(0, ...projects.map(p => p.id)) + 1,
            ...body,
        };
        projects = [...projects, newProject];
        return HttpResponse.json(newProject, { status: 201 });
    }),
    http.delete('*/api/projects/:id', ({ params }) => {
        const id = Number(params.id);
        projects = projects.filter(p => p.id !== id);
        return HttpResponse.json({}, { status: 204 });
    }),
];
