import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

export default function NotificationsPage() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchNotifications = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get('/notifications', {
        params: { userId: user.id, page, size: 20 }
      });
      setNotifications(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  }, [user.id, page]);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  async function markAsRead(id) {
    try {
      await api.put(`/notifications/${id}/read?userId=${user.id}`);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch {}
  }

  async function markAllAsRead() {
    try {
      await api.put(`/notifications/read-all?userId=${user.id}`);
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch {}
  }

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">Notifications</h1>
        {unreadCount > 0 && (
          <button onClick={markAllAsRead} className="text-sm text-primary-600 hover:underline">
            Mark all as read
          </button>
        )}
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading...</div>
      ) : notifications.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-senior-lg text-gray-500">No notifications yet</p>
        </div>
      ) : (
        <>
          <div className="space-y-2">
            {notifications.map((n) => {
              const detailLink = n.relatedType === 'ACTIVITY'
                ? `/activities/${n.relatedId}`
                : `/communities/${n.relatedId}`;

              return (
                <Link
                  key={n.id}
                  to={detailLink}
                  onClick={() => !n.read && markAsRead(n.id)}
                  className={`card flex items-start gap-4 hover:shadow-md transition-shadow ${
                    !n.read ? 'border-l-4 border-primary-500 bg-primary-50/50' : ''
                  }`}
                >
                  <div className="flex-1 min-w-0">
                    <p className={`text-senior-base ${!n.read ? 'font-semibold' : ''}`}>
                      {n.title}
                    </p>
                    <p className="text-sm text-gray-600 mt-1">{n.message}</p>
                    <p className="text-xs text-gray-400 mt-2">
                      {new Date(n.createdAt).toLocaleDateString('en-IN', {
                        day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
                      })}
                    </p>
                  </div>
                  {!n.read && (
                    <span className="w-3 h-3 rounded-full bg-primary-500 mt-2 flex-shrink-0" />
                  )}
                </Link>
              );
            })}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center gap-4 mt-8">
              <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}
                className="btn-secondary text-sm py-2 px-4">Previous</button>
              <span className="flex items-center text-senior-base text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                className="btn-secondary text-sm py-2 px-4">Next</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
