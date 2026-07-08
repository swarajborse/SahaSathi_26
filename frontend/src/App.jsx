import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import MainLayout from './layouts/MainLayout';
import ProtectedRoute from './components/common/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import NearbyUsersPage from './pages/NearbyUsersPage';
import ActivitiesListPage from './pages/ActivitiesListPage';
import ActivityDetailPage from './pages/ActivityDetailPage';
import CreateActivityPage from './pages/CreateActivityPage';
import MyActivitiesPage from './pages/MyActivitiesPage';
import CommunitiesListPage from './pages/CommunitiesListPage';
import CommunityDetailPage from './pages/CommunityDetailPage';
import CreateCommunityPage from './pages/CreateCommunityPage';
import MyCommunitiesPage from './pages/MyCommunitiesPage';
import ManageJoinRequestsPage from './pages/ManageJoinRequestsPage';
import AgeVerificationPage from './pages/AgeVerificationPage';
import NotificationsPage from './pages/NotificationsPage';
import SearchPage from './pages/SearchPage';
import CalendarPage from './pages/CalendarPage';
import ChatPage from './pages/ChatPage';

function Home() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center px-4">
      <h1 className="text-senior-2xl font-bold text-gray-900 mb-4">
        Welcome to Sahasathi
      </h1>
      <p className="text-senior-lg text-gray-600 max-w-2xl mb-8">
        Connecting senior citizens with nearby like-minded people through
        activities, communities, and local events.
      </p>
      <div className="flex gap-4">
        <button className="btn-primary">Get Started</button>
        <button className="btn-secondary">Learn More</button>
      </div>
    </div>
  );
}

function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh]">
      <h1 className="text-senior-2xl font-bold text-gray-900">404</h1>
      <p className="text-senior-lg text-gray-600">Page not found</p>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<MainLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/register"
            element={
              <ProtectedRoute>
                <RegisterPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/nearby"
            element={
              <ProtectedRoute>
                <NearbyUsersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/activities"
            element={
              <ProtectedRoute>
                <ActivitiesListPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/activities/create"
            element={
              <ProtectedRoute>
                <CreateActivityPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/activities/:activityId"
            element={
              <ProtectedRoute>
                <ActivityDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-activities"
            element={
              <ProtectedRoute>
                <MyActivitiesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/communities"
            element={
              <ProtectedRoute>
                <CommunitiesListPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/communities/create"
            element={
              <ProtectedRoute>
                <CreateCommunityPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/communities/:communityId"
            element={
              <ProtectedRoute>
                <CommunityDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-communities"
            element={
              <ProtectedRoute>
                <MyCommunitiesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/join-requests/:targetType/:targetId"
            element={
              <ProtectedRoute>
                <ManageJoinRequestsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/verify-age"
            element={
              <ProtectedRoute>
                <AgeVerificationPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <NotificationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/search"
            element={
              <ProtectedRoute>
                <SearchPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/calendar"
            element={
              <ProtectedRoute>
                <CalendarPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/chat"
            element={
              <ProtectedRoute>
                <ChatPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/chat/:conversationId"
            element={
              <ProtectedRoute>
                <ChatPage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
