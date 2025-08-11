import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from '@/pages/Home';
import Dashboard from '@/pages/Dashboard';
import Projects from '@/pages/Projects';
import TimeEntries from '@/pages/TimeEntries';
import NotAuthorized from "@/pages/NotAuthorized";
import PrivateRoute from '@/auth/PrivateRoute';
import Layout from '@/components/Layout';
import { AuthProvider } from '@/auth/AuthProvider';

export default function App() {
    return (
        <AuthProvider>
            <Router>
                <div className="min-h-screen w-full bg-gray-50">
                    <Layout>
                        <div className="container mx-auto px-4">
                            <Routes>
                                <Route path="/" element={<Home />} />

                                <Route
                                    path="/time-entries"
                                    element={
                                        <PrivateRoute allowedRoles={['admin', 'user']}>
                                            <TimeEntries />
                                        </PrivateRoute>
                                    }
                                />

                                <Route
                                    path="/projects"
                                    element={
                                        <PrivateRoute allowedRoles={['admin']}>
                                            <Projects />
                                        </PrivateRoute>
                                    }
                                />

                                <Route
                                    path="/dashboard"
                                    element={
                                        <PrivateRoute allowedRoles={['admin']}>
                                            <Dashboard />
                                        </PrivateRoute>
                                    }
                                />

                                <Route path="/not-authorized" element={<NotAuthorized />} />
                            </Routes>
                        </div>
                    </Layout>
                </div>
            </Router>
        </AuthProvider>
    );
}