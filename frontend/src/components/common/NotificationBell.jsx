import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';
import useAuth from '../../hooks/useAuth';

export default function NotificationBell() {
  const { user } = useAuth();
  const [count, setCount] = useState(0);

  const fetchCount = useCallback(async () => {
    if (!user?.id) return;
    try {
      const res = await api.get('/notifications/unread-count', {
        params: { userId: user.id }
      });
      setCount(res.data.data.count);
    } catch {
      setCount(0);
    }
  }, [user?.id]);

  useEffect(() => {
    fetchCount();
    const interval = setInterval(fetchCount, 30000);
    return () => clearInterval(interval);
  }, [fetchCount]);

  return (
    <Link to="/notifications" className="relative">
      <span className="text-senior-lg">🔔</span>
      {count > 0 && (
        <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
          {count > 9 ? '9+' : count}
        </span>
      )}
    </Link>
  );
}
