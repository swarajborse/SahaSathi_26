import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendOtp, verifyOtp } from '../services/firebase';
import useAuth from '../hooks/useAuth';
import { toast } from 'react-toastify';

export default function LoginPage() {
  const [step, setStep] = useState('phone');
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [confirmation, setConfirmation] = useState(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSendOtp(e) {
    e.preventDefault();
    if (phone.length < 10) {
      toast.error('Please enter a valid phone number');
      return;
    }
    setLoading(true);
    try {
      const fullPhone = '+91' + phone;
      const confirm = await sendOtp(fullPhone);
      setConfirmation(confirm);
      setStep('otp');
      toast.success('OTP sent to your phone');
    } catch (err) {
      toast.error(err.message || 'Failed to send OTP');
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyOtp(e) {
    e.preventDefault();
    if (otp.length < 4) {
      toast.error('Please enter the OTP');
      return;
    }
    setLoading(true);
    try {
      const idToken = await verifyOtp(confirmation, otp);
      const userData = await login(idToken);
      if (userData.newUser) {
        navigate('/register');
      } else {
        navigate('/');
      }
    } catch (err) {
      toast.error('Invalid OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center px-4">
      <div className="card max-w-md w-full">
        <h1 className="page-title text-center mb-2">Welcome to Sahasathi</h1>
        <p className="text-senior-base text-gray-600 text-center mb-8">
          Enter your phone number to continue
        </p>

        <div id="recaptcha-container"></div>

        {step === 'phone' ? (
          <form onSubmit={handleSendOtp} className="space-y-6">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-2">
                Phone Number
              </label>
              <div className="flex items-center border-2 border-gray-300 rounded-lg">
                <span className="px-4 py-3 text-senior-base text-gray-500 bg-gray-50 border-r-2 border-gray-300 rounded-l-lg">
                  +91
                </span>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 10))}
                  placeholder="9876543210"
                  className="input-field border-0 rounded-l-none flex-1"
                  maxLength={10}
                  autoFocus
                />
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? 'Sending OTP...' : 'Send OTP'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleVerifyOtp} className="space-y-6">
            <div>
              <label className="block text-senior-base font-medium text-gray-700 mb-2">
                Enter OTP
              </label>
              <p className="text-sm text-gray-500 mb-4">
                OTP sent to +91 {phone}
              </p>
              <input
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                placeholder="Enter 6-digit OTP"
                className="input-field text-center text-senior-xl tracking-widest"
                maxLength={6}
                autoFocus
              />
            </div>
            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? 'Verifying...' : 'Verify OTP'}
            </button>
            <button
              type="button"
              onClick={() => setStep('phone')}
              className="btn-secondary w-full"
            >
              Change Phone Number
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
