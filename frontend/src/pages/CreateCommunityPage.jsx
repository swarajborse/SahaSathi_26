import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

export default function CreateCommunityPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: {
      name: '',
      description: '',
      category: 'Walking',
      locality: user?.locality || '',
      city: user?.city || '',
      maxMembers: 50,
      isPrivate: false,
      minAge: '',
    },
  });

  async function onSubmit(data) {
    try {
      const payload = {
        ...data,
        minAge: data.minAge ? parseInt(data.minAge) : null,
        maxMembers: data.maxMembers ? parseInt(data.maxMembers) : null,
      };
      const res = await api.post(`/communities?userId=${user.id}`, payload);
      toast.success('Community created');
      navigate(`/communities/${res.data.data.id}`);
    } catch {
      toast.error('Failed to create community');
    }
  }

  const categories = ['Walking', 'Yoga', 'Gardening', 'Reading', 'Music', 'Cooking',
    'Board Games', 'Card Games', 'Badminton', 'Swimming', 'Dancing', 'Meditation',
    'Photography', 'Travel', 'Volunteering'];

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="card">
        <h1 className="page-title mb-6">Create Community</h1>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">Name *</label>
            <input {...register('name', { required: 'Name is required' })}
              className="input-field" placeholder="Morning Walkers Group" />
            {errors.name && <p className="text-red-500 text-sm mt-1">{errors.name.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Category *</label>
              <select {...register('category', { required: true })} className="input-field">
                {categories.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-1">Max Members</label>
              <input type="number" {...register('maxMembers')} className="input-field" placeholder="50" />
            </div>
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

          <div className="flex items-center gap-3">
            <input type="checkbox" {...register('isPrivate')} id="isPrivate"
              className="w-5 h-5 text-primary-600" />
            <label htmlFor="isPrivate" className="text-senior-base text-gray-700">
              Private community (age-restricted)
            </label>
          </div>

          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-1">
              Minimum Age (for private communities)
            </label>
            <input type="number" {...register('minAge')} className="input-field" placeholder="55" />
          </div>

          <div>
            <textarea {...register('description')} className="input-field" rows={3}
              placeholder="Describe your community..." />
          </div>

          <button type="submit" className="btn-primary w-full">Create Community</button>
        </form>
      </div>
    </div>
  );
}
