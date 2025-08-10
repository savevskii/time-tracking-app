import { useEffect, useMemo, useState } from 'react';
import api from '@/api/apiClient';
import { Button, Input, Select } from '@/components/ui';
import type { Project, TimeEntry, NewTimeEntry } from '@/types';

export default function TimeEntries() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [entries, setEntries] = useState<TimeEntry[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // form state
    const [projectId, setProjectId] = useState<number | ''>('');
    const [date, setDate] = useState<string>(() => new Date().toISOString().slice(0, 10)); // YYYY-MM-DD
    const [start, setStart] = useState<string>('09:00');
    const [end, setEnd] = useState<string>('17:00');
    const [description, setDescription] = useState<string>('');

    useEffect(() => {
        void loadData();
    }, []);

    const loadData = async () => {
        setLoading(true);
        setError(null);
        try {
            const [{ data: proj }, { data: ents }] = await Promise.all([
                api.get<Project[]>('/api/projects'),
                api.get<TimeEntry[]>('/api/time-entries'),
            ]);
            setProjects(proj);
            setEntries(ents);
        } catch {
            setError('Failed to load data');
        } finally {
            setLoading(false);
        }
    };

    const durationMinutes = useMemo(() => {
        const [sh, sm] = start.split(':').map(Number);
        const [eh, em] = end.split(':').map(Number);
        const d = (eh * 60 + em) - (sh * 60 + sm);
        return isFinite(d) && d > 0 ? d : 0;
    }, [start, end]);

    const handleAdd = async () => {
        if (!projectId || !date || !start || !end) return;
        setLoading(true);
        setError(null);
        try {
            const payload: NewTimeEntry = {
                projectId: Number(projectId),
                date,
                start,
                end,
                description,
                // include if backend expects client-calculated duration:
                durationMinutes,
            };
            await api.post('/api/time-entries', payload);
            // reset description only; keep project/date/times for faster entry
            setDescription('');
            await loadData();
        } catch {
            setError('Failed to add entry');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm('Delete this entry?')) return;
        setLoading(true);
        setError(null);
        try {
            await api.delete(`/api/time-entries/${id}`);
            await loadData();
        } catch {
            setError('Failed to delete entry');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-6 max-w-4xl mx-auto">
            <h2 className="text-2xl font-bold mb-6">Time Entries</h2>

            {/* Form */}
            <div className="bg-white rounded-lg shadow p-4 mb-6 border">
                <h3 className="text-lg font-semibold mb-3">Add Entry</h3>
                <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
                    <Select
                        aria-label="Project"
                        value={projectId}
                        onChange={(e) => setProjectId(Number(e.target.value))}
                    >
                        <option value="">Select project</option>
                        {projects.map((p) => (
                            <option key={p.id} value={p.id}>{p.name}</option>
                        ))}
                    </Select>
                    <Input
                        type="date"
                        aria-label="Date"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                    />
                    <Input
                        type="time"
                        aria-label="Start time"
                        value={start}
                        onChange={(e) => setStart(e.target.value)}
                    />
                    <Input
                        type="time"
                        aria-label="End time"
                        value={end}
                        onChange={(e) => setEnd(e.target.value)}
                    />
                    <Input
                        aria-label="Description"
                        placeholder="Description (optional)"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                </div>

                <div className="mt-3 flex items-center justify-between">
                    <p className="text-sm text-gray-600">
                        Duration: <span className="font-medium">{Math.floor(durationMinutes / 60)}h {durationMinutes % 60}m</span>
                    </p>
                    <Button onClick={handleAdd} disabled={loading || !projectId || durationMinutes <= 0}>
                        {loading ? 'Saving...' : 'Add Entry'}
                    </Button>
                </div>

                {error && <p className="text-red-600 mt-2">{error}</p>}
            </div>

            {/* List */}
            <div className="bg-white rounded-lg shadow p-4 border">
                <h3 className="text-lg font-semibold mb-4">Entries</h3>
                {loading && entries.length === 0 ? (
                    <p>Loading...</p>
                ) : entries.length === 0 ? (
                    <p className="text-gray-500">No entries yet.</p>
                ) : (
                    <table className="w-full text-left">
                        <thead className="text-sm text-gray-500">
                        <tr className="border-b">
                            <th className="py-2 pr-2">Date</th>
                            <th className="py-2 pr-2">Project</th>
                            <th className="py-2 pr-2">Time</th>
                            <th className="py-2 pr-2">Duration</th>
                            <th className="py-2 pr-2">Description</th>
                            <th className="py-2 pr-2 text-right">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {entries.map((e) => (
                            <tr key={e.id} className="border-b last:border-0">
                                <td className="py-2 pr-2">{e.date}</td>
                                <td className="py-2 pr-2">{e.projectName ?? projects.find(p => p.id === e.projectId)?.name ?? e.projectId}</td>
                                <td className="py-2 pr-2">{e.start}–{e.end}</td>
                                <td className="py-2 pr-2">
                                    {Math.floor(e.durationMinutes / 60)}h {e.durationMinutes % 60}m
                                </td>
                                <td className="py-2 pr-2">{e.description}</td>
                                <td className="py-2 pr-2 text-right">
                                    <Button variant="destructive" size="sm" onClick={() => handleDelete(e.id)} disabled={loading}>
                                        Delete
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
}