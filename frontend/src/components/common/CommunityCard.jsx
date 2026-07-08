import { Link } from 'react-router-dom';

export default function CommunityCard({ community }) {
  return (
    <Link to={`/communities/${community.id}`} className="card hover:shadow-lg transition-shadow duration-200 block">
      <div className="flex items-start justify-between mb-3">
        <div className="flex-1 min-w-0">
          <h3 className="text-senior-lg font-semibold text-gray-900 truncate">
            {community.name}
          </h3>
          <p className="text-sm text-gray-500 mt-1">{community.category}</p>
        </div>
        {community.isPrivate && (
          <span className="text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full">Private</span>
        )}
      </div>

      <div className="space-y-1 text-sm text-gray-600">
        {community.locality && <div>📍 {community.locality}, {community.city}</div>}
        {!community.locality && <div>📍 {community.city}</div>}
      </div>

      <div className="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between text-sm">
        <span className="text-gray-500">👤 {community.creatorName}</span>
        <span className="text-gray-500">
          {community.memberCount}/{community.maxMembers || '∞'} members
          {community.isJoined && <span className="ml-2 text-primary-600 font-medium">✓ Joined</span>}
        </span>
      </div>
    </Link>
  );
}
