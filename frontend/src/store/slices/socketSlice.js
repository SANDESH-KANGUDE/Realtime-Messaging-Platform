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
      const { chatId, userId } = action.payload;
      if (!state.typingUsers[chatId]) {
        state.typingUsers[chatId] = [];
      }
      if (!state.typingUsers[chatId].includes(userId)) {
        state.typingUsers[chatId].push(userId);
      }
    },
    setTypingStopped: (state, action) => {
      const { chatId, userId } = action.payload;
      if (state.typingUsers[chatId]) {
        state.typingUsers[chatId] = state.typingUsers[chatId].filter(id => id !== userId);
      }
    },
    setPresenceUpdated: (state, action) => {
      const { userId, status } = action.payload;
      state.onlineUsers[userId] = status;
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
