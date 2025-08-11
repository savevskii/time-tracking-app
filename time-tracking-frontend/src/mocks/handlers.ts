import { projectHandlers } from './projects.handlers';
import { dashboardHandlers } from './dashboard.handlers';
import { timeEntriesHandlers } from './timeEntries.handlers';

export const handlers = [
    ...projectHandlers,
    ...dashboardHandlers,
    ...timeEntriesHandlers,
];

export { resetProjects } from './projects.handlers';
export { resetDashboard } from './dashboard.handlers';
export { resetTimeEntries } from './timeEntries.handlers';