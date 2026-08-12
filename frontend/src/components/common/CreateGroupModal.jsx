import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useDispatch, useSelector } from 'react-redux';
import { groupSchema } from '../../utils/schemas';
import { useCreateGroupChatMutation } from '../../api/chatApi';
import { useGetFriendsQuery } from '../../api/userApi';
import { closeModal } from '../../store/slices/uiSlice';
import { setActiveChatId } from '../../store/slices/chatSlice';
import { X, Users, Loader } from 'lucide-react';

export const CreateGroupModal = () => {
  const dispatch = useDispatch();
  const isOpen = useSelector((state) => state.ui.modals.createGroup);
  const [selectedFriends, setSelectedFriends] = useState([]);
  
  const [createGroup, { isLoading }] = useCreateGroupChatMutation();
  const { data: friendsRes } = useGetFriendsQuery();
  const friends = friendsRes?.data || [];

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(groupSchema),
    defaultValues: {
      title: '',
      avatarUrl: '',
    }
  });

  const toggleSelectFriend = (friendId) => {
    setSelectedFriends((prev) => {
      if (prev.includes(friendId)) {
        return prev.filter((id) => id !== friendId);
      } else {
        return [...prev, friendId];
      }
    });
  };

  const onSubmit = async (data) => {
    try {
      const payload = {
        title: data.title.trim(),
        avatarUrl: data.avatarUrl?.trim() || null,
        memberUserIds: selectedFriends,
      };

      const res = await createGroup(payload).unwrap();
      dispatch(setActiveChatId(res.data.id));
      handleClose();
    } catch (err) {
      console.error(err);
    }
  };

  const handleClose = () => {
    reset();
    setSelectedFriends([]);
    dispatch(closeModal('createGroup'));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 p-6 flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800 mb-4 flex-shrink-0">
          <div className="flex items-center gap-2">
            <Users className="text-aura-teal-500" size={20} />
            <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100">Create New Group</h3>
          </div>
          <button 
            onClick={handleClose}
            className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {/* Content Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 overflow-y-auto pr-1 flex-1">
          <div>
            <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
              Group Title
            </label>
            <input
              type="text"
              placeholder="e.g. Project Aura Devs"
              {...register('title')}
              className="w-full px-4 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
            />
            {errors.title && (
              <p className="text-[10px] text-red-500 mt-1">{errors.title.message}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
              Avatar URL (Optional)
            </label>
            <input
              type="text"
              placeholder="https://example.com/avatar.jpg"
              {...register('avatarUrl')}
              className="w-full px-4 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
            />
            {errors.avatarUrl && (
              <p className="text-[10px] text-red-500 mt-1">{errors.avatarUrl.message}</p>
            )}
          </div>

          {/* Select Members Section */}
          <div className="flex flex-col flex-1">
            <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">
              Select Group Members ({selectedFriends.length} selected)
            </label>
            <div className="border border-slate-200 dark:border-slate-800 rounded-xl overflow-y-auto max-h-[160px] divide-y divide-slate-100 dark:divide-slate-800 bg-slate-50 dark:bg-slate-950">
              {friends.map((friend) => {
                const friendUserId = friend.friendProfile?.userId;
                if (!friendUserId) return null;
                const isChecked = selectedFriends.includes(friendUserId);
                
                return (
                  <button
                    key={friendUserId}
                    type="button"
                    onClick={() => toggleSelectFriend(friendUserId)}
                    className="w-full p-2.5 flex items-center justify-between text-left hover:bg-slate-100 dark:hover:bg-slate-900 text-xs transition cursor-pointer"
                  >
                    <div className="flex items-center gap-2 truncate">
                      <div className="w-8 h-8 rounded-full bg-slate-200 dark:bg-slate-800 flex items-center justify-center font-bold text-slate-500">
                        {friend.friendProfile?.displayName?.charAt(0).toUpperCase() || 'U'}
                      </div>
                      <span className="font-semibold text-slate-700 dark:text-slate-300 truncate">
                        {friend.friendProfile?.displayName || friend.friendProfile?.username || 'Unknown Friend'}
                      </span>
                    </div>
                    <div className={`w-4 h-4 rounded border flex items-center justify-center transition-all ${
                      isChecked ? 'bg-aura-teal-500 border-aura-teal-600' : 'border-slate-300 dark:border-slate-700'
                    }`}>
                      {isChecked && <div className="w-1.5 h-1.5 bg-white rounded-full" />}
                    </div>
                  </button>
                );
              })}

              {friends.length === 0 && (
                <p className="text-center text-xs text-slate-400 py-6">No friends available to add</p>
              )}
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={isLoading || selectedFriends.length === 0}
            className="w-full py-2.5 bg-aura-teal-600 hover:bg-aura-teal-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex-shrink-0"
          >
            {isLoading ? <Loader size={16} className="animate-spin" /> : 'Create Group'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateGroupModal;
