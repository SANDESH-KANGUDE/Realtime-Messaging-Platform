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
  useGetChatsQuery,
  useUpdateChatThemeMutation
} from '../../api/chatApi';
import { 
  useGetFriendsQuery, 
  useGetUserProfileQuery,
  useGetPreferencesQuery
} from '../../api/userApi';
import { 
  useGetUploadUrlMutation, 
  useConfirmUploadMutation 
} from '../../api/mediaApi';
import { useLazySearchEntitiesQuery } from '../../api/searchApi';
import { 
  setActiveChatId, 
  toggleMuteState, 
  togglePinState 
} from '../../store/slices/chatSlice';
import { openModal } from '../../store/slices/uiSlice';
import { emitTyping, joinRoom, queryPresence } from '../../socket/socketClient';
import { sanitizeHtml, linkifyText } from '../../utils/sanitization';
import { useInfiniteScroll } from '../../hooks/useInfiniteScroll';
import PollView from './PollView';
import { 
  ArrowLeft, Search, Send, Smile, CornerUpLeft, 
  Trash2, Edit3, X, Pin, VolumeX, BarChart2, Check, CheckCheck, Loader,
  MessageSquare, Copy, Info, Paperclip, FileText, Palette
} from 'lucide-react';

const ChatHeaderName = ({ chat, currentUserId, friends }) => {
  if (!chat) return 'Conversation';
  if (chat.type === 'GROUP') return chat.title;

  const otherMember = chat.members?.find((m) => m.userId !== currentUserId);
  if (!otherMember) return 'Direct Chat';

  const { data: profileRes } = useGetUserProfileQuery(otherMember.userId, {
    skip: !otherMember.userId
  });

  const resolvedName = profileRes?.data?.displayName || profileRes?.data?.username || (() => {
    const friendInfo = friends.find(f => f.friendProfile?.userId === otherMember.userId || f.requesterId === otherMember.userId || f.addresseeId === otherMember.userId);
    return friendInfo?.friendProfile?.displayName || friendInfo?.friendProfile?.username || otherMember.userId;
  })();

  return resolvedName;
};

const ChatHeaderAvatarImage = ({ chat, currentUserId, friends }) => {
  if (!chat) return 'U';
  if (chat.type === 'GROUP') {
    if (chat.avatarUrl) {
      return <img src={chat.avatarUrl} alt="Group" className="w-full h-full object-cover rounded-full" />;
    }
    return 'G';
  }

  const otherMember = chat.members?.find((m) => m.userId !== currentUserId);
  if (!otherMember) return 'D';

  const { data: profileRes } = useGetUserProfileQuery(otherMember.userId, {
    skip: !otherMember.userId
  });

  const avatarUrl = profileRes?.data?.avatarUrl || (() => {
    const friendInfo = friends.find(f => f.friendProfile?.userId === otherMember.userId || f.requesterId === otherMember.userId || f.addresseeId === otherMember.userId);
    return friendInfo?.friendProfile?.avatarUrl;
  })();

  if (avatarUrl) {
    return <img src={avatarUrl} alt="Avatar" className="w-full h-full object-cover rounded-full" />;
  }

  const resolvedName = profileRes?.data?.displayName || profileRes?.data?.username || (() => {
    const friendInfo = friends.find(f => f.friendProfile?.userId === otherMember.userId || f.requesterId === otherMember.userId || f.addresseeId === otherMember.userId);
    return friendInfo?.friendProfile?.displayName || friendInfo?.friendProfile?.username || 'U';
  })();

  return resolvedName.charAt(0).toUpperCase();
};

const SenderName = ({ userId }) => {
  const { data: profileRes } = useGetUserProfileQuery(userId, {
    skip: !userId
  });
  return profileRes?.data?.displayName || profileRes?.data?.username || userId.substring(0, 8);
};

export const ChatPanel = () => {
  const dispatch = useDispatch();
  const activeChatId = useSelector((state) => state.chat.activeChatId);
  const currentUserId = useSelector((state) => state.auth.user?.userId);
  const pins = useSelector((state) => state.chat.pins);
  const mutes = useSelector((state) => state.chat.mutes);
  const onlineUsers = useSelector((state) => state.socket.onlineUsers);
  const typingUsers = useSelector((state) => state.socket.typingUsers);
  const socketConnected = useSelector((state) => state.socket.connected);

  const { data: chatsRes } = useGetChatsQuery();
  const { data: friendsRes } = useGetFriendsQuery();

  const chats = chatsRes?.data || [];
  const friends = friendsRes?.data || [];

  const activeChat = chats.find(c => c.id === activeChatId);

  const [messageText, setMessageText] = useState('');
  const [editingMessage, setEditingMessage] = useState(null);
  const [replyingTo, setReplyingTo] = useState(null);
  const [activeReactionPickerMessageId, setActiveReactionPickerMessageId] = useState(null);
  const [selectedMessageActionsId, setSelectedMessageActionsId] = useState(null);
  const [showMessageInfoId, setShowMessageInfoId] = useState(null);
  const typingTimeoutRef = useRef(null);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  
  const [attachedFile, setAttachedFile] = useState(null);
  const [uploadingFile, setUploadingFile] = useState(false);
  const [showThemePicker, setShowThemePicker] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const [updateChatTheme] = useUpdateChatThemeMutation();
  const { data: prefRes } = useGetPreferencesQuery();
  const [getUploadUrl] = useGetUploadUrlMutation();
  const [confirmUpload] = useConfirmUploadMutation();
  const [searchEntities, { data: searchResultsRes, isLoading: searchLoading }] = useLazySearchEntitiesQuery();

  useEffect(() => {
    if (searchQuery.trim().length >= 2) {
      const delayDebounce = setTimeout(() => {
        searchEntities({ q: searchQuery, type: 'MESSAGE' });
      }, 300);
      return () => clearTimeout(delayDebounce);
    }
  }, [searchQuery, searchEntities]);

  const scrollToMessage = (msgId) => {
    const el = document.getElementById(`msg-${msgId}`);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      el.classList.add('bg-yellow-500/20');
      setTimeout(() => {
        el.classList.remove('bg-yellow-500/20');
      }, 2000);
    } else {
      console.warn(`Message element msg-${msgId} not found in DOM`);
    }
  };

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

  // Query presence status of other user in direct chat
  useEffect(() => {
    let intervalId = null;
    if (activeChatId && activeChat?.type === 'DIRECT') {
      const otherMember = activeChat.members?.find((m) => m.userId !== currentUserId);
      if (otherMember?.userId) {
        queryPresence(otherMember.userId);
        intervalId = setInterval(() => {
          queryPresence(otherMember.userId);
        }, 4000);
      }
    }
    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [activeChatId, activeChat, currentUserId, socketConnected]);

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
  
  // Cleanup typing indicator when activeChatId changes or component unmounts
  useEffect(() => {
    return () => {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
        typingTimeoutRef.current = null;
      }
      if (activeChatId) {
        emitTyping(activeChatId, false);
      }
    };
  }, [activeChatId]);

  const handleInputChange = (e) => {
    setMessageText(e.target.value);
    
    // Typing indicators trigger
    emitTyping(activeChatId, true);
    
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }
    
    typingTimeoutRef.current = setTimeout(() => {
      emitTyping(activeChatId, false);
      typingTimeoutRef.current = null;
    }, 2000);
  };

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadingFile(true);
    try {
      const uploadUrlRes = await getUploadUrl({
        fileName: file.name,
        fileType: file.type,
        fileSize: file.size
      }).unwrap();

      const { mediaId, uploadUrl } = uploadUrlRes.data;

      await fetch(uploadUrl, {
        method: 'PUT',
        body: file,
        headers: {
          'Content-Type': file.type
        }
      });

      const confirmRes = await confirmUpload(mediaId).unwrap();
      const finalUrl = confirmRes.data.url;

      setAttachedFile({
        name: file.name,
        type: file.type,
        url: finalUrl
      });
    } catch (err) {
      console.error('Failed to upload attachment file', err);
    } finally {
      setUploadingFile(false);
    }
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!messageText.trim() && !replyingTo && !attachedFile) return;

    try {
      const sanitized = sanitizeHtml(messageText.trim());
      
      if (editingMessage) {
        await editMessage({ messageId: editingMessage.id, content: sanitized }).unwrap();
        setEditingMessage(null);
      } else {
        const payload = {
          chatId: activeChatId,
          type: attachedFile ? (attachedFile.type.startsWith('image/') ? 'IMAGE' : 'DOCUMENT') : 'TEXT',
          content: sanitized || attachedFile.name,
          mediaUrl: attachedFile ? attachedFile.url : null,
          replyToMessageId: replyingTo ? replyingTo.id : null
        };
        await sendMessage(payload).unwrap();
        setReplyingTo(null);
        setAttachedFile(null);
      }
      setMessageText('');
      setShowEmojiPicker(false);
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
        typingTimeoutRef.current = null;
      }
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
    const friendInfo = friends.find(f => f.friendProfile?.userId === otherMember.userId || f.requesterId === otherMember.userId || f.addresseeId === otherMember.userId);
    return friendInfo?.friendProfile ? (friendInfo.friendProfile.displayName || friendInfo.friendProfile.username) : otherMember.userId;
  };

  const getChatHeaderStatus = () => {
    if (!activeChat) return '';
    if (activeChat.type === 'GROUP') return 'Group Chat';
    const otherMember = activeChat.members?.find((m) => m.userId !== currentUserId);
    if (!otherMember) return 'Offline';
    const typers = (typingUsers[activeChatId] || []).filter(id => id !== currentUserId);
    if (typers.includes(otherMember.userId)) {
      return 'typing...';
    }
    const isOnline = onlineUsers[otherMember.userId] === 'ONLINE';
    return isOnline ? 'Online' : 'Offline';
  };

  const headerName = getChatHeaderName();
  const headerStatus = getChatHeaderStatus();
  const initialChar = headerName.charAt(0).toUpperCase();

  const myMemberEntry = activeChat?.members?.find(m => m.userId === currentUserId);
  const activeTheme = myMemberEntry?.theme || prefRes?.data?.theme || 'theme-slate';
  const themeClasses = {
    'theme-teal': 'bg-gradient-to-br from-teal-50 to-emerald-100/30 dark:from-teal-950/40 dark:to-slate-950',
    'theme-rose': 'bg-gradient-to-br from-rose-50 to-pink-100/30 dark:from-rose-950/40 dark:to-slate-950',
    'theme-lavender': 'bg-gradient-to-br from-purple-50 to-indigo-100/30 dark:from-purple-950/40 dark:to-slate-950',
    'theme-green': 'bg-gradient-to-br from-emerald-50 to-teal-100/30 dark:from-emerald-950/40 dark:to-slate-950',
    'theme-doodle': 'bg-gradient-to-br from-amber-50 to-orange-100/30 dark:from-amber-950/40 dark:to-slate-950',
    'theme-slate': 'bg-slate-50 dark:bg-slate-950'
  };
  const bgClass = themeClasses[activeTheme] || themeClasses['theme-slate'];

  const handleThemeChange = async (themeVal) => {
    try {
      await updateChatTheme({ chatId: activeChatId, theme: themeVal }).unwrap();
      setShowThemePicker(false);
    } catch (err) {
      console.error(err);
    }
  };

  const matchingSearchResults = searchResultsRes?.data?.filter(item => item.metadata === activeChatId) || [];

  return (
    <div className="w-full h-full flex flex-col relative bg-slate-50 dark:bg-slate-950 transition-colors duration-200">
      {/* Header */}
      <div className="p-4 flex items-center justify-between bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 shadow-sm z-10">
        <div className="flex items-center gap-3">
          <button 
            onClick={() => dispatch(setActiveChatId(null))}
            className="md:hidden p-1 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg cursor-pointer"
          >
            <ArrowLeft size={18} />
          </button>
          
          <div className="w-10 h-10 rounded-full overflow-hidden bg-aura-teal-500/20 flex items-center justify-center font-bold text-aura-teal-600 select-none">
            <ChatHeaderAvatarImage chat={activeChat} currentUserId={currentUserId} friends={friendsRes?.data || []} />
          </div>
          <div>
            <h3 className="font-bold text-slate-800 dark:text-slate-100 text-sm">
              <ChatHeaderName chat={activeChat} currentUserId={currentUserId} friends={friendsRes?.data || []} />
            </h3>
            <div className="flex items-center gap-1">
              {activeChat?.type === 'DIRECT' && (
                <div className={`w-1.5 h-1.5 rounded-full ${
                  headerStatus === 'Online' || headerStatus === 'typing...' ? 'bg-emerald-500 animate-pulse' : 'bg-slate-400'
                }`} />
              )}
              <span className="text-[10px] text-slate-400">
                {headerStatus}
              </span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-1 relative">
          {/* Search Trigger */}
          <button 
            onClick={() => {
              setShowSearch(!showSearch);
              if (showSearch) setSearchQuery('');
            }}
            className={`p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer ${
              showSearch ? 'text-aura-teal-500' : 'text-slate-500'
            }`}
            title="Search messages"
          >
            <Search size={18} />
          </button>

          {/* Theme Palette Dropdown */}
          <div className="relative">
            <button 
              onClick={() => setShowThemePicker(!showThemePicker)}
              className={`p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer ${
                showThemePicker ? 'text-aura-teal-500' : 'text-slate-500'
              }`}
              title="Change chat background theme"
            >
              <Palette size={18} />
            </button>
            {showThemePicker && (
              <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-lg p-2.5 z-30">
                <span className="block text-[10px] text-slate-450 dark:text-slate-400 font-semibold mb-2 uppercase tracking-wider">Chat Theme</span>
                <div className="grid grid-cols-2 gap-1.5">
                  {[
                    { name: 'Teal', value: 'theme-teal', color: 'bg-teal-500' },
                    { name: 'Rose', value: 'theme-rose', color: 'bg-rose-500' },
                    { name: 'Lavender', value: 'theme-lavender', color: 'bg-purple-500' },
                    { name: 'Green', value: 'theme-green', color: 'bg-emerald-500' },
                    { name: 'Doodle', value: 'theme-doodle', color: 'bg-amber-500' },
                    { name: 'Slate', value: 'theme-slate', color: 'bg-slate-500' }
                  ].map((t) => (
                    <button
                      key={t.value}
                      onClick={() => handleThemeChange(t.value)}
                      className={`flex items-center gap-1.5 p-1 text-[10px] font-semibold text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900 border rounded-lg cursor-pointer ${
                        activeTheme === t.value ? 'border-aura-teal-500 bg-aura-teal-50/10' : 'border-slate-100 dark:border-slate-750'
                      }`}
                    >
                      <span className={`w-3 h-3 rounded-full ${t.color} flex-shrink-0`} />
                      <span className="truncate">{t.name}</span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

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
          
          {activeChat?.type === 'GROUP' && (
            <button
              onClick={() => dispatch(openModal('createPoll'))}
              className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
              title="Create Poll"
            >
              <BarChart2 size={18} />
            </button>
          )}
        </div>
      </div>

      {/* Message Keyword Search Panel */}
      {showSearch && (
        <div className="bg-slate-100/80 dark:bg-slate-900/80 border-b border-slate-200 dark:border-slate-800 flex flex-col z-10">
          <div className="p-3 flex items-center gap-2">
            <Search size={14} className="text-slate-400" />
            <input 
              type="text"
              placeholder="Search words in this chat..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="flex-1 bg-transparent text-xs text-slate-800 dark:text-slate-200 outline-none placeholder:text-slate-400"
            />
            {searchQuery && (
              <button 
                onClick={() => setSearchQuery('')} 
                className="text-[10px] font-bold text-slate-455 hover:text-slate-600 dark:text-slate-400 dark:hover:text-slate-200"
              >
                Clear
              </button>
            )}
          </div>

          {/* Results list */}
          {searchQuery.trim().length >= 2 && (
            <div className="max-h-48 overflow-y-auto bg-white dark:bg-slate-950 border-t border-slate-200 dark:border-slate-850">
              {searchLoading ? (
                <div className="p-3 text-[10px] text-center text-slate-400 flex items-center justify-center gap-1.5">
                  <Loader size={12} className="animate-spin text-aura-teal-500" />
                  <span>Searching...</span>
                </div>
              ) : matchingSearchResults.length === 0 ? (
                <div className="p-3 text-[10px] text-center text-slate-400">No matching messages found</div>
              ) : (
                matchingSearchResults.map((res) => (
                  <div 
                    key={res.entityId} 
                    onClick={() => scrollToMessage(res.entityId)}
                    className="px-4 py-2.5 hover:bg-slate-50 dark:hover:bg-slate-900 border-b border-slate-100 dark:border-slate-900 text-[11px] cursor-pointer flex justify-between items-center transition"
                  >
                    <div className="flex-1 min-w-0 pr-4">
                      <p className="font-semibold text-slate-655 dark:text-slate-400 text-[9px] mb-0.5">{res.title}</p>
                      <p className="text-slate-700 dark:text-slate-200 truncate italic">"{res.snippet}"</p>
                    </div>
                    <span className="text-[8px] text-slate-400 flex-shrink-0">
                      {new Date(res.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric' })}
                    </span>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      )}

      {/* Messages viewport */}
      <div 
        ref={scrollContainerRef}
        className={`flex-1 overflow-y-auto p-4 space-y-3 flex flex-col transition-colors duration-300 ${bgClass}`}
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
              id={`msg-${msg.id}`}
              className={`flex flex-col max-w-[80%] md:max-w-[70%] group transition-all duration-300 rounded-2xl ${
                isMe ? 'self-end items-end' : 'self-start items-start'
              }`}
            >
              {/* Sender Name (only in GROUP chats, if not me) */}
              {!isMe && activeChat?.type === 'GROUP' && (
                <span className="text-[10px] font-semibold text-slate-500 dark:text-slate-400 ml-2 mb-0.5">
                  <SenderName userId={msg.senderId} />
                </span>
              )}

              {/* Message Bubble Container */}
              <div 
                onClick={() => setSelectedMessageActionsId(selectedMessageActionsId === msg.id ? null : msg.id)}
                className={`p-3 rounded-2xl relative shadow-sm border cursor-pointer ${
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

                {/* Media Files Rendering */}
                {msg.mediaUrl && (
                  msg.type === 'IMAGE' ? (
                    <div className="mb-2 rounded-xl overflow-hidden border border-slate-250/20 bg-slate-100 dark:bg-slate-950 max-w-[240px]">
                      <a href={msg.mediaUrl} target="_blank" rel="noopener noreferrer">
                        <img src={msg.mediaUrl} alt="Attachment" className="w-full h-auto max-h-[180px] object-cover hover:opacity-90 transition" />
                      </a>
                    </div>
                  ) : (
                    <a 
                      href={msg.mediaUrl} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className={`mb-2 p-2.5 rounded-xl flex items-center gap-2 text-xs font-bold border transition max-w-[240px] ${
                        isMe 
                          ? 'bg-white/10 hover:bg-white/20 border-white/20 text-cyan-200' 
                          : 'bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 border-slate-200 dark:border-slate-700 text-aura-teal-600 dark:text-aura-teal-400'
                      }`}
                    >
                      <FileText size={16} className="flex-shrink-0" />
                      <span className="truncate flex-1">{msg.content || 'Attached File'}</span>
                    </a>
                  )
                )}

                {/* Content View Renders */}
                {msg.type === 'POLL' ? (
                  <PollView message={msg} />
                ) : (
                  (!msg.mediaUrl || msg.type === 'IMAGE') && msg.content && (
                    <p className="text-xs md:text-sm whitespace-pre-wrap break-words">
                      {linkifyText(msg.content)}
                    </p>
                  )
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

                {/* Controls popover on click */}
                 {!isDeleted && selectedMessageActionsId === msg.id && (
                  <div 
                    onClick={(e) => e.stopPropagation()}
                    className={`flex items-center gap-1.5 absolute top-1/2 -translate-y-1/2 bg-white dark:bg-slate-800 p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 shadow-md z-20 ${
                      isMe ? 'right-full mr-2' : 'left-full ml-2'
                    }`}
                  >
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
                                setSelectedMessageActionsId(null);
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
                      onClick={() => {
                        setReplyingTo(msg);
                        setSelectedMessageActionsId(null);
                      }}
                      className="p-1 hover:bg-slate-100 dark:hover:bg-slate-700 rounded text-slate-500 cursor-pointer"
                      title="Reply"
                    >
                      <CornerUpLeft size={12} />
                    </button>

                    <button 
                      onClick={() => {
                        navigator.clipboard.writeText(msg.content);
                        setSelectedMessageActionsId(null);
                      }}
                      className="p-1 hover:bg-slate-100 dark:hover:bg-slate-700 rounded text-slate-500 cursor-pointer"
                      title="Copy"
                    >
                      <Copy size={12} />
                    </button>

                    <button 
                      onClick={() => {
                        setShowMessageInfoId(showMessageInfoId === msg.id ? null : msg.id);
                        setSelectedMessageActionsId(null);
                      }}
                      className="p-1 hover:bg-slate-100 dark:hover:bg-slate-700 rounded text-slate-500 cursor-pointer"
                      title="Message Info"
                    >
                      <Info size={12} />
                    </button>

                    {isMe && msg.createdAt && (new Date().getTime() - new Date(msg.createdAt).getTime()) <= 3 * 60 * 1000 && (
                      <>
                        <button 
                          onClick={() => {
                            setEditingMessage(msg);
                            setMessageText(msg.content);
                            setSelectedMessageActionsId(null);
                          }}
                          className="p-1 hover:bg-slate-100 dark:hover:bg-slate-700 rounded text-slate-500 cursor-pointer"
                          title="Edit"
                        >
                          <Edit3 size={12} />
                        </button>
                        <button 
                          onClick={() => {
                            handleDelete(msg.id);
                            setSelectedMessageActionsId(null);
                          }}
                          className="p-1 hover:bg-red-50 dark:hover:bg-red-950/20 rounded text-red-500 cursor-pointer"
                          title="Delete"
                        >
                          <Trash2 size={12} />
                        </button>
                      </>
                    )}
                  </div>
                )}

                {/* Message Info Box overlay */}
                {showMessageInfoId === msg.id && (
                  <div 
                    className={`absolute bottom-full mb-2 bg-slate-900 text-white text-[11px] p-3 rounded-xl shadow-xl border border-slate-800 z-30 min-w-[220px] cursor-default ${
                      isMe ? 'right-0' : 'left-0'
                    }`}
                    onClick={(e) => e.stopPropagation()}
                  >
                    <div className="flex justify-between items-center border-b border-slate-800 pb-1.5 mb-1.5 font-bold">
                      <span>Message Info</span>
                      <button 
                        onClick={() => setShowMessageInfoId(null)}
                        className="text-slate-400 hover:text-white text-xs font-bold"
                      >
                        ×
                      </button>
                    </div>
                    <div className="space-y-1.5">
                      <div className="flex justify-between gap-4">
                        <span className="text-slate-400">Sent:</span>
                        <span>{msg.createdAt ? new Date(msg.createdAt).toLocaleString() : 'N/A'}</span>
                      </div>
                      <div className="flex justify-between gap-4">
                        <span className="text-slate-400">Delivered:</span>
                        <span>{msg.createdAt ? new Date(msg.createdAt).toLocaleString() : 'N/A'}</span>
                      </div>
                      <div className="border-t border-slate-800 pt-1.5 mt-1.5">
                        <p className="text-slate-400 font-semibold mb-1">Read receipts:</p>
                        {!msg.readReceipts || msg.readReceipts.length === 0 ? (
                          <p className="text-slate-500 italic">No one has read this yet</p>
                        ) : (
                          <div className="max-h-[80px] overflow-y-auto space-y-1 pr-1">
                            {msg.readReceipts.map((receipt, idx) => (
                              <div key={idx} className="flex justify-between items-center gap-4">
                                <span className="truncate font-semibold text-aura-teal-400">
                                  <SenderName userId={receipt.userId} />
                                </span>
                                <span className="text-[10px] text-slate-400 flex-shrink-0">
                                  {receipt.readAt ? new Date(receipt.readAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                                </span>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
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

      {/* Emoji Picker Popover */}
      {showEmojiPicker && (
        <div className="absolute bottom-[70px] left-4 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-3 rounded-xl shadow-lg flex flex-wrap gap-2 w-64 z-50">
          {['😀', '😂', '😍', '👍', '🔥', '👏', '🎉', '❤️', '🙌', '🤔', '👀', '✨', '✔️', '😢', '😡', '😮', '💩', '🚀', '💯'].map((emoji) => (
            <button
              key={emoji}
              type="button"
              onClick={() => setMessageText((prev) => prev + emoji)}
              className="text-lg p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 rounded transition cursor-pointer"
            >
              {emoji}
            </button>
          ))}
        </div>
      )}

      {/* Attached file preview */}
      {attachedFile && (
        <div className="px-4 py-2 bg-slate-50 dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2 text-slate-700 dark:text-slate-200 font-semibold truncate max-w-[80%]">
            {attachedFile.type.startsWith('image/') ? (
              <img src={attachedFile.url} className="w-8 h-8 rounded object-cover border border-slate-200 dark:border-slate-700" alt="Preview" />
            ) : (
              <FileText size={16} className="text-aura-teal-555 text-aura-teal-500 flex-shrink-0" />
            )}
            <span className="truncate">{attachedFile.name}</span>
          </div>
          <button 
            type="button" 
            onClick={() => setAttachedFile(null)} 
            className="p-1 rounded hover:bg-slate-200 dark:hover:bg-slate-800 text-slate-500 cursor-pointer"
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
          onClick={() => setShowEmojiPicker(!showEmojiPicker)}
          className={`p-2 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl cursor-pointer ${
            showEmojiPicker ? 'text-aura-teal-500 bg-aura-teal-500/10' : 'text-slate-500'
          }`}
          title="Choose emoji"
        >
          <Smile size={20} />
        </button>

        <input 
          type="text"
          value={messageText}
          onChange={handleInputChange}
          onFocus={() => emitTyping(activeChatId, true)}
          onBlur={() => emitTyping(activeChatId, false)}
          placeholder={editingMessage ? "Edit message..." : "Type your message here..."}
          className="flex-1 px-4 py-2.5 text-sm border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
        />

        {/* Attachment option after textbox */}
        <label className="p-2 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 rounded-xl cursor-pointer transition flex items-center justify-center flex-shrink-0" title="Attach image or PDF">
          {uploadingFile ? (
            <Loader size={18} className="animate-spin text-aura-teal-500" />
          ) : (
            <Paperclip size={18} />
          )}
          <input 
            type="file" 
            accept="image/*,application/pdf" 
            className="hidden" 
            onChange={handleFileChange}
            disabled={uploadingFile}
          />
        </label>

        <button 
          type="submit"
          disabled={sendLoading || uploadingFile || (!messageText.trim() && !replyingTo && !attachedFile)}
          className="p-2.5 bg-aura-teal-600 text-white rounded-xl shadow-md hover:bg-aura-teal-700 transition cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
        >
          <Send size={18} />
        </button>
      </form>
    </div>
  );
};

export default ChatPanel;
