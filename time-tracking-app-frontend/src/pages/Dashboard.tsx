import TimeEntryForm from '../components/time-entry/TimeEntryForm';

export default function Dashboard() {
    return (
        <div className="py-10 px-4 space-y-8">
            <h2 className="text-2xl font-semibold text-gray-800">Dashboard</h2>
            <TimeEntryForm />
        </div>
    );
}