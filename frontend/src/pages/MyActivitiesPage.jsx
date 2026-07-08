import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import ActivityCard from '../components/common/ActivityCard';

export default function MyActivitiesPage() {
  const { user } = useAuth();
  const [joined, setJoined] = useState([]);
  const [managed, setManaged] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('joined');

  useEffect(() => {
    async function fetch() {
      setLoading(true);
      try {
        const [joinedRes, managedRes] = await Promise.all([
          api.get(`/activities/user/${user.id}`),
          api.get(`/activities/user/${user.id}/managed`, { params: { page: 0, size: 50 } }),
        ]);
        setJoined(joinedRes.data.data);
        setManaged(managedRes.data.data.content);
      } catch {
        setJoined([]);
        setManaged([]);
      } finally {
        setLoading(false);
      }
    }
    fetch();
  }, [user.id]);

  const activities = tab === 'joined' ? joined : managed;

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">My Activities</h1>
        <Link to="/activities/create" className="btn-primary text-sm py-2 px-4">
          + Create Activity
        </Link>
      </div>

      <div className="flex gap-4 mb-6">
        <button
          onClick={() => setTab('joined')}
          className={`text-senior-base font-medium pb-2 border-b-2 transition-colors
            ${tab === 'joined' ? 'border-primary-600 text-primary-700' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
        >
          Joined ({joined.length})
        </button>
        <button
          onClick={() => setTab('managed')}
          className={`text-senior-base font-medium pb-2 border-b-2 transition-colors
            ${tab === 'managed' ? 'border-primary-600 text-primary-700' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
        >
          My Activities ({managed.length})
        </button>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading...</div>
      ) : activities.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-senior-lg text-gray-500">
            {tab === 'joined' ? 'You haven\'t joined any activities yet' : 'You haven\'t created any activities yet'}
          </p>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {activities.map((a) => (
            <ActivityCard key={a.id} activity={a} />
          ))}
        </div>
      )}
    </div>
  );
}
