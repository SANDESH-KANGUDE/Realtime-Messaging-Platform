import { api } from './index';

export const authApi = api.injectEndpoints({
  endpoints: (builder) => ({
    register: builder.mutation({
      query: (data) => ({
        url: '/api/v1/auth/register',
        method: 'POST',
        data,
      }),
    }),
    login: builder.mutation({
      query: (data) => ({
        url: '/api/v1/auth/login',
        method: 'POST',
        data,
      }),
    }),
    logout: builder.mutation({
      query: () => ({
        url: '/api/v1/auth/logout',
        method: 'POST',
      }),
      async onQueryStarted(arg, { dispatch, queryFulfilled }) {
        try {
          await queryFulfilled;
        } catch (err) {
          console.error('Logout request failed', err);
        }
      },
    }),
    getMe: builder.query({
      query: () => ({
        url: '/api/v1/auth/me',
        method: 'GET',
      }),
      providesTags: ['Profile'],
    }),
  }),
});

export const {
  useRegisterMutation,
  useLoginMutation,
  useLogoutMutation,
  useLazyGetMeQuery,
  useGetMeQuery,
} = authApi;
export default authApi;
