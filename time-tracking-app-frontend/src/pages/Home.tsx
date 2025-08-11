import { Link } from 'react-router-dom';
import { Button } from '@/components/ui';

export default function Home() {
    return (
        <section className="relative">
            {/* subtle gradient accent */}
            <div className="pointer-events-none absolute inset-x-0 -top-10 -z-10 h-64 bg-gradient-to-b from-blue-50 to-transparent" />

            <div className="mx-auto max-w-5xl px-4 py-12">
                {/* Hero */}
                <div className="text-center">
          <span className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-200">
            TrackLight • simple time tracking
          </span>

                    <h1 className="mt-4 text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl">
                        Track time. Stay focused. Ship faster.
                    </h1>

                    <p className="mx-auto mt-4 max-w-2xl text-lg text-gray-600">
                        A lightweight time-tracking app designed for clarity and speed—no clutter,
                        just the essentials you need to log work and see progress.
                    </p>

                    <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
                        <Link to="/dashboard">
                            <Button size="lg">Go to Dashboard</Button>
                        </Link>

                        <Link to="/entries" className="inline-block">
                            <Button size="lg">Log a Time Entry</Button>
                        </Link>

                        <Link to="/projects" className="inline-block">
                            <Button size="lg">Manage Projects</Button>
                        </Link>
                    </div>
                </div>

                {/* Features */}
                <div className="mt-12 grid gap-4 sm:grid-cols-3">
                    <Feature
                        emoji="⏱️"
                        title="Fast entry"
                        desc="Add start & end times in seconds with a clean, minimal form."
                    />
                    <Feature
                        emoji="📊"
                        title="Clear insights"
                        desc="See weekly project hours and trends at a glance."
                    />
                    <Feature
                        emoji="🔒"
                        title="Secure"
                        desc="Keycloak-protected access—your data stays yours."
                    />
                </div>

                {/* Tiny footer note */}
                <p className="mt-10 text-center text-sm text-gray-500">
                    Built for my diploma thesis • CI/CD with K8s + Jenkins + Automated testing
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