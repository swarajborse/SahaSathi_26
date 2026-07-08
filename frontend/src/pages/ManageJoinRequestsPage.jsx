import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

export default function ManageJoinRequestsPage() {
  const { targetType, targetId } = useParams();
  const { user } = useAuth();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRequests();
  }, [targetType, targetId, user.id]);

  async function fetchRequests() {
    try {
      const res = await api.get('/join-requests/pending', {
        params: { targetType: targetType.toUpperCase(), targetId, userId: user.id }
      });
      setRequests(res.data.data);
    } catch {
      setRequests([]);
    } finally {
      setLoading(false);
    }
  }

  async function handleApprove(requestId) {
    try {
      await api.put(`/join-requests/${requestId}/approve?userId=${user.id}`);
      toast.success('Request approved');
      setRequests((prev) => prev.filter((r) => r.id !== requestId));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to approve');
    }
  }

  async function handleReject(requestId) {
    try {
      await api.put(`/join-requests/${requestId}/reject?userId=${user.id}`);
      toast.success('Request rejected');
      setRequests((prev) => prev.filter((r) => r.id !== requestId));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to reject');
    }
  }

  const backLink = targetType === 'community' ? '/communities' : '/activities';

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <a href={backLink} className="text-primary-600 hover:underline text-sm">&larr; Back</a>
          <h1 className="page-title mt-1">Join Requests</h1>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading...</div>
      ) : requests.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-senior-lg text-gray-500">No pending requests</p>
        </div>
      ) : (
        <div className="space-y-4">
          {requests.map((req) => (
            <div key={req.id} className="card flex items-center justify-between">
              <div className="flex items-center gap-4">
                {req.requester.profilePictureUrl ? (
                  <img src={`/api/v1/files/${req.requester.profilePictureUrl}`} alt=""
                    className="w-12 h-12 rounded-full object-cover" />
                ) : (
                  <div className="w-12 h-12 rounded-full bg-primary-50 flex items-center justify-center">
                    <span className="text-lg font-bold text-primary-500">
                      {req.requester.name?.charAt(0)}
                    </span>
                  </div>
                )}
                <div>
                  <p className="font-medium text-senior-base">{req.requester.name}</p>
                  <p className="text-sm text-gray-500">
                    {req.requester.age ? `${req.requester.age} yrs` : ''}
                  </p>
                  <p className="text-xs text-gray-400">
                    Requested {new Date(req.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
              <div className="flex gap-2">
                <button onClick={() => handleApprove(req.id)}
                  className="btn-primary text-sm py-2 px-4">Approve</button>
                <button onClick={() => handleReject(req.id)}
                  className="btn-danger text-sm py-2 px-4">Reject</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
