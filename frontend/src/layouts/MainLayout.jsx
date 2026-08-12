import React from 'react';
import { useSelector } from 'react-redux';
import Sidebar from '../components/chat/Sidebar';
import ChatPanel from '../components/chat/ChatPanel';
import CreateGroupModal from '../components/common/CreateGroupModal';
import CreatePollModal from '../components/common/CreatePollModal';
import ProfileModal from '../components/common/ProfileModal';
import FriendRequestsModal from '../components/common/FriendRequestsModal';

export const MainLayout = () => {
  const activeChatId = useSelector((state) => state.chat.activeChatId);

  return (
    <div className="w-screen h-screen flex overflow-hidden bg-slate-50 dark:bg-slate-950 transition-colors duration-200">
      {/* Responsive layout containers */}
      {/* Sidebar container */}
      <div 
        className={`h-full w-full md:w-[30%] md:min-w-[320px] md:max-w-[400px] flex-shrink-0 ${
          activeChatId ? 'hidden md:block' : 'block'
        }`}
      >
        <Sidebar />
      </div>

      {/* Main chat panel container */}
      <div 
        className={`h-full flex-1 ${
          activeChatId ? 'block' : 'hidden md:block'
        }`}
      >
        <ChatPanel />
      </div>

      {/* Global Modals Overlays */}
      <CreateGroupModal />
      <CreatePollModal />
      <ProfileModal />
      <FriendRequestsModal />
    </div>
  );
};

export default MainLayout;
