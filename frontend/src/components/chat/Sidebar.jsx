import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { 
  useGetChatsQuery, 
  useCreateDirectChatMutation,
  useCreateGroupChatMutation 
} from '../../api/chatApi';
import { 
  useGetFriendsQuery, 
  useGetFriendRequestsQuery,
  useAcceptFriendRequestMutation,
  useSendFriendRequestMutation,
  useLazySearchUsersQuery
} from '../../api/userApi';
import { 
  setActiveChatId, 
  setArchivedViewActive, 
  setSearchQuery 
} from '../../store/slices/chatSlice';
import { toggleTheme } from '../../store/slices/themeSlice';
import { logOut } from '../../store/slices/authSlice';
import { openModal } from '../../store/slices/uiSlice';
import { disconnectSocket } from '../../socket/socketClient';
import { 
  MessageSquare, Settings, Users, LogOut, Sun, Moon, 
  Search, Pin, VolumeX, Archive, Plus, CheckCheck, UserPlus, Check,
  Loader
} from 'lucide-react';

export const Sidebar = () => {
  const dispatch = useDispatch();
  const activeChatId = useSelector((state) => state.chat.activeChatId);
  const archivedViewActive = useSelector((state) => state.chat.archivedViewActive);
  const mutes = useSelector((state) => state.chat.mutes);
  const pins = useSelector((state) => state.chat.pins);
  const localSearchQuery = useSelector((state) => state.chat.searchQuery);
  const themeMode = useSelector((state) => state.theme.mode);
  const currentUser = useSelector((state) => state.auth.user);
  const typingUsers = useSelector((state) => state.socket.typingUsers);

  const [globalSearchActive, setGlobalSearchActive] = useState(false);
  const [globalSearchQuery, setGlobalSearchQuery] = useState('');
  const [groupTitle, setGroupTitle] = useState('');

  // RTK Query hooks
  const { data: chatsRes, isLoading: chatsLoading } = useGetChatsQuery(undefined, {
    pollingInterval: 10000, // Sync cache regularly
  });
  const { data: friendsRes } = useGetFriendsQuery();
  const { data: requestsRes } = useGetFriendRequestsQuery();
  
  const [searchUsers, { data: searchUsersRes, isLoading: searchUsersLoading }] = useLazySearchUsersQuery();
  const [createDirectChat, { isLoading: createChatLoading }] = useCreateDirectChatMutation();
  const [acceptRequest] = useAcceptFriendRequestMutation();
  const [sendFriendRequest, { isLoading: sendRequestLoading }] = useSendFriendRequestMutation();

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
    const friendInfo = friends.find(f => f.friendProfile?.userId === otherMember.userId);
    if (friendInfo?.friendProfile) {
      return friendInfo.friendProfile.displayName || friendInfo.friendProfile.username;
    }
    return otherMember.userId; // fallback
  }

  const handleChatSelect = (chatId) => {
    dispatch(setActiveChatId(chatId));
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
    dispatch(logOut());
  };

  return (
    <div className="w-full h-full flex flex-col bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 transition-colors duration-200">
      {/* Header bar */}
      <div className="p-4 flex items-center justify-between border-b border-slate-200 dark:border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-aura-teal-600 flex items-center justify-center text-white font-bold text-lg select-none">
            {currentUser?.displayName?.charAt(0).toUpperCase() || 'U'}
          </div>
          <div>
            <h3 className="font-bold text-slate-800 dark:text-slate-100 text-sm">
              {currentUser?.displayName || currentUser?.username}
            </h3>
            <span className="text-[10px] bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded text-slate-500">
              {currentUser?.role?.replace('ROLE_', '')}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-1">
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
              const typers = typingUsers[chat.id] || [];
              const isSomeoneTyping = typers.length > 0;

              return (
                <button
                  key={chat.id}
                  onClick={() => handleChatSelect(chat.id)}
                  className={`w-full text-left p-3.5 flex items-center justify-between border-b border-slate-100 dark:border-slate-800/40 hover:bg-slate-50 dark:hover:bg-slate-800/20 transition-all ${
                    isActive ? 'bg-aura-teal-500/10 dark:bg-aura-teal-500/5 border-l-4 border-l-aura-teal-500' : 'border-l-4 border-l-transparent'
                  }`}
                >
                  <div className="flex items-center gap-3 truncate flex-1 mr-2">
                    <div className="w-10 h-10 rounded-full bg-slate-200 dark:bg-slate-800 flex items-center justify-center font-bold text-slate-600 dark:text-slate-400 select-none flex-shrink-0">
                      {chat.type === 'GROUP' ? 'G' : chatTitle.charAt(0).toUpperCase()}
                    </div>
                    <div className="truncate flex-1">
                      <div className="flex items-center gap-2">
                        <h4 className="text-xs font-bold text-slate-800 dark:text-slate-100 truncate">
                          {chatTitle}
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
                          {chat.lastMessage || 'No messages yet'}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="flex flex-col items-end gap-1.5 flex-shrink-0">
                    <span className="text-[9px] text-slate-400">
                      {chat.updatedAt ? new Date(chat.updatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                    </span>
                    {chat.unreadCount > 0 && (
                      <span className="text-[9px] bg-aura-teal-500 text-white rounded-full min-w-[16px] h-4 px-1 flex items-center justify-center font-bold">
                        {chat.unreadCount}
                      </span>
                    )}
                  </div>
                </button>
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
