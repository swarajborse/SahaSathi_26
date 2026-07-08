import { useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import UserCard from '../components/common/UserCard';

export default function NearbyUsersPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [localityFilter, setLocalityFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchNearby = useCallback(async () => {
    if (!user?.id) return;
    setLoading(true);
    try {
      const params = { page, size: 10 };
      if (localityFilter) params.locality = localityFilter;
      const res = await api.get(`/users/${user.id}/nearby`, { params });
      setUsers(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, [user?.id, localityFilter, page]);

  useEffect(() => {
    fetchNearby();
  }, [fetchNearby]);

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">Nearby Seniors</h1>
        <input
          type="text"
          value={localityFilter}
          onChange={(e) => { setLocalityFilter(e.target.value); setPage(0); }}
          placeholder="Filter by locality..."
          className="input-field max-w-xs"
        />
      </div>

      {user?.city && (
        <p className="text-senior-base text-gray-500 mb-6">
          People in <span className="font-medium text-gray-700">{user.city}</span>
          {user.locality && localityFilter !== user.locality && (
            <> &middot; <button
              onClick={() => { setLocalityFilter(user.locality); setPage(0); }}
              className="text-primary-600 hover:underline"
            >
              Show only {user.locality}
            </button></>
          )}
        </p>
      )}

      {loading ? (
        <div className="text-center text-gray-500 py-12">Loading nearby users...</div>
      ) : users.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-senior-lg text-gray-500">No nearby users found</p>
          <p className="text-sm text-gray-400 mt-2">
            Make sure your profile has a city set. Other users in your area will appear here.
          </p>
        </div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2">
            {users.map((nearbyUser) => (
              <UserCard key={nearbyUser.id} user={nearbyUser} />
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center gap-4 mt-8">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="btn-secondary text-sm py-2 px-4"
              >
                Previous
              </button>
              <span className="flex items-center text-senior-base text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                className="btn-secondary text-sm py-2 px-4"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
