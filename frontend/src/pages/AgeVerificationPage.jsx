import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

export default function AgeVerificationPage() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [dob, setDob] = useState(user?.dateOfBirth || '');
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const fileRef = useRef(null);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!file) {
      toast.error('Please upload your Aadhaar image');
      return;
    }
    if (!dob) {
      toast.error('Please confirm your date of birth');
      return;
    }

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('aadhaarImage', file);
      formData.append('dob', dob);
      const res = await api.post(`/users/${user.id}/verify-age`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      const data = res.data.data;
      setResult(data);

      if (data.verified) {
        updateUser({ ...user, ageVerified: true, verificationDate: new Date().toISOString() });
        toast.success('Age verified successfully!');
      } else {
        toast.error(data.message);
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Verification failed');
    } finally {
      setUploading(false);
    }
  }

  if (result?.verified) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center px-4">
        <div className="card max-w-md w-full text-center">
          <div className="text-5xl mb-4">✅</div>
          <h1 className="page-title mb-2">Age Verified</h1>
          <p className="text-senior-base text-gray-600 mb-6">{result.message}</p>
          <p className="text-sm text-gray-400 mb-6">
            You can now join senior-only communities and activities.
          </p>
          <button onClick={() => navigate(-1)} className="btn-primary">
            Go Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[60vh] flex items-center justify-center px-4 py-8">
      <div className="card max-w-md w-full">
        <h1 className="page-title text-center mb-2">Age Verification</h1>
        <p className="text-senior-base text-gray-600 text-center mb-6">
          To join senior-only groups, we need to verify your age.
          Upload your Aadhaar card image below.
        </p>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-2">
              Aadhaar Card Image
            </label>
            <div
              onClick={() => fileRef.current?.click()}
              className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center cursor-pointer hover:border-primary-400 transition-colors"
            >
              {file ? (
                <div>
                  <p className="text-senior-base text-primary-600">{file.name}</p>
                  <p className="text-sm text-gray-400 mt-1">
                    {(file.size / 1024 / 1024).toFixed(2)} MB
                  </p>
                </div>
              ) : (
                <div>
                  <p className="text-senior-2xl text-gray-300 mb-2">📄</p>
                  <p className="text-senior-base text-gray-500">
                    Tap to upload Aadhaar image
                  </p>
                  <p className="text-sm text-gray-400 mt-1">
                    JPG, PNG or WEBP (max 10MB)
                  </p>
                </div>
              )}
            </div>
            <input
              ref={fileRef}
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
              className="hidden"
            />
          </div>

          <div>
            <label className="block text-senior-base font-medium text-gray-700 mb-2">
              Confirm Date of Birth
            </label>
            <input
              type="date"
              value={dob}
              onChange={(e) => setDob(e.target.value)}
              className="input-field"
              required
            />
            <p className="text-sm text-gray-400 mt-1">
              Must match the DOB on your Aadhaar card
            </p>
          </div>

          <p className="text-sm text-gray-400 bg-gray-50 p-3 rounded-lg">
            🔒 Your Aadhaar image is processed securely and immediately deleted.
            We only store your verification status.
          </p>

          <button type="submit" disabled={uploading} className="btn-primary w-full">
            {uploading ? 'Verifying...' : 'Verify Age'}
          </button>

          <button type="button" onClick={() => navigate(-1)} className="btn-secondary w-full">
            Cancel
          </button>
        </form>
      </div>
    </div>
  );
}
