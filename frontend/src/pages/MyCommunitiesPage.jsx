import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import CommunityCard from '../components/common/CommunityCard';

export default function MyCommunitiesPage() {
  const { user } = useAuth();
  const [communities, setCommunities] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetch() {
      try {
        const res = await api.get(`/communities/user/${user.id}`);
        setCommunities(res.data.data);
      } catch {
        setCommunities([]);
      } finally {
        setLoading(false);
      }
    }
    fetch();
  }, [user.id]);

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">My Communities</h1>
        <Link to="/communities/create" className="btn-primary text-sm py-2 px-4">
          + Create Community
        </Link>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading...</div>
      ) : communities.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-senior-lg text-gray-500">
            You haven't joined any communities yet
          </p>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {communities.map((c) => (
            <CommunityCard key={c.id} community={c} />
          ))}
        </div>
      )}
    </div>
  );
}
