import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import ReportModal from '../components/common/ReportModal';

export default function CommunityDetailPage() {
  const { communityId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [community, setCommunity] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [requested, setRequested] = useState(false);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [showReport, setShowReport] = useState(false);

  useEffect(() => {
    async function fetch() {
      try {
        const res = await api.get(`/communities/${communityId}`, {
          params: { userId: user.id }
        });
        setCommunity(res.data.data);
      } catch {
        toast.error('Failed to load community');
        navigate('/communities');
      } finally {
        setLoading(false);
      }
    }
    fetch();
  }, [communityId, user.id, navigate]);

  async function handleJoin() {
    setActionLoading(true);
    try {
      const res = await api.post(`/communities/${communityId}/join?userId=${user.id}`);
      const result = res.data.data;
      if (result.type === 'REQUEST_CREATED') {
        setRequested(true);
        toast.success('Join request sent for approval');
      } else {
        setCommunity(result.data);
        toast.success('Joined community');
      }
    } catch (err) {
      if (err.response?.status === 403) {
        navigate('/verify-age');
        return;
      }
      toast.error(err.response?.data?.message || 'Failed to join');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleLeave() {
    setActionLoading(true);
    try {
      const res = await api.post(`/communities/${communityId}/leave?userId=${user.id}`);
      setCommunity(res.data.data);
      toast.success('Left community');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to leave');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDelete() {
    if (!confirm('Delete this community? This cannot be undone.')) return;
    setActionLoading(true);
    try {
      await api.delete(`/communities/${communityId}?userId=${user.id}`);
      toast.success('Community deleted');
      navigate('/communities');
    } catch {
      toast.error('Failed to delete');
    } finally {
      setActionLoading(false);
    }
  }

  if (loading) {
    return <div className="text-center py-12 text-gray-500">Loading...</div>;
  }

  if (!community) {
    return <div className="text-center py-12 text-gray-500">Community not found</div>;
  }

  const pictureSrc = (url) => url ? `/api/v1/files/${url}` : null;

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <button onClick={() => navigate('/communities')}
        className="text-primary-600 hover:underline mb-4 inline-block">
        &larr; Back to Communities
      </button>

      <div className="card">
        <div className="flex items-start justify-between mb-4">
          <div>
            <h1 className="page-title">{community.name}</h1>
            <p className="text-sm text-gray-500 mt-1">{community.category}</p>
          </div>
          {community.isPrivate && (
            <span className="px-3 py-1 rounded-full text-sm font-medium bg-yellow-100 text-yellow-700">Private</span>
          )}
        </div>

        {community.description && (
          <p className="text-senior-base text-gray-700 mb-6">{community.description}</p>
        )}

        <div className="grid grid-cols-2 gap-4 text-sm text-gray-600 mb-6">
          {community.locality && <div><span className="font-medium">📍 Locality:</span> {community.locality}</div>}
          <div><span className="font-medium">🏙 City:</span> {community.city}</div>
          <div><span className="font-medium">👥 Members:</span> {community.memberCount}/{community.maxMembers || '∞'}</div>
          {community.minAge && <div><span className="font-medium">🎂 Min Age:</span> {community.minAge}+</div>}
        </div>

        <div className="flex gap-3 mb-6">
          {!community.isJoined && !requested && (
            <button onClick={handleJoin} disabled={actionLoading}
              className="btn-primary">Join Community</button>
          )}
          {!community.isJoined && requested && (
            <button disabled className="btn-secondary opacity-50 cursor-not-allowed">
              Request Pending
            </button>
          )}
          {community.isJoined && !community.isOrganizer && (
            <button onClick={handleLeave} disabled={actionLoading}
              className="btn-secondary">Leave Community</button>
          )}
          {community.isOrganizer && (
            <>
              <button onClick={() => navigate(`/communities/${community.id}/edit`)}
                className="btn-secondary">Edit</button>
              <button onClick={() => navigate(`/join-requests/community/${community.id}`)}
                className="btn-secondary">Manage Requests</button>
              <button onClick={handleDelete} disabled={actionLoading}
                className="btn-danger">Delete Community</button>
            </>
          )}
          <button onClick={() => setShowReport(true)}
            className="text-sm text-gray-500 hover:text-red-500 ml-auto">
            Report
          </button>
        </div>

        <div className="border-t pt-4">
          <h3 className="font-semibold text-senior-base mb-3">
            Members ({community.memberCount})
          </h3>
          <div className="space-y-3">
            {community.members?.map((m) => (
              <div key={m.id} className="flex items-center gap-3">
                {m.profilePictureUrl ? (
                  <img src={pictureSrc(m.profilePictureUrl)} alt={m.name}
                    className="w-10 h-10 rounded-full object-cover" />
                ) : (
                  <div className="w-10 h-10 rounded-full bg-primary-50 flex items-center justify-center">
                    <span className="text-sm font-bold text-primary-500">{m.name?.charAt(0)}</span>
                  </div>
                )}
                <div>
                  <p className="font-medium text-sm">{m.name}</p>
                  <p className="text-xs text-gray-500">{m.age ? `${m.age} yrs` : ''} {m.locality ? `· ${m.locality}` : ''}</p>
                </div>
                {m.id === community.creator?.id && (
                  <span className="ml-auto text-xs text-primary-600 font-medium">Organizer</span>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      <ReportModal
        isOpen={showReport}
        onClose={() => setShowReport(false)}
        targetType="COMMUNITY"
        targetId={parseInt(communityId)}
        userId={user.id}
      />
    </div>
  );
}
