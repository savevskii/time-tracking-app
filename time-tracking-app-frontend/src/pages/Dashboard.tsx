import { useEffect, useState } from 'react';
import api from '@/api/apiClient';
import type { Summary } from '@/types';

export default function Dashboard() {
    const [data, setData] = useState<Summary | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError]   = useState<string | null>(null);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            setError(null);
            try {
                const { data } = await api.get<Summary>('/api/summary');
                setData(data);
            } catch {
                setError('Failed to load summary');
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    return (
        <div className="max-w-4xl mx-auto">
            <h2 className="text-2xl font-bold mb-4">Dashboard</h2>

            {loading && <p>Loading...</p>}
            {error && <p className="text-red-600">{error}</p>}

            {data && (
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <div className="rounded-lg bg-white p-4 shadow border">
                        <p className="text-sm text-gray-500">Total Projects</p>
                        <p className="mt-1 text-3xl font-semibold">{data.totalProjects}</p>
                    </div>
                    <div className="rounded-lg bg-white p-4 shadow border">
                        <p className="text-sm text-gray-500">Hours this week</p>
                        <p className="mt-1 text-3xl font-semibold">{data.weekHours}</p>
                    </div>
                    <div className="rounded-lg bg-white p-4 shadow border">
                        <p className="text-sm text-gray-500">Hours today</p>
                        <p className="mt-1 text-3xl font-semibold">{data.todayHours}</p>
                    </div>
                </div>
            )}
        </div>
    );
}