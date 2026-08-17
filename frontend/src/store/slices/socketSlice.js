import { createSlice } from '@reduxjs/toolkit';

const socketSlice = createSlice({
  name: 'socket',
  initialState: {
    connected: false,
    typingUsers: {}, // { [chatId]: [userId1, userId2] }
    onlineUsers: {},  // { [userId]: 'ONLINE' | 'OFFLINE' }
  },
  reducers: {
    setSocketConnected: (state, action) => {
      state.connected = action.payload;
    },
    setTypingStarted: (state, action) => {
      console.log('[SocketSlice] setTypingStarted payload:', action.payload);
      const { chatId, userId } = action.payload;
      const currentTypers = state.typingUsers[chatId] || [];
      if (!currentTypers.includes(userId)) {
        state.typingUsers = {
          ...state.typingUsers,
          [chatId]: [...currentTypers, userId]
        };
      }
      console.log('[SocketSlice] setTypingStarted updated typingUsers:', JSON.stringify(state.typingUsers));
    },
    setTypingStopped: (state, action) => {
      console.log('[SocketSlice] setTypingStopped payload:', action.payload);
      const { chatId, userId } = action.payload;
      if (state.typingUsers[chatId]) {
        const updatedTypers = state.typingUsers[chatId].filter(id => id !== userId);
        state.typingUsers = {
          ...state.typingUsers,
          [chatId]: updatedTypers
        };
      }
      console.log('[SocketSlice] setTypingStopped updated typingUsers:', JSON.stringify(state.typingUsers));
    },
    setPresenceUpdated: (state, action) => {
      const { userId, status } = action.payload;
      state.onlineUsers = {
        ...state.onlineUsers,
        [userId]: status
      };
    },
    clearSocketState: (state) => {
      state.connected = false;
      state.typingUsers = {};
      state.onlineUsers = {};
    }
  }
});

export const {
  setSocketConnected,
  setTypingStarted,
  setTypingStopped,
  setPresenceUpdated,
  clearSocketState
} = socketSlice.actions;

export default socketSlice.reducer;
