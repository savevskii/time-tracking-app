import { useState } from 'react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import api from '../../api/apiClient';

export default function CreateProjectForm() {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await api.post('/api/projects', { name, description });
            setName('');
            setDescription('');
        } catch (err) {
            setError('Failed to create project');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="max-w-xl mx-auto space-y-6 bg-white p-6 rounded shadow">
            <h2 className="text-2xl font-semibold text-gray-800">Create Project</h2>

            <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">Name</label>
                <Input
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Project name"
                    required
                />
            </div>

            <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">Description</label>
                <Input
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Description"
                />
            </div>

            {error && <p className="text-red-500 text-sm">{error}</p>}

            <Button type="submit" disabled={loading}>
                {loading ? 'Creating...' : 'Create Project'}
            </Button>
        </form>
    );
}
