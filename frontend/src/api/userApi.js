import { api } from './index';

export const userApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getProfile: builder.query({
      query: () => ({
        url: '/api/v1/users/me',
        method: 'GET',
      }),
      providesTags: ['Profile'],
    }),
    getUserProfile: builder.query({
      query: (userId) => ({
        url: `/api/v1/users/${userId}`,
        method: 'GET',
      }),
    }),
    updateProfile: builder.mutation({
      query: (data) => ({
        url: '/api/v1/users/me/profile',
        method: 'PUT',
        data,
      }),
      invalidatesTags: ['Profile'],
    }),
    searchUsers: builder.query({
      query: (query) => ({
        url: `/api/v1/users/search?query=${encodeURIComponent(query)}`,
        method: 'GET',
      }),
      providesTags: ['Friends'],
    }),
    getFriends: builder.query({
      query: () => ({
        url: '/api/v1/users/friends',
        method: 'GET',
      }),
      providesTags: ['Friends'],
    }),
    getFriendRequests: builder.query({
      query: () => ({
        url: '/api/v1/users/friends/pending',
        method: 'GET',
      }),
      providesTags: ['Friends'],
    }),
    sendFriendRequest: builder.mutation({
      query: (addresseeId) => ({
        url: '/api/v1/users/friends/request',
        method: 'POST',
        data: { addresseeId },
      }),
      invalidatesTags: ['Friends'],
    }),
    acceptFriendRequest: builder.mutation({
      query: (requestId) => ({
        url: `/api/v1/users/friends/request/${requestId}/accept`,
        method: 'PUT',
      }),
      invalidatesTags: ['Friends'],
    }),
    getPreferences: builder.query({
      query: () => ({
        url: '/api/v1/users/preferences',
        method: 'GET',
      }),
      providesTags: ['Profile'],
    }),
    updatePreferences: builder.mutation({
      query: (data) => ({
        url: '/api/v1/users/preferences',
        method: 'PUT',
        data,
      }),
      invalidatesTags: ['Profile'],
    }),
  }),
});

export const {
  useGetProfileQuery,
  useGetUserProfileQuery,
  useUpdateProfileMutation,
  useLazySearchUsersQuery,
  useGetFriendsQuery,
  useGetFriendRequestsQuery,
  useSendFriendRequestMutation,
  useAcceptFriendRequestMutation,
  useGetPreferencesQuery,
  useUpdatePreferencesMutation,
} = userApi;
export default userApi;
