import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import CommunityCard from '../components/common/CommunityCard';

export default function CommunitiesListPage() {
  const { user } = useAuth();
  const [communities, setCommunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchCommunities = useCallback(async () => {
    setLoading(true);
    try {
      const params = { userId: user.id, page, size: 10 };
      if (user?.city) params.city = user.city;
      const res = await api.get('/communities', { params });
      setCommunities(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      setCommunities([]);
    } finally {
      setLoading(false);
    }
  }, [user?.id, user?.city, page]);

  useEffect(() => {
    fetchCommunities();
  }, [fetchCommunities]);

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">Communities</h1>
        <Link to="/communities/create" className="btn-primary text-sm py-2 px-4">
          + Create Community
        </Link>
      </div>

      {user?.city && (
        <p className="text-senior-base text-gray-500 mb-6">
          Communities in <span className="font-medium text-gray-700">{user.city}</span>
        </p>
      )}

      {loading ? (
        <div className="text-center text-gray-500 py-12">Loading communities...</div>
      ) : communities.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-senior-lg text-gray-500">No communities found</p>
          <p className="text-sm text-gray-400 mt-2">
            Be the first to create a community!
          </p>
        </div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2">
            {communities.map((c) => (
              <CommunityCard key={c.id} community={c} />
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
