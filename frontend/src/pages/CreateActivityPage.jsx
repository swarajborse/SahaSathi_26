import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

export default function CreateActivityPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      title: '',
      description: '',
      category: 'Walking',
      dateTime: '',
      duration: 60,
      location: '',
      locality: user?.locality || '',
      city: user?.city || '',
      maxParticipants: 20,
      isPrivate: false,
      minAge: '',
    },
  });

  async function onSubmit(data) {
    setLoading(true);
    try {
      const payload = {
        ...data,
        dateTime: new Date(data.dateTime).toISOString(),
        minAge: data.minAge ? parseInt(data.minAge) : null,
        maxParticipants: data.maxParticipants ? parseInt(data.maxParticipants) : null,
        duration: data.duration ? parseInt(data.duration) : null,
      };
      const res = await api.post(`/activities?userId=${user.id}`, payload);
      toast.success('Activity created');
      navigate(`/activities/${res.data.data.id}`);
    } catch {
      toast.error('Failed to create activity');
    } finally {
      setLoading(false);
    }
  }

  const categories = ['Walking', 'Yoga', 'Gardening', 'Reading', 'Music', 'Cooking',
    'Board Games', 'Card Games', 'Badminton', 'Swimming', 'Dancing', 'Meditation',
    'Photography', 'Travel', 'Volunteering'];

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="card">
        <h1 className="page-title mb-6">Create Activity</h1>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">Title *</label>
            <input {...register('title', { required: 'Title is required' })}
              className="input-field" placeholder="Morning Walk at Cubbon Park" />
            {errors.title && <p className="text-red-500 text-sm mt-1">{errors.title.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Category *</label>
              <select {...register('category', { required: true })} className="input-field">
                {categories.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Duration (min)</label>
              <input type="number" {...register('duration')} className="input-field" placeholder="60" />
            </div>
          </div>

          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">Date & Time *</label>
            <input type="datetime-local"
              {...register('dateTime', { required: 'Date and time is required' })}
              className="input-field" />
            {errors.dateTime && <p className="text-red-500 text-sm mt-1">{errors.dateTime.message}</p>}
          </div>

          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">Location *</label>
            <input {...register('location', { required: 'Location is required' })}
              className="input-field" placeholder="Cubbon Park, Main Gate" />
            {errors.location && <p className="text-red-500 text-sm mt-1">{errors.location.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Locality</label>
              <input {...register('locality')} className="input-field" placeholder="Koramangala" />
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">City *</label>
              <input {...register('city', { required: 'City is required' })}
                className="input-field" placeholder="Bangalore" />
              {errors.city && <p className="text-red-500 text-sm mt-1">{errors.city.message}</p>}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Max Participants</label>
              <input type="number" {...register('maxParticipants')}
                className="input-field" placeholder="20" />
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Min Age (optional)</label>
              <input type="number" {...register('minAge')}
                className="input-field" placeholder="55" />
            </div>
          </div>

          <div>
            <textarea {...register('description')} className="input-field" rows={3}
              placeholder="Describe your activity..." />
          </div>

          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? 'Creating...' : 'Create Activity'}
          </button>
        </form>
      </div>
    </div>
  );
}
