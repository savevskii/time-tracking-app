import { Link } from 'react-router-dom';

export default function Home() {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 p-4">
            <h1 className="text-4xl font-bold text-gray-800 mb-4">Welcome to SolidTime</h1>
            <p className="text-gray-600 text-lg mb-6">
                A simple and effective time tracking tool.
            </p>
            <Link
                to="/dashboard"
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            >
                Go to Dashboard
            </Link>
        </div>
    );
}