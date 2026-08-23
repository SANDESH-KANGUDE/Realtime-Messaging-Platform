import { api } from './index';

export const chatApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getChats: builder.query({
      query: () => ({
        url: '/api/v1/chats',
        method: 'GET',
      }),
      providesTags: ['Chats'],
    }),
    createDirectChat: builder.mutation({
      query: (targetUserId) => ({
        url: '/api/v1/chats/direct',
        method: 'POST',
        data: { targetUserId },
      }),
      invalidatesTags: ['Chats'],
    }),
    createGroupChat: builder.mutation({
      query: (data) => ({
        url: '/api/v1/chats/groups',
        method: 'POST',
        data, // { title, avatarUrl, memberUserIds }
      }),
      invalidatesTags: ['Chats'],
    }),
    pinChat: builder.mutation({
      query: (chatId) => ({
        url: `/api/v1/chats/${chatId}/pin`,
        method: 'PUT',
      }),
      invalidatesTags: ['Chats'],
    }),
    unpinChat: builder.mutation({
      query: (chatId) => ({
        url: `/api/v1/chats/${chatId}/unpin`,
        method: 'PUT',
      }),
      invalidatesTags: ['Chats'],
    }),
    archiveChat: builder.mutation({
      query: (chatId) => ({
        url: `/api/v1/chats/${chatId}/archive`,
        method: 'PUT',
      }),
      invalidatesTags: ['Chats'],
    }),
    unarchiveChat: builder.mutation({
      query: (chatId) => ({
        url: `/api/v1/chats/${chatId}/unarchive`,
        method: 'PUT',
      }),
      invalidatesTags: ['Chats'],
    }),
    addGroupMember: builder.mutation({
      query: ({ chatId, userId }) => ({
        url: `/api/v1/chats/${chatId}/members`,
        method: 'POST',
        data: { userId },
      }),
      invalidatesTags: ['Chats'],
    }),
    removeGroupMember: builder.mutation({
      query: ({ chatId, userId }) => ({
        url: `/api/v1/chats/${chatId}/members/${userId}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['Chats'],
    }),
    updateMemberRole: builder.mutation({
      query: ({ chatId, userId, role }) => ({
        url: `/api/v1/chats/${chatId}/members/${userId}/role?role=${role}`,
        method: 'PUT',
      }),
      invalidatesTags: ['Chats'],
    }),
    updateChatTheme: builder.mutation({
      query: ({ chatId, theme }) => ({
        url: `/api/v1/chats/${chatId}/theme?theme=${encodeURIComponent(theme)}`,
        method: 'PUT',
      }),
      invalidatesTags: ['Chats'],
    }),
    deleteGroupChat: builder.mutation({
      query: (chatId) => ({
        url: `/api/v1/chats/${chatId}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['Chats'],
    }),
  }),
});

export const {
  useGetChatsQuery,
  useCreateDirectChatMutation,
  useCreateGroupChatMutation,
  usePinChatMutation,
  useUnpinChatMutation,
  useArchiveChatMutation,
  useUnarchiveChatMutation,
  useAddGroupMemberMutation,
  useRemoveGroupMemberMutation,
  useUpdateMemberRoleMutation,
  useUpdateChatThemeMutation,
  useDeleteGroupChatMutation,
} = chatApi;
export default chatApi;
