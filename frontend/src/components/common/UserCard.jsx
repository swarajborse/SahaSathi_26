import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import api from '../../services/api';

export default function UserCard({ user }) {
  const { user: currentUser } = useAuth();
  const navigate = useNavigate();
  const pictureSrc = user.profilePictureUrl
    ? `/api/v1/files/${user.profilePictureUrl}`
    : null;

  async function startChat() {
    try {
      const res = await api.post('/chat/conversations', null, {
        params: { userId: currentUser.id, otherUserId: user.id }
      });
      navigate(`/chat/${res.data.data.id}`);
    } catch {
      navigate('/chat');
    }
  }

  return (
    <div className="card hover:shadow-lg transition-shadow duration-200">
      <div className="flex items-start gap-4">
        <Link to={`/users/${user.id}`}>
          {pictureSrc ? (
            <img
              src={pictureSrc}
              alt={user.name}
              className="w-16 h-16 rounded-full object-cover border-2 border-primary-100"
            />
          ) : (
            <div className="w-16 h-16 rounded-full bg-primary-50 border-2 border-primary-100 flex items-center justify-center">
              <span className="text-senior-lg text-primary-400 font-bold">
                {user.name?.charAt(0)?.toUpperCase() || '?'}
              </span>
            </div>
          )}
        </Link>

        <div className="flex-1 min-w-0">
          <Link to={`/users/${user.id}`} className="hover:text-primary-600">
            <h3 className="text-senior-lg font-semibold text-gray-900 truncate">
              {user.name}
            </h3>
          </Link>
          <div className="flex items-center gap-3 text-sm text-gray-500 mt-1">
            {user.age && <span>{user.age} yrs</span>}
            {user.gender && <span>{user.gender}</span>}
            {user.locality && <span>{user.locality}</span>}
          </div>
          {user.bio && (
            <p className="text-sm text-gray-600 mt-2 line-clamp-2">{user.bio}</p>
          )}
        </div>
      </div>

      {user.mutualInterests && user.mutualInterests.length > 0 && (
        <div className="mt-4 pt-3 border-t border-gray-100">
          <p className="text-xs text-gray-400 mb-2">
            {user.mutualInterestCount} mutual interest{user.mutualInterestCount > 1 ? 's' : ''}
          </p>
          <div className="flex flex-wrap gap-1.5">
            {user.mutualInterests.slice(0, 4).map((interest) => (
              <span
                key={interest.id}
                className="inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-primary-50 text-primary-700"
              >
                {interest.icon && <span className="mr-0.5">{interest.icon}</span>}
                {interest.name}
              </span>
            ))}
            {user.mutualInterests.length > 4 && (
              <span className="text-xs text-gray-400">
                +{user.mutualInterests.length - 4} more
              </span>
            )}
          </div>
        </div>
      )}

      {user.id !== currentUser?.id && (
        <div className="mt-4 pt-3 border-t border-gray-100">
          <button onClick={startChat}
            className="btn-primary text-sm py-2 px-4 w-full">
            Send Message
          </button>
        </div>
      )}
    </div>
  );
}
