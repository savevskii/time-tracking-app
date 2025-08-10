import { projectHandlers } from './projects.handlers';
import { dashboardHandlers } from './dashboard.handlers';

export const handlers = [
    ...projectHandlers,
    ...dashboardHandlers,
];

export { resetProjects } from './projects.handlers';
export { resetDashboard } from './dashboard.handlers';