import { useEffect, useMemo, useState } from 'react';
import { listTimeEntries, createTimeEntry, deleteTimeEntry } from '@/api/timeEntries';
import { listProjects } from '@/api/projects';
import { Input, Button } from '@/components/ui';
import type { Project, TimeEntry } from '@/types';

function minutesToHM(mins: number) {
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    return `${h}h ${m.toString().padStart(2, '0')}m`;
}

export default function TimeEntries() {
    const [entries, setEntries] = useState<TimeEntry[]>([]);
    const [projects, setProjects] = useState<Project[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // form state
    const [projectId, setProjectId] = useState<number | ''>('');
    const [title, setTitle] = useState('');
    const [startTime, setStartTime] = useState(''); // datetime-local string
    const [endTime, setEndTime] = useState('');
    const [description, setDescription] = useState('');

    useEffect(() => {
        (async () => {
            try {
                setLoading(true);
                const [p, e] = await Promise.all([listProjects(), listTimeEntries()]);
                setProjects(p);
                setEntries(e);
            } catch {
                setError('Failed to load time entries.');
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    const isValid = useMemo(() => {
        if (!projectId || !title.trim() || !startTime || !endTime) return false;
        return endTime > startTime; // basic client-side check
    }, [projectId, title, startTime, endTime]);

    const resetForm = () => {
        setProjectId('');
        setTitle('');
        setStartTime('');
        setEndTime('');
        setDescription('');
    };

    const onCreate = async () => {
        if (!isValid) return;
        setSaving(true);
        setError(null);
        try {
            const newEntry = await createTimeEntry({
                projectId: projectId as number,
                title: title.trim(),
                startTime,
                endTime,
                description: description.trim() ? description : null,
            });
            setEntries((prev) => [newEntry, ...prev]);
            resetForm();
        } catch {
            setError('Failed to create time entry.');
        } finally {
            setSaving(false);
        }
    };

    const onDelete = async (id: number) => {
        if (!confirm('Delete this time entry?')) return;
        const prev = entries;
        setEntries((p) => p.filter((x) => x.id !== id)); // optimistic
        try {
            await deleteTimeEntry(id);
        } catch {
            setEntries(prev);
            alert('Failed to delete entry.');
        }
    };

    return (
        <div className="p-6 max-w-4xl mx-auto">
            <h2 className="text-2xl font-bold mb-6">Time Entries</h2>

            {/* Create form */}
            <div className="bg-white rounded-lg shadow p-4 mb-6">
                <h3 className="text-lg font-semibold mb-3">Log work</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label htmlFor="project" className="block text-sm text-gray-600 mb-1">Project</label>
                        <select
                            id="project"
                            name="project"
                            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            value={projectId}
                            onChange={(e) => setProjectId(e.target.value ? Number(e.target.value) : '')}
                        >
                            <option value="">Select a project</option>
                            {projects.map((p) => (
                                <option key={p.id} value={p.id}>{p.name}</option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label htmlFor="title" className="block text-sm text-gray-600 mb-1">Title</label>
                        <Input
                            id="title"
                            name="title"
                            placeholder="e.g. Implement dashboard cards"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                        />
                    </div>

                    <div>
                        <label htmlFor="start" className="block text-sm text-gray-600 mb-1">Start</label>
                        <Input
                            id="start"
                            name="start"
                            type="datetime-local"
                            value={startTime}
                            onChange={(e) => setStartTime(e.target.value)}
                        />
                    </div>

                    <div>
                        <label htmlFor="end" className="block text-sm text-gray-600 mb-1">End</label>
                        <Input
                            id="end"
                            name="end"
                            type="datetime-local"
                            value={endTime}
                            onChange={(e) => setEndTime(e.target.value)}
                        />
                    </div>

                    <div className="md:col-span-2">
                        <label htmlFor="description" className="block text-sm text-gray-600 mb-1">
                            Description (optional)
                        </label>
                        <Input
                            id="description"
                            name="description"
                            placeholder="Notes…"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />
                    </div>
                </div>

                <div className="flex items-center gap-3 mt-4">
                    <Button onClick={onCreate} disabled={!isValid || saving}>
                        {saving ? 'Saving…' : 'Add entry'}
                    </Button>
                    {!isValid && (
                        <span className="text-sm text-gray-500">
              Select project, fill title, and ensure end after start.
            </span>
                    )}
                </div>

                {error && <p className="text-red-600 mt-2">{error}</p>}
            </div>

            {/* List */}
            <div className="bg-white rounded-lg shadow p-4">
                <h3 className="text-lg font-semibold mb-3">Your entries</h3>
                {loading ? (
                    <p>Loading…</p>
                ) : entries.length === 0 ? (
                    <p className="text-gray-500">No entries yet.</p>
                ) : (
                    <ul className="divide-y divide-gray-200">
                        {entries.map((e) => (
                            <li key={e.id} className="py-3 flex items-center justify-between">
                                <div className="min-w-0">
                                    <p className="font-medium truncate">{e.title}</p>
                                    <p className="text-sm text-gray-600">
                                        {e.projectName} • {e.startTime.replace('T', ' ')} → {e.endTime.replace('T', ' ')} • {minutesToHM(e.durationMinutes)}
                                    </p>
                                </div>
                                <Button
                                    variant="destructive"
                                    size="sm"
                                    onClick={() => onDelete(e.id)}
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