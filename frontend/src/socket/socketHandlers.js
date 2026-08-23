import { 
  setSocketConnected, 
  setTypingStarted, 
  setTypingStopped, 
  setPresenceUpdated 
} from '../store/slices/socketSlice';
import { incrementUnreadCount } from '../store/slices/chatSlice';
import { api } from '../api';
import { store } from '../store';

export const handleSocketConnect = () => {
  store.dispatch(setSocketConnected(true));
};

export const handleSocketDisconnect = () => {
  store.dispatch(setSocketConnected(false));
};

export const handleTypingStarted = (data) => {
  // data: { chatId, userId }
  store.dispatch(setTypingStarted(data));
};

export const handleTypingStopped = (data) => {
  // data: { chatId, userId }
  store.dispatch(setTypingStopped(data));
};

export const handlePresenceUpdated = (data) => {
  // data: { userId, status }
  store.dispatch(setPresenceUpdated(data));
};

export const handleMessageReceived = (data) => {
  // data is the new message object: { id, chatId, senderId, content, ... }
  const state = store.getState();
  const activeChatId = state.chat.activeChatId;
  const currentUserId = state.auth.user?.userId;

  // Invalidate RTK Query cache to trigger refetching
  store.dispatch(
    api.util.invalidateTags([
      { type: 'Messages', id: data.chatId },
      'Chats',
      'Notifications'
    ])
  );

  // If the received message is in the currently active chat, mark it as read immediately
  if (activeChatId === data.chatId) {
    if (data.senderId !== currentUserId) {
      // Dispatch REST API call to mark as read
      store.dispatch(
        api.endpoints.markAsRead.initiate(data.id)
      );
    }
  } else {
    const chats = api.endpoints.getChats.select()(state).data?.data || [];
    const chat = chats.find(c => c.id === data.chatId);
    if (!chat?.archived) {
      // Increment local unread count
      store.dispatch(incrementUnreadCount(data.chatId));
    }
  }
};

export const handleMessageEdited = (data) => {
  // data: { messageId, chatId, content, ... }
  store.dispatch(
    api.util.invalidateTags([
      { type: 'Messages', id: data.chatId }
    ])
  );
};

export const handleMessageDeleted = (data) => {
  // data: { messageId, chatId }
  store.dispatch(
    api.util.invalidateTags([
      { type: 'Messages', id: data.chatId }
    ])
  );
};

export const handleFriendRequestReceived = (data) => {
  store.dispatch(
    api.util.invalidateTags(['Friends'])
  );
};

export const handleFriendRequestAccepted = (data) => {
  store.dispatch(
    api.util.invalidateTags(['Friends', 'Chats'])
  );
};

export const handleMessageRead = (data) => {
  // data: { messageId, chatId, userId, readCount }
  store.dispatch(
    api.util.invalidateTags([
      { type: 'Messages', id: data.chatId },
      'Chats',
      'Notifications'
    ])
  );
};

export const handleMessageDelivered = (data) => {
  // data: { messageId, chatId, userId }
  store.dispatch(
    api.util.invalidateTags([
      { type: 'Messages', id: data.chatId }
    ])
  );
};
