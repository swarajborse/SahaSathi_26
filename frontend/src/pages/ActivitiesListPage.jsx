import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import ActivityCard from '../components/common/ActivityCard';

export default function ActivitiesListPage() {
  const { user } = useAuth();
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchActivities = useCallback(async () => {
    if (!user?.city) return;
    setLoading(true);
    try {
      const params = { city: user.city, userId: user.id, page, size: 10 };
      if (category) params.category = category;
      if (user.locality) params.locality = user.locality;
      const res = await api.get('/activities', { params });
      setActivities(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      setActivities([]);
    } finally {
      setLoading(false);
    }
  }, [user?.city, user?.id, user?.locality, category, page]);

  useEffect(() => {
    fetchActivities();
  }, [fetchActivities]);

  const categories = ['Walking', 'Yoga', 'Gardening', 'Reading', 'Music', 'Cooking',
    'Board Games', 'Card Games', 'Badminton', 'Swimming', 'Dancing', 'Meditation'];

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">Activities</h1>
        <Link to="/activities/create" className="btn-primary text-sm py-2 px-4">
          + Create Activity
        </Link>
      </div>

      <div className="flex gap-3 mb-6 overflow-x-auto pb-2">
        <button
          onClick={() => { setCategory(''); setPage(0); }}
          className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap
            ${!category ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
        >
          All
        </button>
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => { setCategory(cat); setPage(0); }}
            className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap
              ${category === cat ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
          >
            {cat}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="text-center text-gray-500 py-12">Loading activities...</div>
      ) : activities.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-senior-lg text-gray-500">No activities found in {user?.city}</p>
          <p className="text-sm text-gray-400 mt-2">
            Be the first to create an activity!
          </p>
        </div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2">
            {activities.map((a) => (
              <ActivityCard key={a.id} activity={a} />
            ))}
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
