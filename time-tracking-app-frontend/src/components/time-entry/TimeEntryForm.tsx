import { useState } from 'react';
import api from '../../api/apiClient';


const MOCK_PROJECTS = ['Project Alpha', 'Project Beta', 'Internal', 'Research'];

export default function TimeEntryForm() {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        from: '',
        to: '',
        projectEntity: '',
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    };

    const [errors, setErrors] = useState<Record<string, string>>({});

    const validate = () => {
        const newErrors: Record<string, string> = {};
        if (!formData.title.trim()) newErrors.title = 'Title is required';
        if (!formData.from) newErrors.from = 'Start time is required';
        if (!formData.to) newErrors.to = 'End time is required';
        if (formData.from && formData.to && formData.from >= formData.to)
            newErrors.to = 'End time must be after start time';
        if (!formData.projectEntity) newErrors.projectEntity = 'Please select a projectEntity';
        return newErrors;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const validationErrors = validate();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        try {
            await api.post('/time-entries', formData);
            alert('Entry saved!');
            setFormData({ title: '', description: '', from: '', to: '', projectEntity: '' });
        } catch (err: any) {
            console.error(err);
            alert(err?.response?.data?.message || 'Submission failed.');
        }
    };


    return (
        <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow p-6 max-w-2xl mx-auto space-y-4">
            <h3 className="text-xl font-semibold text-gray-800">New Time Entry</h3>

            <div>
                <label className="block text-sm font-medium text-gray-700">Title</label>
                <input
                    name="title"
                    value={formData.title}
                    onChange={handleChange}
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                />
            </div>

            <div>
                <label className="block text-sm font-medium text-gray-700">Description</label>
                <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows={3}
                    className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700">From</label>
                    <input
                        type="datetime-local"
                        name="from"
                        value={formData.from}
                        onChange={handleChange}
                        required
                        className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700">To</label>
                    <input
                        type="datetime-local"
                        name="to"
                        value={formData.to}
                        onChange={handleChange}
                        required
                        className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                    />
                </div>
            </div>

            <div>
                <label className="block text-sm font-medium text-gray-700">Project</label>
                <select
                    name="projectEntity"
                    value={formData.projectEntity}
                    onChange={handleChange}
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                >
                    <option value="">Select a projectEntity</option>
                    {MOCK_PROJECTS.map((proj) => (
                        <option key={proj} value={proj}>
                            {proj}
                        </option>
                    ))}
                </select>
            </div>

            <button
                type="submit"
                className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            >
                Save Entry
            </button>
        </form>
    );
}