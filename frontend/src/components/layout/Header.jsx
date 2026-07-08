import { Link } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import NotificationBell from '../common/NotificationBell';

export default function Header() {
  const { user, logout } = useAuth();

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <nav className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
        <Link to="/" className="text-senior-xl font-bold text-primary-700">
          Sahasathi
        </Link>

        {user && (
          <div className="flex items-center gap-6">
            <Link
              to="/chat"
              className="text-senior-base text-gray-600 hover:text-primary-600 font-medium"
            >
              Messages
            </Link>
            <Link
              to="/calendar"
              className="text-senior-base text-gray-600 hover:text-primary-600 font-medium"
            >
              Calendar
            </Link>
            <Link
              to="/search"
              className="text-senior-base text-gray-600 hover:text-primary-600 font-medium"
            >
              Search
            </Link>
            <Link
              to="/nearby"
              className="text-senior-base text-gray-600 hover:text-primary-600 font-medium"
            >
              Nearby
            </Link>
            <Link
              to="/activities"
              className="text-senior-base text-gray-600 hover:text-primary-600 font-medium"
            >
              Activities
            </Link>
            <Link
              to="/communities"
              className="text-senior-base text-gray-600 hover:text-primary-600 font-medium"
            >
              Communities
            </Link>
          </div>
        )}

        <div className="flex items-center gap-4">
          {user ? (
            <>
              <NotificationBell />
              <Link to="/profile" className="text-senior-base text-gray-700 hover:text-primary-600 font-medium">
                {user.name}
              </Link>
              <button onClick={logout} className="btn-secondary text-sm py-2 px-4">
                Logout
              </button>
            </>
          ) : (
            <Link to="/login" className="btn-secondary text-sm py-2 px-4">
              Login
            </Link>
          )}
        </div>
      </nav>
    </header>
  );
}
