import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useGetFriendRequestsQuery, useAcceptFriendRequestMutation } from '../../api/userApi';
import { useCreateDirectChatMutation } from '../../api/chatApi';
import { closeModal } from '../../store/slices/uiSlice';
import { X, UserCheck, Loader } from 'lucide-react';

export const FriendRequestsModal = () => {
  const dispatch = useDispatch();
  const isOpen = useSelector((state) => state.ui.modals.friendRequests);

  const { data: requestsRes, isLoading } = useGetFriendRequestsQuery(undefined, { skip: !isOpen });
  const [acceptRequest, { isLoading: acceptLoading }] = useAcceptFriendRequestMutation();
  const [createDirectChat] = useCreateDirectChatMutation();

  const requests = requestsRes?.data || [];

  const handleAccept = async (id, requesterId) => {
    try {
      await acceptRequest(id).unwrap();
      if (requesterId) {
        await createDirectChat(requesterId).unwrap();
      }
    } catch (err) {
      console.error(err);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 p-6 flex flex-col max-h-[80vh]">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800 mb-4 flex-shrink-0">
          <div className="flex items-center gap-2">
            <UserCheck className="text-aura-teal-500" size={20} />
            <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100">Friend Requests</h3>
          </div>
          <button 
            onClick={() => dispatch(closeModal('friendRequests'))}
            className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {/* Content list */}
        <div className="flex-1 overflow-y-auto space-y-2 pr-1">
          {isLoading && (
            <div className="flex justify-center p-4">
              <Loader size={20} className="animate-spin text-aura-teal-500" />
            </div>
          )}

          {requests.map((req) => (
            <div 
              key={req.id} 
              className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-950 rounded-xl border border-slate-200/50 dark:border-slate-800/50"
            >
              <div>
                <h4 className="text-xs font-bold text-slate-800 dark:text-slate-100">
                  {req.friendProfile?.displayName || req.friendProfile?.username || req.requesterId?.substring(0, 8)}
                </h4>
                <span className="text-[10px] text-slate-400">Sent a connection request</span>
              </div>

              <button
                onClick={() => handleAccept(req.id, req.requesterId)}
                disabled={acceptLoading}
                className="px-3 py-1 bg-aura-teal-500 hover:bg-aura-teal-600 text-white rounded text-[10px] font-bold transition flex items-center gap-1 cursor-pointer disabled:opacity-50"
              >
                Accept
              </button>
            </div>
          ))}

          {requests.length === 0 && !isLoading && (
            <p className="text-center text-xs text-slate-400 py-8 select-none">No pending friend requests</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default FriendRequestsModal;
