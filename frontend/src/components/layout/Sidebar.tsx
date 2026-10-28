import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Scale,
  PackageCheck,
  FileCheck2,
  CalendarClock,
  Sparkles,
  Settings,
  ShieldCheck,
  PlusCircle,
  HelpCircle,
} from 'lucide-react';

interface SidebarProps {
  onCloseMobile?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ onCloseMobile }) => {
  const navItems = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/comparisons', label: 'Offer Comparisons', icon: Scale },
    { to: '/purchases', label: 'Ownership Library', icon: PackageCheck },
    { to: '/documents', label: 'Document Verification', icon: FileCheck2 },
    { to: '/deadlines', label: 'Deadlines & Alerts', icon: CalendarClock },
    { to: '/assistant', label: 'BuyWise Assistant', icon: Sparkles },
    { to: '/settings', label: 'Settings & Demo', icon: Settings },
  ];

  return (
    <aside className="w-64 bg-white border-r border-slate-200 h-screen flex flex-col justify-between shrink-0">
      <div>
        {/* Brand Header */}
        <div className="h-16 px-6 border-b border-slate-200 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-slate-900 flex items-center justify-center text-white shadow-xs">
              <ShieldCheck className="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <span className="font-bold text-lg text-slate-900 tracking-tight block leading-tight">
                BuyWise
              </span>
              <span className="text-[10px] text-slate-500 font-medium tracking-wide block uppercase">
                Purchase Intelligence
              </span>
            </div>
          </div>
        </div>

        {/* Quick Action Button */}
        <div className="p-4">
          <NavLink
            to="/comparisons/new"
            onClick={onCloseMobile}
            className="w-full flex items-center justify-center gap-2 py-2.5 px-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors"
          >
            <PlusCircle className="w-4 h-4" />
            New Comparison
          </NavLink>
        </div>

        {/* Navigation Items */}
        <nav className="px-3 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onCloseMobile}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium transition-all ${
                    isActive
                      ? 'bg-slate-900 text-white shadow-xs font-semibold'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`
                }
              >
                <Icon className="w-4 h-4 shrink-0" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Footer Info Badge */}
      <div className="p-4 border-t border-slate-200">
        <div className="bg-slate-50 border border-slate-200 rounded-xl p-3">
          <div className="flex items-center justify-between text-[11px] font-medium text-slate-700 mb-1">
            <span className="flex items-center gap-1.5 text-emerald-700 font-semibold">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              Demo Ready (MAD)
            </span>
            <span className="text-[10px] text-slate-400">v1.0.0</span>
          </div>
          <p className="text-[11px] text-slate-500 leading-relaxed">
            Deterministic evaluation engine with verified source grounding.
          </p>
        </div>
      </div>
    </aside>
  );
};
