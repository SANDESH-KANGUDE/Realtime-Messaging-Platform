import axios from 'axios';
import { setCredentials, logOut } from '../store/slices/authSlice';

// Gateway URL
const API_BASE_URL = window.location.origin.includes('localhost')
  ? 'http://localhost:8080'
  : window.location.origin;

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Enables browser to carry HttpOnly cookies automatically
});

// Avoid circular dependency issues by lazy-loading store
let store;
export const injectStore = (_store) => {
  store = _store;
};

// Request Interceptor
apiClient.interceptors.request.use(
  (config) => {
    if (store) {
      const token = store.getState().auth.accessToken;
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor for handling global 401 token refreshes
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    // Check if error is 401 and request has not been retried yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Avoid intercepting login/refresh paths
      if (originalRequest.url.includes('/api/v1/auth/login') || originalRequest.url.includes('/api/v1/auth/refresh')) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Send refresh token request (browser attaches HttpOnly cookie automatically)
        const refreshResponse = await axios.post(
          `${API_BASE_URL}/api/v1/auth/refresh`,
          {},
          { withCredentials: true }
        );

        const newAccessToken = refreshResponse.data.data.accessToken;
        const user = refreshResponse.data.data.user || store.getState().auth.user;

        if (store) {
          store.dispatch(setCredentials({ accessToken: newAccessToken, user }));
        }

        processQueue(null, newAccessToken);
        isRefreshing = false;

        // Retry original request
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;

        // If refresh fails, log out the user
        if (store) {
          store.dispatch(logOut());
        }
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
