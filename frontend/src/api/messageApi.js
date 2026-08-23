import { api } from './index';

export const messageApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getChatMessages: builder.query({
      query: ({ chatId, page = 0, size = 30 }) => ({
        url: `/api/v1/messages/chat/${chatId}?page=${page}&size=${size}`,
        method: 'GET',
      }),
      // We will handle merging paginated results in the ChatPage UI hook state
      providesTags: (result, error, { chatId }) => [{ type: 'Messages', id: chatId }],
    }),
    sendMessage: builder.mutation({
      query: (data) => ({
        url: '/api/v1/messages',
        method: 'POST',
        data, // { chatId, type, content, replyToMessageId, pollQuestion, pollOptions }
      }),
      invalidatesTags: (result, error, { chatId }) => [{ type: 'Messages', id: chatId }, 'Chats'],
    }),
    editMessage: builder.mutation({
      query: ({ messageId, content }) => ({
        url: `/api/v1/messages/${messageId}`,
        method: 'PUT',
        data: { content },
      }),
    }),
    deleteMessage: builder.mutation({
      query: (messageId) => ({
        url: `/api/v1/messages/${messageId}`,
        method: 'DELETE',
      }),
    }),
    addReaction: builder.mutation({
      query: ({ messageId, emoji }) => ({
        url: `/api/v1/messages/${messageId}/reactions`,
        method: 'POST',
        data: { emoji },
      }),
    }),
    markAsRead: builder.mutation({
      query: (messageId) => ({
        url: `/api/v1/messages/${messageId}/read`,
        method: 'POST',
      }),
    }),
    pinMessage: builder.mutation({
      query: ({ messageId }) => ({
        url: `/api/v1/messages/${messageId}/pin`,
        method: 'PUT',
      }),
      invalidatesTags: (result, error, { chatId }) => [
        { type: 'Messages', id: chatId },
        { type: 'Messages', id: `pinned-${chatId}` },
        'Chats'
      ],
    }),
    unpinMessage: builder.mutation({
      query: ({ messageId }) => ({
        url: `/api/v1/messages/${messageId}/unpin`,
        method: 'PUT',
      }),
      invalidatesTags: (result, error, { chatId }) => [
        { type: 'Messages', id: chatId },
        { type: 'Messages', id: `pinned-${chatId}` },
        'Chats'
      ],
    }),
    votePoll: builder.mutation({
      query: ({ messageId, optionIndex }) => ({
        url: `/api/v1/messages/${messageId}/poll/vote`,
        method: 'POST',
        data: { optionIndex },
      }),
    }),
    getUnreadCounts: builder.query({
      query: () => ({
        url: '/api/v1/messages/unread',
        method: 'GET',
      }),
      providesTags: ['Notifications'],
    }),
    markChatAsRead: builder.mutation({
      query: (chatId) => ({
        url: `/api/v1/messages/chats/${chatId}/read`,
        method: 'POST',
      }),
      invalidatesTags: (result, error, chatId) => [{ type: 'Messages', id: chatId }, { type: 'Messages', id: `pinned-${chatId}` }, 'Chats', 'Notifications'],
    }),
    getPinnedMessages: builder.query({
      query: (chatId) => ({
        url: `/api/v1/messages/chat/${chatId}/pinned`,
        method: 'GET',
      }),
      providesTags: (result, error, chatId) => [{ type: 'Messages', id: `pinned-${chatId}` }],
    }),
    markChatAsDelivered: builder.mutation({
      query: (chatId) => ({
        url: `/api/v1/messages/chats/${chatId}/deliver`,
        method: 'POST',
      }),
      invalidatesTags: (result, error, chatId) => [{ type: 'Messages', id: chatId }],
    }),
  }),
});

export const {
  useGetChatMessagesQuery,
  useLazyGetChatMessagesQuery,
  useSendMessageMutation,
  useEditMessageMutation,
  useDeleteMessageMutation,
  useAddReactionMutation,
  useMarkAsReadMutation,
  usePinMessageMutation,
  useUnpinMessageMutation,
  useVotePollMutation,
  useGetUnreadCountsQuery,
  useMarkChatAsReadMutation,
  useGetPinnedMessagesQuery,
  useMarkChatAsDeliveredMutation,
} = messageApi;
export default messageApi;
