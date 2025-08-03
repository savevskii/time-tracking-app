import React from 'react';
import NavBar from './NavBar';
import type { ReactNode } from 'react'

interface LayoutProps {
    children: ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => (
    <>
        <NavBar />
        <div className="max-w-3xl mx-auto px-4">
            {children}
        </div>
    </>
);

export default Layout;