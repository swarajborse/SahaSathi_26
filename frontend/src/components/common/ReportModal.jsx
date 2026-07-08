import { useState } from 'react';
import api from '../../services/api';

const REASONS = ['SPAM', 'INAPPROPRIATE', 'HARASSMENT', 'FAKE', 'OFFENSIVE', 'OTHER'];

export default function ReportModal({ isOpen, onClose, targetType, targetId, userId }) {
  const [reason, setReason] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  if (!isOpen) return null;

  async function handleSubmit(e) {
    e.preventDefault();
    if (!reason) return;
    setSubmitting(true);
    try {
      await api.post('/reports', null, {
        params: { reporterId: userId, targetType, targetId, reason, description }
      });
      setSubmitted(true);
    } catch {
      setSubmitted(false);
    } finally {
      setSubmitting(false);
    }
  }

  function handleClose() {
    setReason('');
    setDescription('');
    setSubmitted(false);
    onClose();
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" onClick={handleClose}>
      <div className="bg-white rounded-xl max-w-md w-full p-6" onClick={(e) => e.stopPropagation()}>
        {submitted ? (
          <div className="text-center py-4">
            <p className="text-senior-lg font-semibold text-green-600 mb-2">Thank you for your report</p>
            <p className="text-sm text-gray-500 mb-4">We will review it shortly.</p>
            <button onClick={handleClose} className="btn-primary py-2 px-6">Close</button>
          </div>
        ) : (
          <>
            <h3 className="text-senior-lg font-semibold mb-4">Report {targetType.toLowerCase()}</h3>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">Reason</label>
                <select value={reason} onChange={(e) => setReason(e.target.value)} required
                  className="input text-senior-base">
                  <option value="">Select a reason...</option>
                  {REASONS.map((r) => (
                    <option key={r} value={r}>{r.charAt(0) + r.slice(1).toLowerCase()}</option>
                  ))}
                </select>
              </div>
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Description <span className="text-gray-400">(optional)</span>
                </label>
                <textarea value={description} onChange={(e) => setDescription(e.target.value)}
                  rows={3} maxLength={1000}
                  className="input text-senior-base resize-none"
                  placeholder="Provide additional details..." />
              </div>
              <div className="flex gap-3 justify-end">
                <button type="button" onClick={handleClose}
                  className="btn-secondary py-2 px-4">Cancel</button>
                <button type="submit" disabled={submitting || !reason}
                  className="btn-primary py-2 px-4">
                  {submitting ? 'Submitting...' : 'Submit Report'}
                </button>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
