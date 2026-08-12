import { configureStore } from '@reduxjs/toolkit';
import { api } from '../api';
import authReducer from './slices/authSlice';
import themeReducer from './slices/themeSlice';
import socketReducer from './slices/socketSlice';
import chatReducer from './slices/chatSlice';
import uiReducer from './slices/uiSlice';
import { injectStore } from '../api/client';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    theme: themeReducer,
    socket: socketReducer,
    chat: chatReducer,
    ui: uiReducer,
    [api.reducerPath]: api.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(api.middleware),
});

// Inject store reference into Axios client to avoid circular imports
injectStore(store);

export default store;
