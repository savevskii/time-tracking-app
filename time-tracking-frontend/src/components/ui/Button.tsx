import { ButtonHTMLAttributes, forwardRef } from "react";
import clsx from "clsx";

type Variant = "default" | "destructive" | "outline";
type Size = "sm" | "md" | "lg";

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: Variant;
    size?: Size;
}

const variantClasses: Record<Variant, string> = {
    default: "bg-blue-600 text-white hover:bg-blue-700",
    destructive: "bg-red-600 text-white hover:bg-red-700",
    outline: "border border-gray-300 text-gray-700 hover:bg-gray-100 dark:border-gray-600 dark:text-gray-300",
};

const sizeClasses: Record<Size, string> = {
    sm: "px-3 py-1 text-sm",
    md: "px-4 py-2 text-base",
    lg: "px-5 py-3 text-lg",
};

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
    (
        { className = "", variant = "default", size = "md", ...props },
        ref
    ) => {
        return (
            <button
                ref={ref}
                className={clsx(
                    "rounded-md font-medium transition-colors duration-150",
                    variantClasses[variant],
                    sizeClasses[size],
                    className
                )}
                {...props}
            />
        );
    }
);

Button.displayName = "Button";
export { Button };
