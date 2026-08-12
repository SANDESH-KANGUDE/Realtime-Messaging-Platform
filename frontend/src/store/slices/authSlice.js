import { createSlice } from '@reduxjs/toolkit';

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    accessToken: null,
    user: null,
    isAuthenticated: false,
  },
  reducers: {
    setCredentials: (state, action) => {
      const { accessToken, user } = action.payload;
      state.accessToken = accessToken;
      state.user = user;
      state.isAuthenticated = !!accessToken;
    },
    logOut: (state) => {
      state.accessToken = null;
      state.user = null;
      state.isAuthenticated = false;
    },
    updateUserProfileState: (state, action) => {
      if (state.user) {
        state.user = { ...state.user, ...action.payload };
      }
    }
  }
});

export const { setCredentials, logOut, updateUserProfileState } = authSlice.actions;
export default authSlice.reducer;
