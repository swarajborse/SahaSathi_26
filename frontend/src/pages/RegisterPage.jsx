import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

export default function RegisterPage() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      name: user?.name || '',
    },
  });

  async function onSubmit(data) {
    if (!user?.id) {
      toast.error('Please login first');
      navigate('/login');
      return;
    }
    setLoading(true);
    try {
      const res = await api.put(`/users/${user.id}/profile`, data);
      updateUser(res.data.data);
      toast.success('Profile created successfully');
      navigate('/');
    } catch {
      toast.error('Failed to save profile');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center px-4 py-8">
      <div className="card max-w-lg w-full">
        <h1 className="page-title text-center mb-2">Complete Your Profile</h1>
        <p className="text-senior-base text-gray-600 text-center mb-8">
          Tell us a bit about yourself
        </p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">
              Full Name *
            </label>
            <input
              {...register('name', { required: 'Name is required' })}
              className="input-field"
              placeholder="Your full name"
            />
            {errors.name && (
              <p className="text-red-500 text-sm mt-1">{errors.name.message}</p>
            )}
          </div>

          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">
              Email (optional)
            </label>
            <input
              type="email"
              {...register('email')}
              className="input-field"
              placeholder="email@example.com"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">
                Date of Birth *
              </label>
              <input
                type="date"
                {...register('dateOfBirth', { required: 'Date of birth is required' })}
                className="input-field"
              />
              {errors.dateOfBirth && (
                <p className="text-red-500 text-sm mt-1">{errors.dateOfBirth.message}</p>
              )}
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">
                Gender *
              </label>
              <select
                {...register('gender', { required: 'Gender is required' })}
                className="input-field"
              >
                <option value="">Select</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
              {errors.gender && (
                <p className="text-red-500 text-sm mt-1">{errors.gender.message}</p>
              )}
            </div>
          </div>

          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">
              Locality / Area
            </label>
            <input
              {...register('locality')}
              className="input-field"
              placeholder="e.g., Koramangala"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">
                City
              </label>
              <input
                {...register('city')}
                className="input-field"
                placeholder="e.g., Bangalore"
              />
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">
                State
              </label>
              <input
                {...register('state')}
                className="input-field"
                placeholder="e.g., Karnataka"
              />
            </div>
          </div>

          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">
              Bio (optional)
            </label>
            <textarea
              {...register('bio')}
              className="input-field"
              rows={3}
              placeholder="Tell others about your interests..."
            />
          </div>

          <button type="submit" disabled={loading} className="btn-primary w-full mt-6">
            {loading ? 'Saving...' : 'Save Profile'}
          </button>
        </form>
      </div>
    </div>
  );
}
