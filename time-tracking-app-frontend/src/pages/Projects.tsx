import { useEffect, useState } from 'react';
import axios from '../api/apiClient';
import { Input } from '@/components/ui';
import { Button } from '@/components/ui';

type Project = {
    id: number;
    name: string;
    description: string;
};

export default function Projects() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');

    useEffect(() => {
        fetchProjects();
    }, []);

    const fetchProjects = async () => {
        const response = await axios.get<Project[]>('/api/projects');
        setProjects(response.data);
    };

    const handleCreate = async () => {
        if (!name.trim()) return;

        await axios.post('/api/projects', { name, description });
        setName('');
        setDescription('');
        fetchProjects();
    };

    const handleDelete = async (id: number) => {
        await axios.delete(`/api/projects/${id}`);
        fetchProjects();
    };

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h2 className="text-2xl font-bold mb-6">Projects</h2>

            <div className="bg-white rounded-lg shadow p-4 mb-6">
                <h3 className="text-lg font-semibold mb-2">Create Project</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <Input
                        placeholder="Project Name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                    />
                    <Input
                        placeholder="Description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                </div>
                <Button className="mt-4" onClick={handleCreate}>
                    Create
                </Button>
            </div>

            <div className="bg-white rounded-lg shadow p-4">
                <h3 className="text-lg font-semibold mb-4">Project List</h3>
                {projects.length === 0 ? (
                    <p className="text-gray-500">No projects yet.</p>
                ) : (
                    <ul className="divide-y divide-gray-200">
                        {projects.map((project) => (
                            <li key={project.id} className="py-2 flex justify-between items-center">
                                <div>
                                    <p className="font-medium">{project.name}</p>
                                    <p className="text-sm text-gray-500">{project.description}</p>
                                </div>
                                <Button variant="destructive" onClick={() => handleDelete(project.id)}>
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