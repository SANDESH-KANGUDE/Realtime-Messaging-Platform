import { api } from './index';

export const adminApi = api.injectEndpoints({
  endpoints: (builder) => ({
    reportUser: builder.mutation({
      query: (data) => ({
        url: '/api/v1/admin/reports',
        method: 'POST',
        data, // { reportedUserId, reason }
      }),
      invalidatesTags: ['AdminReports'],
    }),
    getReports: builder.query({
      query: () => ({
        url: '/api/v1/admin/reports',
        method: 'GET',
      }),
      providesTags: ['AdminReports'],
    }),
    banUser: builder.mutation({
      query: (data) => ({
        url: '/api/v1/admin/users/ban',
        method: 'POST',
        data, // { userId, reason }
      }),
    }),
    getAuditLogs: builder.query({
      query: () => ({
        url: '/api/v1/admin/audit-logs',
        method: 'GET',
      }),
    }),
    getAdminDashboard: builder.query({
      query: () => ({
        url: '/api/v1/admin/dashboard',
        method: 'GET',
      }),
    }),
  }),
});

export const {
  useReportUserMutation,
  useGetReportsQuery,
  useBanUserMutation,
  useGetAuditLogsQuery,
  useGetAdminDashboardQuery,
} = adminApi;
export default adminApi;
