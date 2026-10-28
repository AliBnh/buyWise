import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Deadline, Reminder } from '../types';
import {
  CalendarClock,
  Clock,
  CheckCircle2,
  AlertTriangle,
  Bell,
  Check,
  Calendar,
  Filter,
} from 'lucide-react';

export const DeadlinesPage: React.FC = () => {
  const [deadlines, setDeadlines] = useState<Deadline[]>([]);
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState<string>('ALL');

  useEffect(() => {
    fetchDeadlinesAndReminders();
  }, []);

  const fetchDeadlinesAndReminders = async () => {
    setLoading(true);
    try {
      const [deadlinesData, remindersData] = await Promise.all([
        api.get<Deadline[]>('/deadlines'),
        api.get<Reminder[]>('/deadlines/reminders'),
      ]);
      setDeadlines(deadlinesData);
      setReminders(remindersData);
    } catch (e) {
      console.error('Failed to load deadlines', e);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkCompleted = async (id: number) => {
    try {
      const updated = await api.patch<Deadline>(`/deadlines/${id}/complete`);
      setDeadlines((prev) => prev.map((d) => (d.id === id ? updated : d)));
    } catch (e: any) {
      alert('Failed to mark deadline completed: ' + e.message);
    }
  };

  const handleDismissReminder = async (id: number) => {
    try {
      await api.patch(`/deadlines/reminders/${id}/dismiss`);
      setReminders((prev) => prev.filter((r) => r.id !== id));
    } catch (e: any) {
      alert('Failed to dismiss reminder: ' + e.message);
    }
  };

  const filteredDeadlines = deadlines.filter((d) => {
    if (filterType === 'ALL') return true;
    if (filterType === 'ACTIVE') return !d.isCompleted && !d.isExpired;
    if (filterType === 'EXPIRED') return d.isExpired && !d.isCompleted;
    if (filterType === 'COMPLETED') return d.isCompleted;
    return d.deadlineType === filterType;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <CalendarClock className="w-6 h-6 text-emerald-600" />
            Deadlines & Reminder Management
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Deterministic monitoring of return periods, manufacturer warranty expirations, and scheduled maintenance.
          </p>
        </div>
      </div>

      {/* Active In-App Reminders Banner */}
      {reminders.length > 0 && (
        <div className="bg-amber-50 border border-amber-200 rounded-2xl p-5 space-y-3">
          <div className="flex items-center justify-between pb-2 border-b border-amber-200/60">
            <h3 className="text-xs font-bold text-amber-900 flex items-center gap-2">
              <Bell className="w-4 h-4 text-amber-600" />
              Active In-App Reminder Notifications ({reminders.length})
            </h3>
            <span className="text-[10px] text-amber-800 font-medium">
              Evaluated Idempotently by Background Daemon
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {reminders.map((rem) => (
              <div
                key={rem.id}
                className="bg-white p-4 rounded-xl border border-amber-200 shadow-xs flex items-start justify-between gap-3 text-xs"
              >
                <div>
                  <p className="font-semibold text-slate-900 mb-1">{rem.message}</p>
                  <p className="text-[11px] text-slate-500">
                    Trigger Date: {rem.triggerDate}
                  </p>
                </div>
                <button
                  onClick={() => handleDismissReminder(rem.id)}
                  className="px-2.5 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg shrink-0 transition-colors"
                >
                  Dismiss
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Filters Bar */}
      <div className="flex items-center justify-between bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
        <div className="flex items-center gap-2 overflow-x-auto text-xs font-medium">
          {[
            { id: 'ALL', label: 'All Events' },
            { id: 'ACTIVE', label: 'Active & Pending' },
            { id: 'WARRANTY_EXPIRY', label: 'Warranty Expiries' },
            { id: 'RETURN_WINDOW', label: 'Return Windows' },
            { id: 'EXPIRED', label: 'Past Due' },
            { id: 'COMPLETED', label: 'Completed' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setFilterType(tab.id)}
              className={`px-3 py-1.5 rounded-lg transition-colors whitespace-nowrap ${
                filterType === tab.id
                  ? 'bg-slate-900 text-white font-semibold'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Deadlines List */}
      {loading ? (
        <div className="space-y-3 animate-pulse">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-20 bg-slate-200 rounded-xl" />
          ))}
        </div>
      ) : filteredDeadlines.length === 0 ? (
        <div className="p-12 text-center bg-white border border-slate-200 rounded-2xl">
          <Calendar className="w-10 h-10 text-slate-300 mx-auto mb-2" />
          <p className="text-xs text-slate-500">No deadlines match the selected filter.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {filteredDeadlines.map((d) => {
            const isCritical = d.daysRemaining <= 7 && !d.isCompleted && !d.isExpired;

            return (
              <div
                key={d.id}
                className={`bg-white p-5 rounded-2xl border shadow-xs transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4 ${
                  d.isCompleted
                    ? 'border-slate-200 opacity-60 bg-slate-50/50'
                    : isCritical
                    ? 'border-rose-300 bg-rose-50/20'
                    : 'border-slate-200'
                }`}
              >
                <div className="flex items-start gap-3">
                  <div
                    className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${
                      d.isCompleted
                        ? 'bg-slate-100 text-slate-500'
                        : isCritical
                        ? 'bg-rose-100 text-rose-700'
                        : 'bg-emerald-100 text-emerald-700'
                    }`}
                  >
                    <CalendarClock className="w-5 h-5" />
                  </div>

                  <div>
                    <div className="flex items-center gap-2">
                      <h4 className="text-xs font-bold text-slate-900">{d.title}</h4>
                      <span className="px-2 py-0.5 bg-slate-100 text-slate-700 text-[10px] font-semibold rounded-md">
                        {d.deadlineType.replace(/_/g, ' ')}
                      </span>
                    </div>

                    <p className="text-xs text-slate-600 mt-1">
                      Target Date: <strong>{d.dueDate}</strong> •{' '}
                      {d.isCompleted ? (
                        <span className="text-slate-500 font-semibold">Completed</span>
                      ) : d.isExpired ? (
                        <span className="text-rose-600 font-bold">Expired</span>
                      ) : (
                        <span
                          className={isCritical ? 'text-rose-700 font-bold' : 'text-slate-800 font-semibold'}
                        >
                          {d.daysRemaining} days remaining
                        </span>
                      )}
                    </p>

                    {d.notes && (
                      <p className="text-[11px] text-slate-500 mt-1 leading-relaxed">
                        {d.notes}
                      </p>
                    )}
                  </div>
                </div>

                {!d.isCompleted && (
                  <button
                    onClick={() => handleMarkCompleted(d.id)}
                    className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-xl text-xs font-semibold shrink-0 flex items-center gap-1.5 transition-colors"
                  >
                    <Check className="w-3.5 h-3.5" />
                    Mark Completed
                  </button>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
