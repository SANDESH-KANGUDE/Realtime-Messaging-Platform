import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/AuthPage';
import ChatPage from './pages/ChatPage';
import { useSelector, useDispatch } from 'react-redux';
import { setCredentials, logOut } from './store/slices/authSlice';
import { connectSocket } from './socket/socketClient';
import apiClient from './api/client';
import { Loader } from 'lucide-react';

// Module-level cache to prevent concurrent duplicate refresh calls during double-mounts (React StrictMode)
let activeRefreshPromise = null;

function App() {
  const theme = useSelector((state) => state.theme.mode);
  const dispatch = useDispatch();
  const [isInitializing, setIsInitializing] = useState(true);

  // Sync theme class to HTML element on mount/toggle
  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  // Try to restore session via refresh token on mount
  useEffect(() => {
    const initializeAuth = async () => {
      const storedRefreshToken = localStorage.getItem('refreshToken');
      if (storedRefreshToken) {
        try {
          // Perform post request to refresh access token, caching the promise to prevent duplicate calls
          if (!activeRefreshPromise) {
            activeRefreshPromise = apiClient.post('/api/v1/auth/refresh', {
              refreshToken: storedRefreshToken
            });
          }
          
          const res = await activeRefreshPromise;
          const { accessToken, refreshToken: newRefreshToken, userId, email, role } = res.data.data;
          
          localStorage.setItem('refreshToken', newRefreshToken);
          dispatch(setCredentials({ accessToken, user: { userId, email, role } }));
          connectSocket(accessToken);
        } catch (err) {
          console.error('Failed to restore session:', err);
          localStorage.removeItem('refreshToken');
          dispatch(logOut());
        } finally {
          activeRefreshPromise = null;
        }
      }
      setIsInitializing(false);
    };

    initializeAuth();
  }, [dispatch]);

  if (isInitializing) {
    return (
      <div className="w-screen h-screen flex flex-col items-center justify-center bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100">
        <Loader className="animate-spin text-aura-teal-500 mb-2" size={32} />
        <p className="text-xs font-semibold tracking-wider text-slate-500">Restoring session...</p>
      </div>
    );
  }

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<AuthPage />} />
        <Route path="/register" element={<AuthPage />} />
        <Route path="/chat" element={<ChatPage />} />
        <Route path="/chat/:chatId" element={<ChatPage />} />
        
        {/* Fallback routes */}
        <Route path="*" element={<Navigate to="/chat" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
