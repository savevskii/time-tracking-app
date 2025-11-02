import { useEffect, useMemo, useState } from 'react';
import { fetchAdminOverview, fetchAdminProjectSummary } from '@/api/dashboard';
import type { OverviewReportResponse, ProjectsReportResponse } from '@/types';
import { Button, Input } from '@/components/ui';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

export default function Dashboard() {
    const [overview, setOverview] = useState<OverviewReportResponse | null>(null);
    const [rows, setRows] = useState<ProjectsReportResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);

    const [tz] = useState<string>('Europe/Skopje');
    const [from, setFrom] = useState<string>('');
    const [to, setTo] = useState<string>('');

    useEffect(() => {
        (async () => {
            try {
                setLoading(true);
                setErr(null);
                const [ov, table] = await Promise.all([
                    fetchAdminOverview(tz),
                    fetchAdminProjectSummary({ timezone: tz, startDate: from || undefined, endDate: to || undefined }),
                ]);
                setOverview(ov);
                setRows(table);
            } catch {
                setErr('Failed to load admin dashboard.');
            } finally {
                setLoading(false);
            }
        })();
    }, [tz, from, to]);

    const chartData = useMemo(
        () => (overview?.topProjectsThisWeek ?? []).map(t => ({ name: t.projectName, hours: t.hours })),
        [overview]
    );

    return (
        <div className="p-6 max-w-6xl mx-auto">
            <h2 className="text-2xl font-bold mb-6">Admin Dashboard</h2>

            {/* Filters */}
            <div className="bg-white rounded-lg shadow p-4 mb-6">
                <h3 className="text-lg font-semibold mb-3">Filters</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                        <label htmlFor="from" className="block text-sm text-gray-600 mb-1">From (optional)</label>
                        <Input id="from" type="datetime-local" value={from} onChange={(e) => setFrom(e.target.value)} />
                    </div>
                    <div>
                        <label htmlFor="to" className="block text-sm text-gray-600 mb-1">To (optional)</label>
                        <Input id="to" type="datetime-local" value={to} onChange={(e) => setTo(e.target.value)} />
                    </div>
                    <div className="flex items-end">
                        <Button onClick={() => { /* values are in state; effect refetches automatically */ }}>
                            Apply
                        </Button>
                    </div>
                </div>
                {err && <p role="alert" className="text-red-600 mt-2">{err}</p>}
            </div>

            {/* KPI Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
                <Card title="Total Projects" value={overview?.totalProjects ?? (loading ? '…' : 0)} />
                <Card title="Hours Today" value={overview?.hoursToday ?? (loading ? '…' : 0)} />
                <Card title="Hours This Week" value={overview?.hoursThisWeek ?? (loading ? '…' : 0)} />
            </div>

            {/* Chart + Table */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="bg-white rounded-lg shadow p-4 lg:col-span-1">
                    <h3 className="text-lg font-semibold mb-3">Top projects (this week)</h3>
                    <div className="h-64">
                        {loading ? (
                            <p>Loading…</p>
                        ) : chartData.length === 0 ? (
                            <p className="text-gray-500">No data.</p>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 24 }}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" interval={0} angle={-20} textAnchor="end" height={50} />
                                    <YAxis />
                                    <Tooltip />
                                    <Bar dataKey="hours" />
                                </BarChart>
                            </ResponsiveContainer>
                        )}
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow p-4 lg:col-span-2 overflow-x-auto">
                    <h3 className="text-lg font-semibold mb-3">Projects overview</h3>
                    {loading ? (
                        <p>Loading…</p>
                    ) : rows.length === 0 ? (
                        <p className="text-gray-500">No projects.</p>
                    ) : (
                        <table className="min-w-full text-sm">
                            <thead className="text-left text-gray-600">
                            <tr>
                                <th className="py-2 pr-4">Project</th>
                                <th className="py-2 pr-4">Hours (week)</th>
                                <th className="py-2 pr-4">Hours (month)</th>
                                <th className="py-2 pr-4">Entries (week)</th>
                                <th className="py-2 pr-4">Last entry</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                            {rows.map((r) => (
                                <tr key={r.projectId}>
                                    <td className="py-2 pr-4">{r.projectName}</td>
                                    <td className="py-2 pr-4">{r.hoursWeek}</td>
                                    <td className="py-2 pr-4">{r.hoursMonth}</td>
                                    <td className="py-2 pr-4">{r.entriesWeek}</td>
                                    <td className="py-2 pr-4">{r.lastEntryAt?.replace('T', ' ') ?? '—'}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
}

function Card({ title, value }: { title: string; value: number | string }) {
    return (
        <div className="bg-white rounded-lg shadow p-4">
            <p className="text-sm text-gray-600">{title}</p>
            <p className="text-2xl font-semibold mt-1">{value}</p>
        </div>
    );
}
