import { useEffect, useState } from 'react';
import { listProjects, createProject, deleteProject } from '@/api/projects';
import { Input, Button } from '@/components/ui';
import type { Project } from '@/types';

export default function Projects() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => { fetchProjects(); }, []);

    const fetchProjects = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await listProjects();
            setProjects(data);
        } catch {
            setError('Failed to load projects');
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = async () => {
        if (!name.trim()) return;
        setLoading(true);
        setError(null);
        try {
            await createProject(name, description);
            setName('');
            setDescription('');
            fetchProjects();
        } catch {
            setError('Failed to create project');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: number) => {
        setLoading(true);
        setError(null);

        const newProjects = projects.filter(project => project.id !== id);
        setProjects(newProjects);

        try {
            await deleteProject(id);
        } catch {
            setProjects(projects);
            setError('Failed to delete project');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h2 className="text-2xl font-bold mb-6">Projects</h2>

            <div className="bg-white rounded-lg shadow p-4 mb-6">
                <h3 className="text-lg font-semibold mb-2">Create Project</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <Input
                        aria-label="Project Name"
                        placeholder="Project Name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                    />
                    <Input
                        aria-label="Project Description"
                        placeholder="Description (optional)"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                </div>
                <Button
                    className="mt-4"
                    type="button"
                    onClick={handleCreate}
                    disabled={loading || !name.trim()}
                >
                    {loading ? 'Creating...' : 'Create'}
                </Button>
                {error && <p className="text-red-500 mt-2">{error}</p>}
            </div>

            <div className="bg-white rounded-lg shadow p-4">
                <h3 className="text-lg font-semibold mb-4">Project List</h3>
                {loading && projects.length === 0 ? (
                    <p>Loading...</p>
                ) : projects.length === 0 ? (
                    <p className="text-gray-500">No projects yet.</p>
                ) : (
                    <ul className="divide-y divide-gray-200">
                        {projects.map((project) => (
                            <li key={project.id} className="py-2 flex justify-between items-center">
                                <div>
                                    <p className="font-medium">{project.name}</p>
                                    <p className="text-sm text-gray-500">{project.description}</p>
                                </div>
                                <Button
                                    variant="destructive"
                                    onClick={() => {
                                        if (confirm('Are you sure you want to delete this project?')) {
                                            handleDelete(project.id);
                                        }
                                    }}
                                    disabled={loading}
                                >
                                    Delete
                                </Button>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
}
