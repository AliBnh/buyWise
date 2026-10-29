import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useCurrency, CurrencyCode } from '../../context/CurrencyContext';
import { api } from '../../api/client';
import { Reminder } from '../../types';
import { Bell, LogOut, User as UserIcon, CheckCircle2, Menu } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface NavbarProps {
  onMobileMenuToggle?: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onMobileMenuToggle }) => {
  const { user, logout } = useAuth();
  const { currentCurrency, setCurrency } = useCurrency();
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchActiveReminders();
    const interval = setInterval(fetchActiveReminders, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchActiveReminders = async () => {
    try {
      const data = await api.get<Reminder[]>('/deadlines/reminders');
      setReminders(data);
    } catch (e) {
      // Ignored if unauthenticated
    }
  };

  const handleDismissReminder = async (id: number) => {
    try {
      await api.patch(`/deadlines/reminders/${id}/dismiss`);
      setReminders((prev) => prev.filter((r) => r.id !== id));
    } catch (e) {
      console.error('Failed to dismiss reminder', e);
    }
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200 sticky top-0 z-30 px-4 sm:px-6 flex items-center justify-between">
      <div className="flex items-center gap-3">
        {onMobileMenuToggle && (
          <button
            onClick={onMobileMenuToggle}
            className="p-2 text-slate-500 hover:text-slate-800 lg:hidden rounded-lg hover:bg-slate-100"
            aria-label="Toggle Navigation"
          >
            <Menu className="w-5 h-5" />
          </button>
        )}
        <div className="hidden sm:block text-xs text-slate-500">
          Consumer Purchase Intelligence & Ownership Platform
        </div>
      </div>

      <div className="flex items-center gap-3">
        {/* Currency Selector */}
        <div className="flex items-center bg-slate-100 rounded-lg p-1 text-xs font-medium">
          {(['MAD', 'EUR', 'USD'] as CurrencyCode[]).map((c) => (
            <button
              key={c}
              onClick={() => setCurrency(c)}
              className={`px-2.5 py-1 rounded-md transition-colors ${
                currentCurrency === c
                  ? 'bg-white text-slate-900 shadow-xs font-semibold'
                  : 'text-slate-500 hover:text-slate-900'
              }`}
            >
              {c}
            </button>
          ))}
        </div>

        {/* Reminders / Notifications */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="p-2 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg relative transition-colors"
            title="Notifications & Upcoming Deadlines"
          >
            <Bell className="w-5 h-5" />
            {reminders.length > 0 && (
              <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 bg-rose-500 rounded-full ring-2 ring-white animate-pulse" />
            )}
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-xl shadow-xl border border-slate-200 p-4 z-50">
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 mb-2">
                <h4 className="text-sm font-semibold text-slate-900">
                  Active Alerts ({reminders.length})
                </h4>
                <button
                  onClick={() => {
                    setShowNotifications(false);
                    navigate('/deadlines');
                  }}
                  className="text-xs text-emerald-600 hover:text-emerald-700 font-medium"
                >
                  View All Deadlines
                </button>
              </div>

              {reminders.length === 0 ? (
                <div className="py-6 text-center text-xs text-slate-500">
                  No pending alerts. All return windows and warranties are monitored.
                </div>
              ) : (
                <div className="max-h-72 overflow-y-auto space-y-2">
                  {reminders.map((rem) => (
                    <div
                      key={rem.id}
                      className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-xs"
                    >
                      <p className="text-slate-800 font-medium mb-1">{rem.message}</p>
                      <div className="flex items-center justify-between mt-2 pt-2 border-t border-amber-200/60">
                        <span className="text-[11px] text-amber-800">
                          Scheduled: {rem.triggerDate}
                        </span>
                        <button
                          onClick={() => handleDismissReminder(rem.id)}
                          className="flex items-center gap-1 text-[11px] text-slate-600 hover:text-emerald-700 font-medium"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          Dismiss
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* User profile & logout */}
        {user && (
          <div className="flex items-center gap-2 pl-3 border-l border-slate-200">
            <div className="flex items-center gap-2 text-xs">
              <div className="w-8 h-8 rounded-full bg-slate-900 text-white flex items-center justify-center font-semibold text-xs">
                {user.fullName.charAt(0)}
              </div>
              <div className="hidden md:block text-left">
                <p className="font-semibold text-slate-900 leading-tight">{user.fullName}</p>
                <p className="text-slate-500 text-[10px]">{user.email}</p>
              </div>
            </div>

            <button
              onClick={() => {
                logout();
                navigate('/login');
              }}
              className="p-2 text-slate-400 hover:text-rose-600 hover:bg-slate-100 rounded-lg transition-colors ml-1"
              title="Sign Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </header>
  );
};
