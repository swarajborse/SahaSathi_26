import { useState, useEffect, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import InterestPicker from '../components/common/InterestPicker';
import ProfilePictureUpload from '../components/common/ProfilePictureUpload';

export default function ProfilePage() {
  const { user, updateUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const [completeness, setCompleteness] = useState(null);
  const [userInterests, setUserInterests] = useState([]);

  const fetchProfileData = useCallback(async () => {
    if (!user?.id) return;
    try {
      const [compRes, intRes] = await Promise.all([
        api.get(`/users/${user.id}/profile-completeness`),
        api.get(`/users/${user.id}/interests`),
      ]);
      setCompleteness(compRes.data.data);
      setUserInterests(intRes.data.data.map((i) => i.id));
    } catch {
      // handled by interceptor
    }
  }, [user?.id]);

  useEffect(() => {
    fetchProfileData();
  }, [fetchProfileData]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      name: user?.name || '',
      email: user?.email || '',
      dateOfBirth: user?.dateOfBirth || '',
      gender: user?.gender || '',
      bio: user?.bio || '',
      locality: user?.locality || '',
      city: user?.city || '',
      state: user?.state || '',
    },
  });

  async function onSubmit(data) {
    setLoading(true);
    try {
      const res = await api.put(`/users/${user.id}/profile`, data);
      updateUser(res.data.data);
      toast.success('Profile updated');
      setEditing(false);
      fetchProfileData();
    } catch {
      toast.error('Failed to update profile');
    } finally {
      setLoading(false);
    }
  }

  async function handlePictureUpload(url) {
    updateUser({ ...user, profilePictureUrl: url });
    fetchProfileData();
  }

  const completenessColor = completeness?.percentage >= 80 ? 'bg-green-500'
    : completeness?.percentage >= 50 ? 'bg-yellow-500'
    : 'bg-red-500';

  if (!editing) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8 space-y-6">
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h1 className="page-title">My Profile</h1>
            <button onClick={() => setEditing(true)} className="btn-primary text-sm py-2 px-4">
              Edit
            </button>
          </div>

          <ProfilePictureUpload
            userId={user?.id}
            currentUrl={user?.profilePictureUrl}
            onUpload={handlePictureUpload}
          />

          {completeness && (
            <div className="mt-6">
              <div className="flex justify-between text-sm text-gray-600 mb-1">
                <span>Profile Completeness</span>
                <span>{completeness.percentage}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-3">
                <div
                  className={`h-3 rounded-full transition-all duration-500 ${completenessColor}`}
                  style={{ width: `${completeness.percentage}%` }}
                />
              </div>
            </div>
          )}

          <div className="mt-6 space-y-4 text-senior-base">
            <div><span className="font-medium text-gray-500">Name:</span> {user?.name}</div>
            <div><span className="font-medium text-gray-500">Phone:</span> {user?.phoneNumber}</div>
            {user?.email && <div><span className="font-medium text-gray-500">Email:</span> {user.email}</div>}
            {user?.age && <div><span className="font-medium text-gray-500">Age:</span> {user.age}</div>}
            {user?.gender && <div><span className="font-medium text-gray-500">Gender:</span> {user.gender}</div>}
            {user?.locality && <div><span className="font-medium text-gray-500">Locality:</span> {user.locality}</div>}
            {user?.city && <div><span className="font-medium text-gray-500">City:</span> {user.city}</div>}
            {user?.state && <div><span className="font-medium text-gray-500">State:</span> {user.state}</div>}
            {user?.bio && <div><span className="font-medium text-gray-500">Bio:</span> {user.bio}</div>}
          </div>

          <div className="mt-6">
            <h3 className="section-title mb-3">Interests</h3>
            <InterestPicker
              userId={user?.id}
              selectedIds={userInterests}
              onUpdate={setUserInterests}
            />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="card">
        <h1 className="page-title mb-6">Edit Profile</h1>

        <ProfilePictureUpload
          userId={user?.id}
          currentUrl={user?.profilePictureUrl}
          onUpload={handlePictureUpload}
        />

        <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-5">
          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">Full Name *</label>
            <input {...register('name', { required: true })} className="input-field" />
            {errors.name && <p className="text-red-500 text-sm mt-1">Name is required</p>}
          </div>
          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">Email</label>
            <input type="email" {...register('email')} className="input-field" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Date of Birth</label>
              <input type="date" {...register('dateOfBirth')} className="input-field" />
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Gender</label>
              <select {...register('gender')} className="input-field">
                <option value="">Select</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>
          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">Bio</label>
            <textarea {...register('bio')} className="input-field" rows={3} />
          </div>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Locality</label>
              <input {...register('locality')} className="input-field" />
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">City</label>
              <input {...register('city')} className="input-field" />
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">State</label>
              <input {...register('state')} className="input-field" />
            </div>
          </div>

          <div>
            <h3 className="section-title mb-3">Interests</h3>
            <InterestPicker
              userId={user?.id}
              selectedIds={userInterests}
              onUpdate={setUserInterests}
            />
          </div>

          <div className="flex gap-4 pt-4">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Saving...' : 'Save Profile'}
            </button>
            <button type="button" onClick={() => setEditing(false)} className="btn-secondary">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
