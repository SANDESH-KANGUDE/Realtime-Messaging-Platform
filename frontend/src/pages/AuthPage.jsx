import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { loginSchema, registerSchema } from '../utils/schemas';
import { useLoginMutation, useRegisterMutation } from '../api/authApi';
import { setCredentials } from '../store/slices/authSlice';
import { connectSocket } from '../socket/socketClient';
import { LogIn, UserPlus, AlertCircle, Loader, Check } from 'lucide-react';

export const AuthPage = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const theme = useSelector((state) => state.theme.mode);
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);

  const [loginUser, { isLoading: isLoginLoading }] = useLoginMutation();
  const [registerUser, { isLoading: isRegisterLoading }] = useRegisterMutation();

  // If already authenticated, redirect immediately
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/chat');
    }
  }, [isAuthenticated, navigate]);

  // Configure Form hook resolvers
  const {
    register: registerField,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(isLogin ? loginSchema : registerSchema),
    defaultValues: {
      identifier: '',
      username: '',
      email: '',
      phoneNumber: '',
      password: '',
    }
  });

  const toggleAuthMode = () => {
    setIsLogin(!isLogin);
    setErrorMessage('');
    setSuccessMessage('');
    reset();
  };

  const onSubmit = async (data) => {
    setErrorMessage('');
    try {
      if (isLogin) {
        // Build login request mapping credentials to email or phone if matches
        const payload = { password: data.password };
        const id = data.identifier.trim();
        
        if (id.includes('@')) {
          payload.email = id;
        } else {
          payload.phoneNumber = id;
        }

        const res = await loginUser(payload).unwrap();
        const { accessToken, refreshToken, userId, email, role } = res.data;
        localStorage.setItem('refreshToken', refreshToken);
        dispatch(setCredentials({ accessToken, user: { userId, email, role } }));
        connectSocket(accessToken);
        navigate('/chat');
      } else {
        const payload = {
          username: data.username.trim(),
          email: data.email.trim(),
          phoneNumber: data.phoneNumber.trim(),
          password: data.password,
          displayName: data.username.trim(),
        };

        await registerUser(payload).unwrap();
        setSuccessMessage('Registration successful! Please sign in with your credentials.');
        setIsLogin(true);
        reset();
      }
    } catch (err) {
      console.error(err);
      setErrorMessage(err.data?.message || 'Authentication failed. Please check your credentials.');
    }
  };

  return (
    <div className={`min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200 dark:from-slate-900 dark:to-slate-950 p-4 transition-colors duration-200`}>
      <div className="w-full max-w-md glass-panel p-8 rounded-2xl shadow-xl transition-all duration-300 transform scale-100">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-aura-teal-600 to-aura-teal-700 dark:from-aura-teal-500 dark:to-aura-teal-600 mb-2">
            Aura Chat
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            {isLogin ? 'Sign in to access your secure workspace' : 'Create an account to start communicating'}
          </p>
        </div>

        {errorMessage && (
          <div className="flex items-center gap-2 bg-red-50 dark:bg-red-950/30 text-red-600 dark:text-red-400 p-3 rounded-lg border border-red-200 dark:border-red-900/50 mb-6 text-sm">
            <AlertCircle size={18} className="flex-shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        {successMessage && (
          <div className="flex items-center gap-2 bg-emerald-50 dark:bg-emerald-950/30 text-emerald-600 dark:text-emerald-400 p-3 rounded-lg border border-emerald-200 dark:border-emerald-900/50 mb-6 text-sm">
            <Check size={18} className="flex-shrink-0" />
            <span>{successMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {isLogin ? (
            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                Email or Phone Number
              </label>
              <input
                type="text"
                placeholder="john@example.com or +1234567890"
                {...registerField('identifier')}
                className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-slate-900/50 text-slate-900 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
              />
              {errors.identifier && (
                <p className="text-xs text-red-500 mt-1">{errors.identifier.message}</p>
              )}
            </div>
          ) : (
            <>
              <div>
                <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                  Username
                </label>
                <input
                  type="text"
                  placeholder="john_doe"
                  {...registerField('username')}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-slate-900/50 text-slate-900 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
                />
                {errors.username && (
                  <p className="text-xs text-red-500 mt-1">{errors.username.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                  Email
                </label>
                <input
                  type="email"
                  placeholder="john@example.com"
                  {...registerField('email')}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-slate-900/50 text-slate-900 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
                />
                {errors.email && (
                  <p className="text-xs text-red-500 mt-1">{errors.email.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                  Phone Number
                </label>
                <input
                  type="text"
                  placeholder="+1234567890"
                  {...registerField('phoneNumber')}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-slate-900/50 text-slate-900 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
                />
                {errors.phoneNumber && (
                  <p className="text-xs text-red-500 mt-1">{errors.phoneNumber.message}</p>
                )}
              </div>

            </>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
              Password
            </label>
            <input
              type="password"
              placeholder="••••••••"
              {...registerField('password')}
              className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-slate-900/50 text-slate-900 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
            />
            {errors.password && (
              <p className="text-xs text-red-500 mt-1">{errors.password.message}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={isLogin ? isLoginLoading : isRegisterLoading}
            className="w-full mt-6 py-3 rounded-xl bg-gradient-to-r from-aura-teal-600 to-aura-teal-700 hover:from-aura-teal-700 hover:to-aura-teal-800 text-white font-bold transition-all duration-150 flex items-center justify-center gap-2 cursor-pointer shadow-md disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {(isLogin ? isLoginLoading : isRegisterLoading) ? (
              <Loader className="animate-spin" size={20} />
            ) : isLogin ? (
              <>
                <LogIn size={20} />
                <span>Sign In</span>
              </>
            ) : (
              <>
                <UserPlus size={20} />
                <span>Sign Up</span>
              </>
            )}
          </button>
        </form>

        <div className="mt-6 text-center text-sm">
          <button
            onClick={toggleAuthMode}
            className="text-aura-teal-600 dark:text-aura-teal-500 font-semibold hover:underline cursor-pointer bg-transparent border-none"
          >
            {isLogin ? "Don't have an account? Sign Up" : 'Already have an account? Sign In'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;
