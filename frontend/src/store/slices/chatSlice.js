import { createSlice } from '@reduxjs/toolkit';

const chatSlice = createSlice({
  name: 'chat',
  initialState: {
    activeChatId: null,
    archivedViewActive: false,
    mutes: {}, // { [chatId]: boolean }
    pins: {},  // { [chatId]: boolean }
    searchQuery: '',
    unreadCounts: {}, // { [chatId]: number }
  },
  reducers: {
    setActiveChatId: (state, action) => {
      state.activeChatId = action.payload;
    },
    setUnreadCounts: (state, action) => {
      state.unreadCounts = action.payload;
    },
    incrementUnreadCount: (state, action) => {
      const chatId = action.payload;
      state.unreadCounts[chatId] = (state.unreadCounts[chatId] || 0) + 1;
    },
    clearUnreadCount: (state, action) => {
      const chatId = action.payload;
      state.unreadCounts[chatId] = 0;
    },
    setArchivedViewActive: (state, action) => {
      state.archivedViewActive = action.payload;
    },
    toggleMuteState: (state, action) => {
      const chatId = action.payload;
      state.mutes[chatId] = !state.mutes[chatId];
    },
    togglePinState: (state, action) => {
      const chatId = action.payload;
      state.pins[chatId] = !state.pins[chatId];
    },
    setMutes: (state, action) => {
      state.mutes = action.payload;
    },
    setPins: (state, action) => {
      state.pins = action.payload;
    },
    setSearchQuery: (state, action) => {
      state.searchQuery = action.payload;
    },
    clearChatState: (state) => {
      state.activeChatId = null;
      state.archivedViewActive = false;
      state.mutes = {};
      state.pins = {};
      state.searchQuery = '';
      state.unreadCounts = {};
    }
  }
});

export const {
  setActiveChatId,
  setArchivedViewActive,
  toggleMuteState,
  togglePinState,
  setMutes,
  setPins,
  setSearchQuery,
  clearChatState,
  setUnreadCounts,
  incrementUnreadCount,
  clearUnreadCount
} = chatSlice.actions;

export default chatSlice.reducer;
