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
  handleMessageDeleted
} from './socketHandlers';

const SOCKET_URL = window.location.origin.includes('localhost')
  ? 'http://localhost:8080' // Gateway single entry point (routes to 8085 internally)
  : window.location.origin;

let socket = null;

export const connectSocket = (accessToken) => {
  if (socket?.connected) return;

  socket = io(SOCKET_URL, {
    auth: {
      token: accessToken
    },
    transports: ['websocket'],
    reconnection: true,
    reconnectionAttempts: 5,
    reconnectionDelay: 1000,
  });

  socket.on(SOCKET_EVENTS.CONNECT, () => {
    console.log('Socket.io connected securely to Gateway proxy');
    handleSocketConnect();
  });

  socket.on(SOCKET_EVENTS.DISCONNECT, () => {
    console.log('Socket.io disconnected');
    handleSocketDisconnect();
  });

  socket.on(SOCKET_EVENTS.TYPING_STARTED, (data) => {
    handleTypingStarted(data);
  });

  socket.on(SOCKET_EVENTS.TYPING_STOPPED, (data) => {
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
    socket.emit(event, { chatId });
  }
};

export const getSocketInstance = () => socket;
