import { Outlet } from 'react-router-dom';
import Sidebar from './SIdebar';
import PushToast from '../ui/PushToast';

export default function MainLayout() {
    return (
        <div className="flex min-h-screen bg-[#0b0e14] text-slate-200">
            <Sidebar />
            <main className="flex-1 overflow-y-auto">
                <Outlet />
            </main>
            <PushToast />
        </div>
    );
}