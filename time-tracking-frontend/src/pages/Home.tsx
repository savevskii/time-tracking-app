import { Link } from 'react-router-dom';
import { Button } from '@/components/ui';
import useAuth from '@/hooks/useAuth';

export default function Home() {
    const { isAuthenticated, isAdmin } = useAuth();

    return (
        <section className="relative">
            <div className="pointer-events-none absolute inset-x-0 -top-10 -z-10 h-64 bg-gradient-to-b from-blue-50 to-transparent" />

            <div className="mx-auto max-w-5xl px-4 py-12">
                {/* Badge + Heading (keeps your test text) */}
                <div className="text-center">
          <span className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-200">
            TrackLight • simple time tracking
          </span>

                    <h1 className="mt-4 text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl">
                        Welcome to TrackLight
                    </h1>

                    <p className="mx-auto mt-4 max-w-2xl text-lg text-gray-600">
                        A lightweight time-tracking app: clean forms, fast logging, and clear project insights.
                    </p>

                    <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
                        {isAuthenticated ? (
                            <>
                                <Link to="/time-entries">
                                    <Button size="lg">Log Time</Button>
                                </Link>

                                {isAdmin && (
                                    <>
                                        <Link to="/dashboard">
                                            <Button size="lg">Admin Dashboard</Button>
                                        </Link>
                                        <Link to="/projects">
                                            <Button size="lg">Manage Projects</Button>
                                        </Link>
                                    </>
                                )}
                            </>
                        ) : (
                            <Link to="/time-entries">
                                <Button size="lg">Get Started</Button>
                            </Link>
                        )}
                    </div>
                </div>

                <div className="mt-12 grid gap-4 sm:grid-cols-3">
                    <Feature emoji="⏱️" title="Fast entry" desc="Log start & end in seconds—no clutter." />
                    <Feature emoji="📊" title="Project insights" desc="Weekly and monthly roll-ups per project." />
                    <Feature emoji="🔒" title="Secure by default" desc="Keycloak-protected access." />
                </div>

                <p className="mt-10 text-center text-sm text-gray-500">
                    Built for a diploma thesis • CI/CD with Spring Boot + React + Jenkins + K8s
                </p>
            </div>
        </section>
    );
}

function Feature({ emoji, title, desc }: { emoji: string; title: string; desc: string }) {
    return (
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
            <div className="flex items-center gap-3">
                <span className="text-2xl">{emoji}</span>
                <h3 className="text-base font-semibold text-gray-900">{title}</h3>
            </div>
            <p className="mt-2 text-sm text-gray-600">{desc}</p>
        </div>
    );
}