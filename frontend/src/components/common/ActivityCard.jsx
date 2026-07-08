import { Link } from 'react-router-dom';

export default function ActivityCard({ activity }) {
  const date = new Date(activity.dateTime);
  const dateStr = date.toLocaleDateString('en-IN', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
  });
  const timeStr = date.toLocaleTimeString('en-IN', {
    hour: '2-digit', minute: '2-digit'
  });

  const statusColors = {
    UPCOMING: 'bg-green-100 text-green-700',
    ONGOING: 'bg-blue-100 text-blue-700',
    COMPLETED: 'bg-gray-100 text-gray-500',
    CANCELLED: 'bg-red-100 text-red-700',
  };

  return (
    <Link to={`/activities/${activity.id}`} className="card hover:shadow-lg transition-shadow duration-200 block">
      <div className="flex items-start justify-between mb-3">
        <div>
          <h3 className="text-senior-lg font-semibold text-gray-900">{activity.title}</h3>
          <p className="text-sm text-gray-500 mt-1">{activity.category}</p>
        </div>
        <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusColors[activity.status] || ''}`}>
          {activity.status}
        </span>
      </div>

      <div className="space-y-2 text-sm text-gray-600">
        <div className="flex items-center gap-2">
          <span>📅</span>
          <span>{dateStr} at {timeStr}</span>
        </div>
        <div className="flex items-center gap-2">
          <span>📍</span>
          <span>{activity.location}{activity.locality ? `, ${activity.locality}` : ''}</span>
        </div>
        {activity.duration && (
          <div className="flex items-center gap-2">
            <span>⏱</span>
            <span>{activity.duration} min</span>
          </div>
        )}
      </div>

      <div className="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between text-sm">
        <span className="text-gray-500">
          👤 {activity.creatorName}
        </span>
        <span className="text-gray-500">
          {activity.participantCount}/{activity.maxParticipants || '∞'} joined
          {activity.isJoined && <span className="ml-2 text-primary-600 font-medium">✓ Joined</span>}
        </span>
      </div>
    </Link>
  );
}
