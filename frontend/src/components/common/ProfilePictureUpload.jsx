import { useState, useRef } from 'react';
import { toast } from 'react-toastify';
import api from '../../services/api';

export default function ProfilePictureUpload({ userId, currentUrl, onUpload }) {
  const [uploading, setUploading] = useState(false);
  const fileRef = useRef(null);

  const pictureSrc = currentUrl
    ? `/api/v1/files/${currentUrl}`
    : null;

  async function handleFileChange(e) {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)) {
      toast.error('Please select a JPG, PNG, GIF, or WEBP image');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      toast.error('File size must be less than 5MB');
      return;
    }

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await api.post(`/users/${userId}/profile-picture`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      onUpload(res.data.data.profilePictureUrl);
      toast.success('Profile picture updated');
    } catch {
      toast.error('Failed to upload picture');
    } finally {
      setUploading(false);
    }
  }

  return (
    <div className="flex flex-col items-center gap-4">
      <div className="relative">
        {pictureSrc ? (
          <img
            src={pictureSrc}
            alt="Profile"
            className="w-28 h-28 rounded-full object-cover border-4 border-primary-100"
          />
        ) : (
          <div className="w-28 h-28 rounded-full bg-primary-50 border-4 border-primary-100 flex items-center justify-center">
            <span className="text-senior-2xl text-primary-400 font-bold">?</span>
          </div>
        )}
        <button
          type="button"
          onClick={() => fileRef.current?.click()}
          disabled={uploading}
          className="absolute bottom-0 right-0 bg-primary-600 text-white rounded-full w-8 h-8 flex items-center justify-center text-lg shadow-md hover:bg-primary-700"
        >
          +
        </button>
      </div>
      <input
        ref={fileRef}
        type="file"
        accept="image/jpeg,image/png,image/gif,image/webp"
        onChange={handleFileChange}
        className="hidden"
      />
      {uploading && <p className="text-sm text-gray-500">Uploading...</p>}
      <p className="text-sm text-gray-400">Click + to upload photo</p>
    </div>
  );
}
