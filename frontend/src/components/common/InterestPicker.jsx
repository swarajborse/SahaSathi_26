import { useState, useEffect } from 'react';
import api from '../../services/api';

export default function InterestPicker({ userId, selectedIds, onUpdate }) {
  const [allInterests, setAllInterests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchInterests() {
      try {
        const res = await api.get('/interests');
        setAllInterests(res.data.data);
      } catch {
        // toast handled by interceptor
      } finally {
        setLoading(false);
      }
    }
    fetchInterests();
  }, []);

  async function toggleInterest(interestId) {
    const newIds = selectedIds.includes(interestId)
      ? selectedIds.filter((id) => id !== interestId)
      : [...selectedIds, interestId];

    if (newIds.length === 0) return;

    try {
      const res = await api.put(`/users/${userId}/interests`, { interestIds: newIds });
      onUpdate(res.data.data.map((i) => i.id));
    } catch {
      // toast handled by interceptor
    }
  }

  if (loading) {
    return <div className="text-gray-500">Loading interests...</div>;
  }

  return (
    <div className="flex flex-wrap gap-3">
      {allInterests.map((interest) => {
        const selected = selectedIds.includes(interest.id);
        return (
          <button
            key={interest.id}
            type="button"
            onClick={() => toggleInterest(interest.id)}
            className={`px-4 py-2 rounded-full border-2 text-senior-base transition-all duration-200
              ${selected
                ? 'bg-primary-100 border-primary-500 text-primary-700 font-medium'
                : 'bg-white border-gray-300 text-gray-600 hover:border-primary-300'
              }`}
          >
            {interest.icon && <span className="mr-1">{interest.icon}</span>}
            {interest.name}
          </button>
        );
      })}
    </div>
  );
}
