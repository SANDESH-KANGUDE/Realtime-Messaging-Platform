import { api } from './index';

export const notificationApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getNotifications: builder.query({
      query: () => ({
        url: '/api/v1/notifications',
        method: 'GET',
      }),
      providesTags: ['Notifications'],
    }),
    markNotificationAsRead: builder.mutation({
      query: (id) => ({
        url: `/api/v1/notifications/${id}/read`,
        method: 'PUT',
      }),
      invalidatesTags: ['Notifications'],
    }),
  }),
});

export const {
  useGetNotificationsQuery,
  useMarkNotificationAsReadMutation,
} = notificationApi;
export default notificationApi;
