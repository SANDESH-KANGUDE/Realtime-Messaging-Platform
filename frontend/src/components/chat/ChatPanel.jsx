import React, { useState, useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { 
  useGetChatMessagesQuery, 
  useSendMessageMutation, 
  useEditMessageMutation, 
  useDeleteMessageMutation,
  useAddReactionMutation,
  usePinMessageMutation,
  useUnpinMessageMutation 
} from '../../api/messageApi';
import { 
  usePinChatMutation, 
  useUnpinChatMutation,
  useGetChatsQuery
} from '../../api/chatApi';
import { useGetFriendsQuery } from '../../api/userApi';
import { 
  setActiveChatId, 
  toggleMuteState, 
  togglePinState 
} from '../../store/slices/chatSlice';
import { openModal } from '../../store/slices/uiSlice';
import { emitTyping, joinRoom } from '../../socket/socketClient';
import { sanitizeHtml, linkifyText } from '../../utils/sanitization';
import { useInfiniteScroll } from '../../hooks/useInfiniteScroll';
import PollView from './PollView';
import { 
  ArrowLeft, Search, Send, Smile, CornerUpLeft, 
  Trash2, Edit3, X, Pin, VolumeX, BarChart2, Check, CheckCheck, Loader,
  MessageSquare
} from 'lucide-react';

export const ChatPanel = () => {
  const dispatch = useDispatch();
  const activeChatId = useSelector((state) => state.chat.activeChatId);
  const currentUserId = useSelector((state) => state.auth.user?.userId);
  const pins = useSelector((state) => state.chat.pins);
  const mutes = useSelector((state) => state.chat.mutes);
  const onlineUsers = useSelector((state) => state.socket.onlineUsers);

  const { data: chatsRes } = useGetChatsQuery();
  const { data: friendsRes } = useGetFriendsQuery();

  const chats = chatsRes?.data || [];
  const friends = friendsRes?.data || [];

  const activeChat = chats.find(c => c.id === activeChatId);

  const [messageText, setMessageText] = useState('');
  const [editingMessage, setEditingMessage] = useState(null);
  const [replyingTo, setReplyingTo] = useState(null);
  const [activeReactionPickerMessageId, setActiveReactionPickerMessageId] = useState(null);
  const [typingTimeout, setTypingTimeout] = useState(null);
  
  // Pagination tracking states
  const [page, setPage] = useState(0);
  const [messagesList, setMessagesList] = useState([]);

  // Fetch paginated messages using RTK Query
  const { data: msgsRes, isLoading: messagesLoading, isFetching: messagesFetching } = useGetChatMessagesQuery(
    { chatId: activeChatId, page, size: 30 },
    { skip: !activeChatId }
  );

  const [sendMessage, { isLoading: sendLoading }] = useSendMessageMutation();
  const [editMessage] = useEditMessageMutation();
  const [deleteMessage] = useDeleteMessageMutation();
  const [addReaction] = useAddReactionMutation();
  const [pinChat] = usePinChatMutation();
  const [unpinChat] = useUnpinChatMutation();

  const totalPages = msgsRes?.data?.totalPages || 0;
  const hasMore = page < totalPages - 1;

  // Joint room room channel subscription on socket
  useEffect(() => {
    if (activeChatId) {
      joinRoom(activeChatId);
      setPage(0);
      setMessagesList([]);
      setReplyingTo(null);
      setEditingMessage(null);
    }
  }, [activeChatId]);

  // Merge historical messages into state list
  useEffect(() => {
    if (msgsRes?.data?.content) {
      const pageMessages = [...msgsRes.data.content].reverse(); // oldest first
      setMessagesList((prev) => {
        // Prevent duplicates
        const existingIds = new Set(prev.map(m => m.id));
        const newUnique = pageMessages.filter(m => !existingIds.has(m.id));
        
        if (page === 0) {
          return pageMessages;
        } else {
          return [...newUnique, ...prev]; // Prepend historical pages
        }
      });
    }
  }, [msgsRes, page]);

  // Scroll locks custom hook
  const loadMoreMessages = () => {
    if (hasMore && !messagesFetching) {
      setPage((p) => p + 1);
    }
  };

  const messagesEndRef = useRef(null);
  const scrollContainerRef = useInfiniteScroll(loadMoreMessages, hasMore, messagesFetching);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // Scroll to bottom on initial load
  useEffect(() => {
    if (page === 0 && messagesList.length > 0) {
      setTimeout(scrollToBottom, 50);
    }
  }, [activeChatId, page]);

  const handleInputChange = (e) => {
    setMessageText(e.target.value);
    
    // Typing indicators trigger
    emitTyping(activeChatId, true);
    
    if (typingTimeout) clearTimeout(typingTimeout);
    
    const timeout = setTimeout(() => {
      emitTyping(activeChatId, false);
    }, 2000);
    setTypingTimeout(timeout);
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!messageText.trim() && !replyingTo) return;

    try {
      const sanitized = sanitizeHtml(messageText.trim());
      
      if (editingMessage) {
        await editMessage({ messageId: editingMessage.id, content: sanitized }).unwrap();
        setEditingMessage(null);
      } else {
        const payload = {
          chatId: activeChatId,
          type: 'TEXT',
          content: sanitized,
          replyToMessageId: replyingTo ? replyingTo.id : null
        };
        await sendMessage(payload).unwrap();
        setReplyingTo(null);
      }
      setMessageText('');
      emitTyping(activeChatId, false);
      setTimeout(scrollToBottom, 50);
    } catch (err) {
      console.error(err);
    }
  };

  const handleMuteToggle = () => {
    dispatch(toggleMuteState(activeChatId));
  };

  const handlePinToggle = async () => {
    const isPinned = pins[activeChatId];
    try {
      if (isPinned) {
        await unpinChat(activeChatId).unwrap();
        dispatch(togglePinState(activeChatId));
      } else {
        await pinChat(activeChatId).unwrap();
        dispatch(togglePinState(activeChatId));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleReact = async (messageId, emoji) => {
    try {
      await addReaction({ messageId, emoji }).unwrap();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (messageId) => {
    try {
      await deleteMessage(messageId).unwrap();
    } catch (err) {
      console.error(err);
    }
  };

  if (!activeChatId) {
    return (
      <div className="hidden md:flex flex-col items-center justify-center w-full h-full bg-slate-50 dark:bg-slate-950 transition-colors duration-200 select-none">
        <MessageSquare size={72} className="text-slate-300 dark:text-slate-800 mb-4 animate-bounce" />
        <h2 className="text-xl font-bold text-slate-700 dark:text-slate-300">Aura Chat Platform</h2>
        <p className="text-sm text-slate-400 mt-2">Select a conversation from the sidebar to start messaging</p>
      </div>
    );
  }

  const getChatHeaderName = () => {
    if (!activeChat) return 'Conversation';
    if (activeChat.type === 'GROUP') return activeChat.title;
    const otherMember = activeChat.members?.find((m) => m.userId !== currentUserId);
    if (!otherMember) return 'Direct Chat';
    const friendInfo = friends.find(f => f.friendProfile?.userId === otherMember.userId);
    return friendInfo?.friendProfile ? (friendInfo.friendProfile.displayName || friendInfo.friendProfile.username) : otherMember.userId;
  };

  const getChatHeaderStatus = () => {
    if (!activeChat) return '';
    if (activeChat.type === 'GROUP') return 'Group Chat';
    const otherMember = activeChat.members?.find((m) => m.userId !== currentUserId);
    if (!otherMember) return 'Offline';
    const isOnline = onlineUsers[otherMember.userId] === 'ONLINE';
    return isOnline ? 'Online' : 'Offline';
  };

  const headerName = getChatHeaderName();
  const headerStatus = getChatHeaderStatus();
  const initialChar = headerName.charAt(0).toUpperCase();

  return (
    <div className="w-full h-full flex flex-col bg-slate-50 dark:bg-slate-950 transition-colors duration-200">
      {/* Header */}
      <div className="p-4 flex items-center justify-between bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 shadow-sm z-10">
        <div className="flex items-center gap-3">
          <button 
            onClick={() => dispatch(setActiveChatId(null))}
            className="md:hidden p-1 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg cursor-pointer"
          >
            <ArrowLeft size={18} />
          </button>
          
          <div className="w-10 h-10 rounded-full bg-aura-teal-500/20 flex items-center justify-center font-bold text-aura-teal-600 select-none">
            {initialChar}
          </div>
          <div>
            <h3 className="font-bold text-slate-800 dark:text-slate-100 text-sm">
              {headerName}
            </h3>
            <div className="flex items-center gap-1">
              {activeChat?.type === 'DIRECT' && (
                <div className={`w-1.5 h-1.5 rounded-full ${
                  headerStatus === 'Online' ? 'bg-emerald-500' : 'bg-slate-400'
                }`} />
              )}
              <span className="text-[10px] text-slate-400">
                {headerStatus}
              </span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-1">
          <button 
            onClick={handlePinToggle}
            className={`p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer ${
              pins[activeChatId] ? 'text-aura-teal-500' : 'text-slate-500'
            }`}
            title={pins[activeChatId] ? 'Unpin chat' : 'Pin chat'}
          >
            <Pin size={18} className={pins[activeChatId] ? 'transform rotate-45' : ''} />
          </button>

          <button 
            onClick={handleMuteToggle}
            className={`p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer ${
              mutes[activeChatId] ? 'text-aura-teal-500' : 'text-slate-500'
            }`}
            title={mutes[activeChatId] ? 'Unmute chat' : 'Mute chat'}
          >
            <VolumeX size={18} />
          </button>
          
          <button
            onClick={() => dispatch(openModal('createPoll'))}
            className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
            title="Create Poll"
          >
            <BarChart2 size={18} />
          </button>
        </div>
      </div>

      {/* Messages viewport */}
      <div 
        ref={scrollContainerRef}
        className="flex-1 overflow-y-auto p-4 space-y-3 flex flex-col"
      >
        {messagesFetching && (
          <div className="flex justify-center p-2">
            <Loader size={16} className="animate-spin text-aura-teal-500" />
          </div>
        )}

        {messagesList.map((msg) => {
          const isMe = msg.senderId === currentUserId;
          const isDeleted = msg.deleted || false;

          return (
            <div 
              key={msg.id}
              className={`flex flex-col max-w-[80%] md:max-w-[70%] group ${
                isMe ? 'self-end items-end' : 'self-start items-start'
              }`}
            >
              {/* Sender Name (if not me) */}
              {!isMe && (
                <span className="text-[10px] text-slate-500 dark:text-slate-400 ml-2 mb-0.5">
                  {msg.senderId?.substring(0, 8)}
                </span>
              )}

              {/* Message Bubble Container */}
              <div 
                className={`p-3 rounded-2xl relative shadow-sm border ${
                  isMe 
                    ? 'bg-aura-teal-500 border-aura-teal-600 text-white rounded-tr-none' 
                    : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 text-slate-900 dark:text-slate-100 rounded-tl-none'
                }`}
              >
                {/* Reply Quote Display */}
                 {msg.replyToMessageId && (() => {
                  const parentMsg = messagesList.find(m => m.id === msg.replyToMessageId);
                  return (
                    <div className="bg-slate-200/50 dark:bg-slate-800/50 p-2 rounded-lg border-l-4 border-l-aura-teal-500 text-xs mb-2">
                      <p className="font-semibold text-[10px] opacity-75">Replying to message</p>
                      <p className="italic truncate">{parentMsg ? parentMsg.content : 'Message deleted or unavailable'}</p>
                    </div>
                  );
                })()}

                {/* Poll View Renders */}
                {msg.type === 'POLL' ? (
                  <PollView message={msg} />
                ) : (
                  <p className="text-xs md:text-sm whitespace-pre-wrap break-words">
                    {linkifyText(msg.content)}
                  </p>
                )}

                {/* Footer details */}
                <div className="flex items-center justify-end gap-1 mt-1">
                  <span className="text-[8px] opacity-60">
                    {msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                  </span>
                  
                  {isMe && !isDeleted && (
                    <span className="opacity-75">
                      {msg.readCount > 0 ? (
                        <CheckCheck size={10} className="text-cyan-300 animate-pulse" />
                      ) : (
                        <Check size={10} />
                      )}
                    </span>
                  )}
                </div>

                {/* Controls popover on hover */}
                 {!isDeleted && (
                  <div className={`hidden group-hover:flex items-center gap-1.5 absolute top-1/2 -translate-y-1/2 bg-white dark:bg-slate-800 p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 shadow-md z-20 ${
                    isMe ? 'right-full mr-2' : 'left-full ml-2'
                  }`}>
                    {/* Reaction trigger */}
                    <div className="relative">
                      <button 
                        onClick={() => setActiveReactionPickerMessageId(activeReactionPickerMessageId === msg.id ? null : msg.id)}
                        className="p-1 hover:bg-slate-100 dark:hover:bg-slate-700 rounded text-slate-500 cursor-pointer"
                        title="React"
                      >
                        <Smile size={12} />
                      </button>
                      
                      {activeReactionPickerMessageId === msg.id && (
                        <div className="absolute bottom-full mb-1 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 p-1 rounded-full shadow-lg flex gap-1 z-30">
                          {['👍', '❤️', '😂', '😮', '😢', '🙏'].map(emoji => (
                            <button
                              key={emoji}
                              onClick={() => {
                                handleReact(msg.id, emoji);
                                setActiveReactionPickerMessageId(null);
                              }}
                              className="hover:scale-125 transition text-xs p-1 cursor-pointer"
                            >
                              {emoji}
                            </button>
                          ))}
                        </div>
                      )}
                    </div>

                    <button 
                      onClick={() => setReplyingTo(msg)}
                      className="p-1 hover:bg-slate-100 dark:hover:bg-slate-700 rounded text-slate-500 cursor-pointer"
                      title="Reply"
                    >
                      <CornerUpLeft size={12} />
                    </button>
                    {isMe && (
                      <>
                        {/* 3-minute editable window */}
                        {msg.createdAt && (new Date().getTime() - new Date(msg.createdAt).getTime()) <= 3 * 60 * 1000 && (
                          <button 
                            onClick={() => {
                              setEditingMessage(msg);
                              setMessageText(msg.content);
                            }}
                            className="p-1 hover:bg-slate-100 dark:hover:bg-slate-700 rounded text-slate-500 cursor-pointer"
                            title="Edit"
                          >
                            <Edit3 size={12} />
                          </button>
                        )}
                        <button 
                          onClick={() => handleDelete(msg.id)}
                          className="p-1 hover:bg-red-50 dark:hover:bg-red-950/20 rounded text-red-500 cursor-pointer"
                          title="Delete"
                        >
                          <Trash2 size={12} />
                        </button>
                      </>
                    )}
                  </div>
                )}
              </div>

              {/* Reactions display */}
              {msg.reactions?.length > 0 && (
                <div className="flex gap-0.5 mt-0.5">
                  {msg.reactions.map((r, idx) => (
                    <span 
                      key={idx} 
                      className="text-[10px] bg-slate-100 dark:bg-slate-800 px-1 py-0.5 rounded-full shadow-sm select-none border border-slate-200/50 dark:border-slate-700/50"
                      title={r.userId}
                    >
                      {r.emoji}
                    </span>
                  ))}
                </div>
              )}
            </div>
          );
        })}
        
        <div ref={messagesEndRef} />
      </div>

      {/* Reply indicator banner */}
      {replyingTo && (
        <div className="bg-slate-100 dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 px-4 py-2 flex items-center justify-between text-xs text-slate-600 dark:text-slate-400">
          <div className="truncate pr-4 flex items-center gap-1.5">
            <CornerUpLeft size={14} className="text-aura-teal-500" />
            <span>Replying to message:</span>
            <span className="italic font-medium truncate">{replyingTo.content}</span>
          </div>
          <button 
            onClick={() => setReplyingTo(null)}
            className="p-1 text-slate-400 hover:text-slate-600 cursor-pointer"
          >
            <X size={14} />
          </button>
        </div>
      )}

      {/* Input controls form */}
      <form 
        onSubmit={handleSend}
        className="p-4 bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 flex items-center gap-3"
      >
        <button 
          type="button"
          onClick={() => handleReact(messagesList[messagesList.length - 1]?.id, '👍')}
          className="p-2 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl cursor-pointer"
          title="React thumbs up"
          disabled={!messagesList.length}
        >
          <Smile size={20} />
        </button>

        <input 
          type="text"
          value={messageText}
          onChange={handleInputChange}
          placeholder={editingMessage ? "Edit message..." : "Type your message here..."}
          className="flex-1 px-4 py-2.5 text-sm border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
        />

        <button 
          type="submit"
          disabled={sendLoading || (!messageText.trim() && !replyingTo)}
          className="p-2.5 bg-aura-teal-600 text-white rounded-xl shadow-md hover:bg-aura-teal-700 transition cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
        >
          <Send size={18} />
        </button>
      </form>
    </div>
  );
};

export default ChatPanel;
