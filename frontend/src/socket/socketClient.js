import { io } from 'socket.io-client';
import { SOCKET_EVENTS } from './socketEvents';
import {
  handleSocketConnect,
  handleSocketDisconnect,
  handleTypingStarted,
  handleTypingStopped,
  handlePresenceUpdated,
  handleMessageReceived,
  handleMessageEdited,
  handleMessageDeleted,
  handleFriendRequestReceived,
  handleFriendRequestAccepted,
  handleMessageRead,
  handleMessageDelivered
} from './socketHandlers';

const SOCKET_URL = window.location.origin.includes('localhost')
  ? 'http://localhost:8085' // Direct to Netty Socket.io server
  : window.location.origin;

let socket = null;

export const connectSocket = (accessToken) => {
  if (socket?.connected) return;

  socket = io(SOCKET_URL, {
    auth: {
      token: accessToken
    },
    query: {
      token: accessToken
    },
    transports: ['websocket'],
    reconnection: true,
    reconnectionAttempts: 5,
    reconnectionDelay: 1000,
  });

  socket.on(SOCKET_EVENTS.CONNECT, () => {
    console.log('Socket.io connected securely to Netty server');
    handleSocketConnect();
  });

  socket.on('connect_error', (err) => {
    console.error('Socket.io connection error:', err.message, err.description, err.context);
  });

  socket.on(SOCKET_EVENTS.DISCONNECT, (reason) => {
    console.log('Socket.io disconnected because:', reason);
    handleSocketDisconnect();
  });

  socket.on(SOCKET_EVENTS.TYPING_STARTED, (data) => {
    console.log('[Socket] Received typing.started event:', data);
    handleTypingStarted(data);
  });

  socket.on(SOCKET_EVENTS.TYPING_STOPPED, (data) => {
    console.log('[Socket] Received typing.stopped event:', data);
    handleTypingStopped(data);
  });

  socket.on(SOCKET_EVENTS.PRESENCE_UPDATED, (data) => {
    handlePresenceUpdated(data);
  });

  socket.on('user.online', (data) => {
    handlePresenceUpdated({ userId: data.userId, status: 'ONLINE' });
  });

  socket.on('user.offline', (data) => {
    handlePresenceUpdated({ userId: data.userId, status: 'OFFLINE' });
  });

  socket.on(SOCKET_EVENTS.MESSAGE_RECEIVED, (data) => {
    handleMessageReceived(data);
  });

  socket.on(SOCKET_EVENTS.MESSAGE_EDITED, (data) => {
    handleMessageEdited(data);
  });

  socket.on(SOCKET_EVENTS.MESSAGE_DELETED, (data) => {
    handleMessageDeleted(data);
  });

  socket.on('message_read', (data) => {
    handleMessageRead(data);
  });

  socket.on('message_delivered', (data) => {
    handleMessageDelivered(data);
  });

  socket.on('friend_request_received', (data) => {
    console.log('Realtime friend request received event:', data);
    handleFriendRequestReceived(data);
  });

  socket.on('friend_request_accepted', (data) => {
    console.log('Realtime friend request accepted event:', data);
    handleFriendRequestAccepted(data);
  });
};

export const disconnectSocket = () => {
  if (socket) {
    socket.disconnect();
    socket = null;
  }
};

export const joinRoom = (chatId) => {
  if (socket?.connected && chatId) {
    socket.emit(SOCKET_EVENTS.JOIN_ROOM, chatId);
  }
};

export const emitTyping = (chatId, isTyping) => {
  if (socket?.connected && chatId) {
    const event = isTyping ? SOCKET_EVENTS.TYPING_START : SOCKET_EVENTS.TYPING_STOP;
    console.log(`[Socket] Emitting ${event} for chatId:`, chatId);
    socket.emit(event, { chatId });
  }
};

export const queryPresence = (userId) => {
  if (socket?.connected && userId) {
    socket.emit('query_presence', { userId });
  }
};

export const getSocketInstance = () => socket;
