import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'];

const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export default function CalendarPage() {
  const { user } = useAuth();
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth());
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDay, setSelectedDay] = useState(null);

  const fetchActivities = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get('/activities/calendar', {
        params: { userId: user.id, year, month: month + 1 }
      });
      setActivities(res.data.data);
    } catch {
      setActivities([]);
    } finally {
      setLoading(false);
    }
  }, [user.id, year, month]);

  useEffect(() => {
    fetchActivities();
  }, [fetchActivities]);

  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDayOfWeek = new Date(year, month, 1).getDay();

  const activityMap = {};
  activities.forEach((a) => {
    const d = new Date(a.dateTime).getDate();
    if (!activityMap[d]) activityMap[d] = [];
    activityMap[d].push(a);
  });

  const selectedActivities = selectedDay ? activityMap[selectedDay] || [] : [];

  function prevMonth() {
    setMonth((m) => {
      if (m === 0) { setYear((y) => y - 1); return 11; }
      return m - 1;
    });
    setSelectedDay(null);
  }

  function nextMonth() {
    setMonth((m) => {
      if (m === 11) { setYear((y) => y + 1); return 0; }
      return m + 1;
    });
    setSelectedDay(null);
  }

  function formatTime(dateStr) {
    return new Date(dateStr).toLocaleTimeString('en-IN', {
      hour: '2-digit', minute: '2-digit'
    });
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="page-title mb-6">Event Calendar</h1>

      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <button onClick={prevMonth} className="btn-secondary text-sm py-2 px-4">
            &larr; Prev
          </button>
          <h2 className="text-senior-xl font-bold text-gray-900">
            {MONTHS[month]} {year}
          </h2>
          <button onClick={nextMonth} className="btn-secondary text-sm py-2 px-4">
            Next &rarr;
          </button>
        </div>

        <div className="grid grid-cols-7 gap-1 mb-2">
          {DAYS.map((d) => (
            <div key={d} className="text-center text-xs font-semibold text-gray-500 py-2">
              {d}
            </div>
          ))}
        </div>

        <div className="grid grid-cols-7 gap-1">
          {Array.from({ length: firstDayOfWeek }).map((_, i) => (
            <div key={`empty-${i}`} className="aspect-square" />
          ))}
          {Array.from({ length: daysInMonth }).map((_, i) => {
            const day = i + 1;
            const hasActivities = !!activityMap[day];
            const isToday = day === today.getDate() && month === today.getMonth() && year === today.getFullYear();
            const isSelected = day === selectedDay;

            return (
              <button
                key={day}
                onClick={() => setSelectedDay(isSelected ? null : day)}
                className={`aspect-square flex flex-col items-center justify-center rounded-lg text-sm transition-colors relative
                  ${isSelected ? 'bg-primary-500 text-white' : isToday ? 'bg-primary-50 text-primary-700 font-bold' : 'hover:bg-gray-100'}
                `}
              >
                <span>{day}</span>
                {hasActivities && (
                  <span className={`text-[10px] mt-0.5 ${isSelected ? 'text-white' : 'text-primary-500'}`}>
                    {activityMap[day].length} event{activityMap[day].length > 1 ? 's' : ''}
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {selectedDay !== null && (
        <div className="mt-6">
          <h3 className="text-senior-lg font-semibold text-gray-900 mb-4">
            Activities on {MONTHS[month]} {selectedDay}, {year}
          </h3>

          {loading ? (
            <div className="text-center py-8 text-gray-500">Loading...</div>
          ) : selectedActivities.length === 0 ? (
            <div className="text-center py-8 text-gray-500">No activities on this day</div>
          ) : (
            <div className="space-y-3">
              {selectedActivities.map((a) => {
                const statusColors = {
                  UPCOMING: 'bg-green-100 text-green-700',
                  ONGOING: 'bg-blue-100 text-blue-700',
                  COMPLETED: 'bg-gray-100 text-gray-500',
                  CANCELLED: 'bg-red-100 text-red-700',
                };

                return (
                  <Link key={a.id} to={`/activities/${a.id}`}
                    className="card flex items-start justify-between hover:shadow-lg transition-shadow duration-200">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <h4 className="text-senior-base font-semibold text-gray-900">{a.title}</h4>
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColors[a.status] || ''}`}>
                          {a.status}
                        </span>
                      </div>
                      <div className="flex items-center gap-4 text-sm text-gray-500">
                        <span>🕐 {formatTime(a.dateTime)}</span>
                        {a.duration && <span>⏱ {a.duration}min</span>}
                        {a.location && <span>📍 {a.location}</span>}
                        {a.locality && <span>{a.locality}</span>}
                      </div>
                      <div className="text-sm text-gray-400 mt-1">
                        👤 {a.creatorName} &middot; {a.participantCount}/{a.maxParticipants || '∞'} joined
                      </div>
                    </div>
                  </Link>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
