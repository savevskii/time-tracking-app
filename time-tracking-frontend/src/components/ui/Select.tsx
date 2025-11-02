import { forwardRef, SelectHTMLAttributes } from 'react';
import clsx from 'clsx';

export type SelectProps = SelectHTMLAttributes<HTMLSelectElement>;

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
    ({ className = '', children, ...props }, ref) => (
        <select
            ref={ref}
            className={clsx(
                'w-full border border-gray-300 rounded-md px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500',
                className
            )}
            {...props}
        >
            {children}
        </select>
    )
);
Select.displayName = 'Select';
