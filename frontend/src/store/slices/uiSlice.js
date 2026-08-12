import { createSlice } from '@reduxjs/toolkit';

const uiSlice = createSlice({
  name: 'ui',
  initialState: {
    modals: {
      createGroup: false,
      createPoll: false,
      profile: false,
      archivedChats: false,
      friendRequests: false,
      payments: false,
    },
    messageSearchActive: false,
  },
  reducers: {
    openModal: (state, action) => {
      state.modals[action.payload] = true;
    },
    closeModal: (state, action) => {
      state.modals[action.payload] = false;
    },
    toggleModal: (state, action) => {
      state.modals[action.payload] = !state.modals[action.payload];
    },
    setMessageSearchActive: (state, action) => {
      state.messageSearchActive = action.payload;
    },
    closeAllModals: (state) => {
      Object.keys(state.modals).forEach((key) => {
        state.modals[key] = false;
      });
      state.messageSearchActive = false;
    }
  }
});

export const {
  openModal,
  closeModal,
  toggleModal,
  setMessageSearchActive,
  closeAllModals
} = uiSlice.actions;

export default uiSlice.reducer;
