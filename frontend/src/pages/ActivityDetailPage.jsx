import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import ReportModal from '../components/common/ReportModal';

export default function ActivityDetailPage() {
  const { activityId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [activity, setActivity] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [requested, setRequested] = useState(false);
  const [showReport, setShowReport] = useState(false);
  const [feedback, setFeedback] = useState([]);
  const [avgRating, setAvgRating] = useState(null);
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [feedbackSubmitted, setFeedbackSubmitted] = useState(false);
  const [feedbackLoading, setFeedbackLoading] = useState(false);

  useEffect(() => {
    async function fetch() {
      try {
        const [actRes, fbRes] = await Promise.all([
          api.get(`/activities/${activityId}`, { params: { userId: user.id } }),
          api.get(`/activities/${activityId}/feedback`)
        ]);
        setActivity(actRes.data.data);
        setFeedback(fbRes.data.data.feedback);
        setAvgRating(fbRes.data.data.averageRating);
      } catch {
        toast.error('Failed to load activity');
        navigate('/activities');
      } finally {
        setLoading(false);
      }
    }
    fetch();
  }, [activityId, user.id, navigate]);

  async function handleFeedbackSubmit(e) {
    e.preventDefault();
    if (rating < 1) return;
    setFeedbackLoading(true);
    try {
      await api.post(`/activities/${activityId}/feedback?userId=${user.id}&rating=${rating}${comment ? `&comment=${encodeURIComponent(comment)}` : ''}`);
      setFeedbackSubmitted(true);
      toast.success('Feedback submitted');
      const fbRes = await api.get(`/activities/${activityId}/feedback`);
      setFeedback(fbRes.data.data.feedback);
      setAvgRating(fbRes.data.data.averageRating);
      setRating(0);
      setComment('');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit feedback');
    } finally {
      setFeedbackLoading(false);
    }
  }

  async function handleJoin() {
    setActionLoading(true);
    try {
      const res = await api.post(`/activities/${activityId}/join?userId=${user.id}`);
      const result = res.data.data;
      if (result.type === 'REQUEST_CREATED') {
        setRequested(true);
        toast.success('Join request sent for approval');
      } else {
        setActivity(result.data);
        toast.success('Joined activity');
      }
    } catch (err) {
      if (err.response?.status === 403) {
        navigate('/verify-age');
        return;
      }
      toast.error('Failed to join');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleLeave() {
    setActionLoading(true);
    try {
      const res = await api.post(`/activities/${activityId}/leave?userId=${user.id}`);
      setActivity(res.data.data);
      toast.success('Left activity');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to leave');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleCancel() {
    if (!confirm('Are you sure you want to cancel this activity?')) return;
    setActionLoading(true);
    try {
      await api.delete(`/activities/${activityId}?userId=${user.id}`);
      toast.success('Activity cancelled');
      navigate('/activities');
    } catch {
      toast.error('Failed to cancel');
    } finally {
      setActionLoading(false);
    }
  }

  if (loading) {
    return <div className="text-center py-12 text-gray-500">Loading...</div>;
  }

  if (!activity) {
    return <div className="text-center py-12 text-gray-500">Activity not found</div>;
  }

  const date = new Date(activity.dateTime);
  const dateStr = date.toLocaleDateString('en-IN', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  });
  const timeStr = date.toLocaleTimeString('en-IN', {
    hour: '2-digit', minute: '2-digit'
  });

  const pictureSrc = (id) => id ? `/api/v1/files/${id}` : null;

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <button onClick={() => navigate('/activities')} className="text-primary-600 hover:underline mb-4 inline-block">
        &larr; Back to Activities
      </button>

      <div className="card">
        <div className="flex items-start justify-between mb-4">
          <div>
            <h1 className="page-title">{activity.title}</h1>
            <p className="text-sm text-gray-500 mt-1">{activity.category}</p>
          </div>
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${
            activity.status === 'UPCOMING' ? 'bg-green-100 text-green-700' :
            activity.status === 'CANCELLED' ? 'bg-red-100 text-red-700' :
            'bg-gray-100 text-gray-500'
          }`}>
            {activity.status}
          </span>
        </div>

        {activity.description && (
          <p className="text-senior-base text-gray-700 mb-6">{activity.description}</p>
        )}

        <div className="grid grid-cols-2 gap-4 text-sm text-gray-600 mb-6">
          <div><span className="font-medium">📅 Date:</span> {dateStr}</div>
          <div><span className="font-medium">⏰ Time:</span> {timeStr}</div>
          {activity.duration && <div><span className="font-medium">⏱ Duration:</span> {activity.duration} min</div>}
          <div><span className="font-medium">📍 Location:</span> {activity.location}</div>
          {activity.locality && <div><span className="font-medium">🏘 Locality:</span> {activity.locality}</div>}
          <div><span className="font-medium">👥 Participants:</span> {activity.participantCount}/{activity.maxParticipants || '∞'}</div>
          {activity.minAge && <div><span className="font-medium">🎂 Min Age:</span> {activity.minAge}+</div>}
        </div>

        <div className="flex gap-3 mb-6 flex-wrap">
          {!activity.isCreator && activity.status === 'UPCOMING' && (
            activity.isJoined
              ? <button onClick={handleLeave} disabled={actionLoading}
                  className="btn-secondary">Leave Activity</button>
              : !requested
                ? <button onClick={handleJoin} disabled={actionLoading}
                    className="btn-primary">Join Activity</button>
                : <button disabled className="btn-secondary opacity-50 cursor-not-allowed">
                    Request Pending</button>
          )}
          {activity.isCreator && activity.status === 'UPCOMING' && (
            <>
              <button onClick={() => navigate(`/activities/${activity.id}/edit`)}
                className="btn-secondary">Edit</button>
              <button onClick={() => navigate(`/join-requests/activity/${activity.id}`)}
                className="btn-secondary">Manage Requests</button>
              <button onClick={handleCancel} disabled={actionLoading}
                className="btn-danger">Cancel Activity</button>
            </>
          )}
          <button onClick={() => setShowReport(true)}
            className="text-sm text-gray-500 hover:text-red-500 ml-auto">
            Report
          </button>
        </div>

        <div className="border-t pt-4">
          <h3 className="font-semibold text-senior-base mb-3">
            Participants ({activity.participantCount})
          </h3>
          <div className="space-y-3">
            {activity.participants?.map((p) => (
              <div key={p.id} className="flex items-center gap-3">
                {p.profilePictureUrl ? (
                  <img src={pictureSrc(p.profilePictureUrl)} alt={p.name}
                    className="w-10 h-10 rounded-full object-cover" />
                ) : (
                  <div className="w-10 h-10 rounded-full bg-primary-50 flex items-center justify-center">
                    <span className="text-sm font-bold text-primary-500">{p.name?.charAt(0)}</span>
                  </div>
                )}
                <div>
                  <p className="font-medium text-sm">{p.name}</p>
                  <p className="text-xs text-gray-500">{p.age ? `${p.age} yrs` : ''} {p.locality ? `· ${p.locality}` : ''}</p>
                </div>
                {p.id === activity.creator?.id && (
                  <span className="ml-auto text-xs text-primary-600 font-medium">Organizer</span>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="border-t pt-6 mt-6">
          <h3 className="font-semibold text-senior-base mb-3">
            Feedback & Ratings
            {avgRating !== null && (
              <span className="ml-2 text-sm text-gray-500 font-normal">
                ({avgRating} avg)
              </span>
            )}
          </h3>

          {activity.status === 'COMPLETED' && !feedbackSubmitted && (
            <form onSubmit={handleFeedbackSubmit} className="mb-4 p-4 bg-gray-50 rounded-lg">
              <p className="text-sm font-medium text-gray-700 mb-2">Rate this activity</p>
              <div className="flex gap-1 mb-3">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button key={star} type="button" onClick={() => setRating(star)}
                    className={`text-senior-xl ${star <= rating ? 'text-yellow-400' : 'text-gray-300'}`}>
                    ★
                  </button>
                ))}
              </div>
              <textarea value={comment} onChange={(e) => setComment(e.target.value)}
                rows={2} maxLength={2000} placeholder="Share your experience (optional)..."
                className="input text-senior-base resize-none mb-3" />
              <button type="submit" disabled={feedbackLoading || rating < 1}
                className="btn-primary text-sm py-2 px-4">
                {feedbackLoading ? 'Submitting...' : 'Submit Feedback'}
              </button>
            </form>
          )}

          {feedback.length > 0 ? (
            <div className="space-y-3">
              {feedback.map((f) => (
                <div key={f.id} className="p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-medium text-sm">{f.user?.name || 'Anonymous'}</span>
                    <span className="text-yellow-400 text-sm">
                      {'★'.repeat(f.rating)}{'☆'.repeat(5 - f.rating)}
                    </span>
                  </div>
                  {f.comment && <p className="text-sm text-gray-600">{f.comment}</p>}
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-400">No feedback yet</p>
          )}
        </div>
      </div>

      <ReportModal
        isOpen={showReport}
        onClose={() => setShowReport(false)}
        targetType="ACTIVITY"
        targetId={parseInt(activityId)}
        userId={user.id}
      />
    </div>
  );
}
