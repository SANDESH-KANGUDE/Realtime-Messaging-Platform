import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { 
  useGetChatsQuery, 
  useCreateDirectChatMutation,
  useCreateGroupChatMutation,
  useArchiveChatMutation,
  useUnarchiveChatMutation
} from '../../api/chatApi';
import { 
  useGetChatMessagesQuery,
  useGetUnreadCountsQuery,
  useMarkChatAsReadMutation
} from '../../api/messageApi';
import {
  useGetNotificationsQuery,
  useMarkNotificationAsReadMutation
} from '../../api/notificationApi';
import { 
  useGetProfileQuery,
  useGetFriendsQuery, 
  useGetFriendRequestsQuery,
  useAcceptFriendRequestMutation,
  useSendFriendRequestMutation,
  useLazySearchUsersQuery,
  useGetUserProfileQuery
} from '../../api/userApi';
import { 
  setActiveChatId, 
  setArchivedViewActive, 
  setSearchQuery,
  setUnreadCounts,
  clearUnreadCount
} from '../../store/slices/chatSlice';
import { toggleTheme } from '../../store/slices/themeSlice';
import { logOut } from '../../store/slices/authSlice';
import { openModal } from '../../store/slices/uiSlice';
import { disconnectSocket } from '../../socket/socketClient';
import { 
  MessageSquare, Settings, Users, LogOut, Sun, Moon, 
  Search, Pin, VolumeX, Archive, Plus, CheckCheck, UserPlus, Check,
  Loader, Bell
} from 'lucide-react';

const ChatTitle = ({ chat, currentUserId, friends }) => {
  if (chat.type === 'GROUP') {
    return chat.title;
  }

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

const ChatAvatarImage = ({ chat, currentUserId, friends }) => {
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

const ChatAvatarChar = ({ chat, currentUserId, friends }) => {
  if (chat.type === 'GROUP') return 'G';

  const otherMember = chat.members?.find((m) => m.userId !== currentUserId);
  if (!otherMember) return 'D';

  const { data: profileRes } = useGetUserProfileQuery(otherMember.userId, {
    skip: !otherMember.userId
  });

  const resolvedName = profileRes?.data?.displayName || profileRes?.data?.username || (() => {
    const friendInfo = friends.find(f => f.friendProfile?.userId === otherMember.userId || f.requesterId === otherMember.userId || f.addresseeId === otherMember.userId);
    return friendInfo?.friendProfile?.displayName || friendInfo?.friendProfile?.username || 'U';
  })();

  return resolvedName.charAt(0).toUpperCase();
};

const LastMessage = ({ chatId }) => {
  const { data: msgsRes } = useGetChatMessagesQuery({ chatId, page: 0, size: 1 }, {
    skip: !chatId
  });

  const lastMessage = msgsRes?.data?.content?.[0];
  if (!lastMessage) return 'No messages yet';

  if (lastMessage.deleted) return 'Message deleted';
  if (lastMessage.type === 'POLL') return '📊 Poll';
  return lastMessage.content;
};

export const Sidebar = () => {
  const dispatch = useDispatch();
  const activeChatId = useSelector((state) => state.chat.activeChatId);
  const archivedViewActive = useSelector((state) => state.chat.archivedViewActive);
  const mutes = useSelector((state) => state.chat.mutes);
  const pins = useSelector((state) => state.chat.pins);
  const unreadCounts = useSelector((state) => state.chat.unreadCounts) || {};
  const localSearchQuery = useSelector((state) => state.chat.searchQuery);
  const themeMode = useSelector((state) => state.theme.mode);
  const currentUser = useSelector((state) => state.auth.user);
  const typingUsers = useSelector((state) => state.socket.typingUsers);

  const [globalSearchActive, setGlobalSearchActive] = useState(false);
  const [globalSearchQuery, setGlobalSearchQuery] = useState('');
  const [groupTitle, setGroupTitle] = useState('');
  const [showNotifications, setShowNotifications] = useState(false);

  // RTK Query hooks
  const { data: profileRes } = useGetProfileQuery();
  const { data: chatsRes, isLoading: chatsLoading } = useGetChatsQuery(undefined, {
    pollingInterval: 4000, // Sync cache regularly
  });
  const { data: friendsRes } = useGetFriendsQuery(undefined, {
    pollingInterval: 4000,
  });
  const { data: requestsRes } = useGetFriendRequestsQuery(undefined, {
    pollingInterval: 4000,
  });
  const { data: unreadCountsRes } = useGetUnreadCountsQuery(undefined, {
    pollingInterval: 10000,
  });
  const [markChatAsRead] = useMarkChatAsReadMutation();

  const { data: notificationsRes } = useGetNotificationsQuery(undefined, {
    pollingInterval: 10000,
  });
  const [markNotificationAsRead] = useMarkNotificationAsReadMutation();

  React.useEffect(() => {
    if (unreadCountsRes?.data) {
      dispatch(setUnreadCounts(unreadCountsRes.data));
    }
  }, [unreadCountsRes, dispatch]);
  
  const [searchUsers, { data: searchUsersRes, isLoading: searchUsersLoading }] = useLazySearchUsersQuery();
  const [createDirectChat, { isLoading: createChatLoading }] = useCreateDirectChatMutation();
  const [acceptRequest] = useAcceptFriendRequestMutation();
  const [sendFriendRequest, { isLoading: sendRequestLoading }] = useSendFriendRequestMutation();
  const [archiveChat] = useArchiveChatMutation();
  const [unarchiveChat] = useUnarchiveChatMutation();

  const handleArchiveChat = async (chatId) => {
    try {
      await archiveChat(chatId).unwrap();
    } catch (err) {
      console.error('Failed to archive chat:', err);
    }
  };

  const handleUnarchiveChat = async (chatId) => {
    try {
      await unarchiveChat(chatId).unwrap();
    } catch (err) {
      console.error('Failed to unarchive chat:', err);
    }
  };

  const chats = chatsRes?.data || [];
  const friends = friendsRes?.data || [];
  const friendRequests = requestsRes?.data || [];

  // Filter and sort chats
  const filteredChats = chats
    .filter((chat) => {
      // Filter out archived status
      const isArchived = chat.archived || false;
      if (archivedViewActive) return isArchived;
      return !isArchived;
    })
    .filter((chat) => {
      // Local title filter
      if (!localSearchQuery.trim()) return true;
      const title = chat.type === 'GROUP' ? chat.title : getDirectChatName(chat);
      return title.toLowerCase().includes(localSearchQuery.toLowerCase());
    })
    .sort((a, b) => {
      // Sort by pinned first
      const pinA = pins[a.id] ? 1 : 0;
      const pinB = pins[b.id] ? 1 : 0;
      if (pinA !== pinB) return pinB - pinA;
      // Then by updatedAt
      return new Date(b.updatedAt) - new Date(a.updatedAt);
    });

  function getDirectChatName(chat) {
    if (chat.type === 'GROUP') return chat.title;
    const otherMember = chat.members?.find((m) => m.userId !== currentUser?.userId);
    if (!otherMember) return 'Direct Chat';
    
    // Look up this otherMember.userId in friends list to find their profile details!
    const friendInfo = friends.find(f => f.friendProfile?.userId === otherMember.userId || f.requesterId === otherMember.userId || f.addresseeId === otherMember.userId);
    if (friendInfo?.friendProfile) {
      return friendInfo.friendProfile.displayName || friendInfo.friendProfile.username;
    }
    return otherMember.userId; // fallback
  }

  const handleChatSelect = (chatId) => {
    dispatch(setActiveChatId(chatId));
    dispatch(clearUnreadCount(chatId));
    markChatAsRead(chatId);
  };

  const handleGlobalSearch = async (e) => {
    const val = e.target.value;
    setGlobalSearchQuery(val);
    if (val.trim().length > 1) {
      searchUsers(val);
    }
  };

  const handleStartDirectChat = async (userId) => {
    try {
      // Deduplicate locally: if direct chat already exists in our chats list, select it
      const existingChat = chats.find(
        (c) => c.type === 'DIRECT' && c.members?.some((m) => m.userId === userId)
      );
      if (existingChat) {
        dispatch(setActiveChatId(existingChat.id));
        setGlobalSearchActive(false);
        setGlobalSearchQuery('');
        return;
      }

      const res = await createDirectChat(userId).unwrap();
      dispatch(setActiveChatId(res.data.id));
      setGlobalSearchActive(false);
      setGlobalSearchQuery('');
    } catch (err) {
      console.error(err);
    }
  };

  const handleSendFriendRequest = async (userId) => {
    try {
      await sendFriendRequest(userId).unwrap();
    } catch (err) {
      console.error(err);
    }
  };

  const handleAcceptRequest = async (requestId) => {
    try {
      await acceptRequest(requestId).unwrap();
    } catch (err) {
      console.error(err);
    }
  };

  const handleLogout = () => {
    disconnectSocket();
    localStorage.removeItem('refreshToken');
    dispatch(logOut());
  };

  return (
    <div className="w-full h-full flex flex-col bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 transition-colors duration-200">
      {/* Header bar */}
      <div className="p-4 flex items-center justify-between border-b border-slate-200 dark:border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full overflow-hidden bg-aura-teal-600 flex items-center justify-center text-white font-bold text-lg select-none">
            {profileRes?.data?.avatarUrl ? (
              <img src={profileRes.data.avatarUrl} alt="Avatar" className="w-full h-full object-cover rounded-full" />
            ) : (
              (profileRes?.data?.username || currentUser?.username || currentUser?.email || 'U').charAt(0).toUpperCase()
            )}
          </div>
          <div>
            <h3 className="font-bold text-slate-800 dark:text-slate-100 text-sm">
              {profileRes?.data?.username || currentUser?.username || currentUser?.email?.split('@')[0] || 'User'}
            </h3>
            <span className="text-[10px] text-slate-500 dark:text-slate-400 block max-w-[150px] truncate">
              {profileRes?.data?.email || profileRes?.data?.phoneNumber || currentUser?.email || ''}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-1">
          <div className="relative">
            <button 
              onClick={() => setShowNotifications(!showNotifications)}
              className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer relative"
              title="Notifications"
            >
              <Bell size={18} />
              {notificationsRes?.data?.filter(n => !n.read).length > 0 && (
                <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-red-500 rounded-full animate-pulse border border-white" />
              )}
            </button>
            {showNotifications && (
              <div className="absolute -right-48 mt-2 w-72 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-xl z-50 p-2 max-h-80 overflow-y-auto">
                <h4 className="text-xs font-bold text-slate-700 dark:text-slate-200 p-2 border-b border-slate-100 dark:border-slate-700">Notifications</h4>
                {notificationsRes?.data?.length === 0 ? (
                  <p className="text-[10px] text-slate-400 p-4 text-center">No notifications yet</p>
                ) : (
                  notificationsRes?.data?.map((n) => (
                    <div key={n.id} className={`p-2 border-b border-slate-50 dark:border-slate-800/60 flex items-start justify-between gap-1.5 ${!n.read ? 'bg-slate-50 dark:bg-slate-900/40' : ''}`}>
                      <div className="flex-1 min-w-0">
                        <p className="text-[11px] font-semibold text-slate-800 dark:text-slate-100">{n.title}</p>
                        <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate">{n.body}</p>
                      </div>
                      {!n.read && (
                        <button 
                          onClick={() => markNotificationAsRead(n.id)}
                          className="text-[9px] text-aura-teal-600 hover:underline flex-shrink-0 cursor-pointer"
                        >
                          Mark read
                        </button>
                      )}
                    </div>
                  ))
                )}
              </div>
            )}
          </div>

          <button 
            onClick={() => dispatch(toggleTheme())}
            className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
            title="Toggle theme"
          >
            {themeMode === 'light' ? <Moon size={18} /> : <Sun size={18} />}
          </button>
          
          <button 
            onClick={() => dispatch(setArchivedViewActive(!archivedViewActive))}
            className={`p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer ${
              archivedViewActive ? 'text-aura-teal-600 bg-aura-teal-100/30' : ''
            }`}
            title="Archived Chats"
          >
            <Archive size={18} />
          </button>

          <button 
            onClick={() => dispatch(openModal('createGroup'))}
            className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
            title="Create Group"
          >
            <Plus size={18} />
          </button>

          <button 
            onClick={() => dispatch(openModal('profile'))}
            className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
            title="Profile settings"
          >
            <Settings size={18} />
          </button>

          <button 
            onClick={handleLogout}
            className="p-2 rounded-lg text-red-500 hover:bg-red-50 dark:hover:bg-red-950/20 cursor-pointer"
            title="Logout"
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>

      {/* Friend requests notification banner */}
      {friendRequests.length > 0 && (
        <button
          onClick={() => dispatch(openModal('friendRequests'))}
          className="bg-aura-teal-50 dark:bg-aura-teal-950/20 px-4 py-2 border-b border-aura-teal-100 dark:border-aura-teal-900/50 flex items-center justify-between text-xs font-semibold text-aura-teal-700 dark:text-aura-teal-400"
        >
          <span>{friendRequests.length} Pending Friend Requests</span>
          <span className="underline hover:text-aura-teal-800">View</span>
        </button>
      )}

      {/* Search box selection tabs */}
      <div className="px-4 py-2 flex gap-2">
        <button
          onClick={() => setGlobalSearchActive(false)}
          className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all ${
            !globalSearchActive
              ? 'bg-aura-teal-500 text-white'
              : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
          }`}
        >
          Chats
        </button>
        <button
          onClick={() => setGlobalSearchActive(true)}
          className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all ${
            globalSearchActive
              ? 'bg-aura-teal-500 text-white'
              : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
          }`}
        >
          Find Users
        </button>
      </div>

      {/* Search Bar */}
      <div className="p-4 pt-1">
        <div className="relative flex items-center">
          <Search size={16} className="absolute left-3 text-slate-400" />
          {globalSearchActive ? (
            <input
              type="text"
              placeholder="Search global users..."
              value={globalSearchQuery}
              onChange={handleGlobalSearch}
              className="w-full pl-9 pr-4 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
            />
          ) : (
            <input
              type="text"
              placeholder="Search current chats..."
              value={localSearchQuery}
              onChange={(e) => dispatch(setSearchQuery(e.target.value))}
              className="w-full pl-9 pr-4 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
            />
          )}
        </div>
      </div>

      {/* Main List */}
      <div className="flex-1 overflow-y-auto">
        {globalSearchActive ? (
          /* Global Users List */
          <div className="space-y-1 p-2">
            {searchUsersLoading && (
              <div className="flex justify-center p-4">
                <Loader size={18} className="animate-spin text-aura-teal-500" />
              </div>
            )}
            
            {searchUsersRes?.data?.map((user) => {
              const isMe = user.userId === currentUser?.userId;
              const isFriend = friends.some((f) => f.status === 'ACCEPTED' && (f.friendProfile?.userId === user.userId || f.requesterId === user.userId || f.addresseeId === user.userId));
              const isPending = friendRequests.some((r) => r.requesterId === user.userId || r.addresseeId === user.userId) || 
                                friends.some((f) => f.status === 'PENDING' && (f.requesterId === user.userId || f.addresseeId === user.userId));

              return (
                <div 
                  key={user.userId} 
                  className="flex items-center justify-between p-3 hover:bg-slate-50 dark:hover:bg-slate-800/40 rounded-xl transition"
                >
                  <div className="flex items-center gap-3 truncate">
                    <div className="w-9 h-9 rounded-full bg-slate-200 dark:bg-slate-800 flex items-center justify-center font-bold text-slate-600 dark:text-slate-400 flex-shrink-0">
                      {user.displayName?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <div className="truncate">
                      <h4 className="text-xs font-semibold text-slate-800 dark:text-slate-100 truncate">
                        {user.displayName || user.username}
                        {isMe && <span className="text-[10px] text-slate-400 ml-1">(You)</span>}
                      </h4>
                      <p className="text-[10px] text-slate-500 truncate">{user.email}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 flex-shrink-0">
                    {isMe ? null : isFriend ? (
                      <button
                        onClick={() => handleStartDirectChat(user.userId)}
                        disabled={createChatLoading}
                        className="p-1 px-3 bg-aura-teal-500 text-white rounded text-[10px] font-bold cursor-pointer hover:bg-aura-teal-600 disabled:opacity-50"
                      >
                        Chat
                      </button>
                    ) : isPending ? (
                      <span className="p-1 px-3 bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-500 rounded text-[10px] font-semibold select-none">
                        Pending
                      </span>
                    ) : (
                      <button
                        onClick={() => handleSendFriendRequest(user.userId)}
                        disabled={sendRequestLoading}
                        className="p-1.5 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 rounded hover:bg-slate-200 dark:hover:bg-slate-700 cursor-pointer flex items-center gap-1 text-[10px] disabled:opacity-50"
                        title="Add Friend"
                      >
                        <UserPlus size={12} />
                        Add Friend
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
            
            {globalSearchQuery.trim() && searchUsersRes?.data?.length === 0 && (
              <p className="text-center text-xs text-slate-400 mt-4">No users found matching query</p>
            )}
          </div>
        ) : (
          /* Chats List */
          <div className="space-y-0.5">
            {chatsLoading && (
              <div className="flex justify-center p-4">
                <Loader size={18} className="animate-spin text-aura-teal-500" />
              </div>
            )}

            {filteredChats.map((chat) => {
              const chatTitle = chat.type === 'GROUP' ? chat.title : getDirectChatName(chat);
              const isActive = chat.id === activeChatId;
              const isMuted = mutes[chat.id];
              const isPinned = pins[chat.id];
              
              // Find typing indicators
              const typers = (typingUsers[chat.id] || []).filter(id => id !== currentUser?.userId);
              const isSomeoneTyping = typers.length > 0;
              if (typingUsers[chat.id] && typingUsers[chat.id].length > 0) {
                console.log(`[Sidebar] Chat ID: ${chat.id}, typingUsers:`, typingUsers[chat.id], 'currentUser.userId:', currentUser?.userId, 'typers after filter:', typers, 'isSomeoneTyping:', isSomeoneTyping);
              }
              const chatUnreadCount = unreadCounts[chat.id] || 0;

              return (
                <div
                  key={chat.id}
                  onClick={() => handleChatSelect(chat.id)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      handleChatSelect(chat.id);
                    }
                  }}
                  className={`w-full text-left p-3.5 flex items-center justify-between border-b border-slate-100 dark:border-slate-800/40 hover:bg-slate-50 dark:hover:bg-slate-800/20 transition-all cursor-pointer ${
                    isActive 
                      ? 'bg-aura-teal-500/10 dark:bg-aura-teal-500/5 border-l-4 border-l-aura-teal-500' 
                      : chatUnreadCount > 0 
                        ? 'bg-slate-100/60 dark:bg-slate-800/40 border-l-4 border-l-aura-teal-400/50 shadow-sm' 
                        : 'border-l-4 border-l-transparent'
                  }`}
                >
                  <div className="flex items-center gap-3 truncate flex-1 mr-2">
                    <div className="w-10 h-10 rounded-full overflow-hidden bg-slate-200 dark:bg-slate-800 flex items-center justify-center font-bold text-slate-600 dark:text-slate-400 select-none flex-shrink-0">
                      <ChatAvatarImage chat={chat} currentUserId={currentUser?.userId} friends={friends} />
                    </div>
                    <div className="truncate flex-1">
                      <div className="flex items-center gap-2">
                        <h4 className={`text-xs truncate flex-1 ${
                          chatUnreadCount > 0 ? 'font-black text-slate-900 dark:text-white' : 'font-bold text-slate-800 dark:text-slate-100'
                        }`}>
                          <ChatTitle chat={chat} currentUserId={currentUser?.userId} friends={friends} />
                        </h4>
                        {isMuted && <VolumeX size={12} className="text-slate-400" />}
                        {isPinned && <Pin size={12} className="text-aura-teal-500 transform rotate-45" />}
                      </div>
                      
                      {isSomeoneTyping ? (
                        <p className="text-[10px] text-aura-teal-500 font-semibold animate-pulse italic mt-0.5">
                          typing...
                        </p>
                      ) : (
                        <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate mt-0.5">
                          <LastMessage chatId={chat.id} />
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="flex flex-col items-end gap-1.5 flex-shrink-0">
                    <span className="text-[9px] text-slate-400">
                      {chat.updatedAt ? new Date(chat.updatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                    </span>
                    <div className="flex items-center gap-1.5">
                      {chatUnreadCount > 0 && (
                        <span className="text-[9px] bg-aura-teal-500 text-white rounded-full min-w-[16px] h-4 px-1 flex items-center justify-center font-bold">
                          {chatUnreadCount}
                        </span>
                      )}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          if (chat.archived) {
                            handleUnarchiveChat(chat.id);
                          } else {
                            handleArchiveChat(chat.id);
                          }
                        }}
                        className="p-1 hover:text-aura-teal-500 text-slate-400 cursor-pointer rounded hover:bg-slate-100 dark:hover:bg-slate-800"
                        title={chat.archived ? "Unarchive Chat" : "Archive Chat"}
                      >
                        <Archive size={12} className={chat.archived ? "text-aura-teal-500" : ""} />
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}

            {filteredChats.length === 0 && !chatsLoading && (
              <div className="text-center p-8 text-xs text-slate-400">
                {archivedViewActive ? 'No archived chats' : 'No active chats. Tap Find Users to start talking!'}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Sidebar;
