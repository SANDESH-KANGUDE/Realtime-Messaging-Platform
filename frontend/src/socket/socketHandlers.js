import { 
  setSocketConnected, 
  setTypingStarted, 
  setTypingStopped, 
  setPresenceUpdated 
} from '../store/slices/socketSlice';
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
  const currentUserId = state.auth.user?.id;

  // Invalidate RTK Query cache to trigger refetching
  store.dispatch(
    api.util.invalidateTags([
      { type: 'Messages', id: data.chatId },
      'Chats'
    ])
  );

  // If the received message is in the currently active chat and not sent by the active user, mark it as read
  if (activeChatId === data.chatId && data.senderId !== currentUserId) {
    // Dispatch REST API call to mark as read
    store.dispatch(
      api.endpoints.markAsRead.initiate(data.id)
    );
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
