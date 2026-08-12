import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useDispatch, useSelector } from 'react-redux';
import { profileSchema } from '../../utils/schemas';
import { useGetProfileQuery, useUpdateProfileMutation } from '../../api/userApi';
import { updateUserProfileState } from '../../store/slices/authSlice';
import { closeModal } from '../../store/slices/uiSlice';
import { X, Settings, Loader } from 'lucide-react';

export const ProfileModal = () => {
  const dispatch = useDispatch();
  const isOpen = useSelector((state) => state.ui.modals.profile);
  const currentUser = useSelector((state) => state.auth.user);

  // Sync profile details
  const { data: profileRes, isLoading: profileLoading } = useGetProfileQuery(undefined, { skip: !isOpen });
  const [updateProfile, { isLoading: updateLoading }] = useUpdateProfileMutation();

  const profile = profileRes?.data || currentUser || {};

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(profileSchema),
    values: {
      displayName: profile.displayName || '',
      phoneNumber: profile.phoneNumber || '',
    }
  });

  const onSubmit = async (data) => {
    try {
      const res = await updateProfile(data).unwrap();
      dispatch(updateUserProfileState(res.data));
      dispatch(closeModal('profile'));
    } catch (err) {
      console.error('Failed to update profile settings', err);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 p-6 flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800 mb-4 flex-shrink-0">
          <div className="flex items-center gap-2">
            <Settings className="text-aura-teal-500" size={20} />
            <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100">Profile Settings</h3>
          </div>
          <button 
            onClick={() => dispatch(closeModal('profile'))}
            className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {profileLoading ? (
          <div className="flex justify-center p-8">
            <Loader className="animate-spin text-aura-teal-500" size={24} />
          </div>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                Display Name
              </label>
              <input
                type="text"
                placeholder="John Doe"
                {...register('displayName')}
                className="w-full px-4 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
              />
              {errors.displayName && (
                <p className="text-[10px] text-red-500 mt-1">{errors.displayName.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                Phone Number
              </label>
              <input
                type="text"
                placeholder="+1234567890"
                {...register('phoneNumber')}
                className="w-full px-4 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
              />
              {errors.phoneNumber && (
                <p className="text-[10px] text-red-500 mt-1">{errors.phoneNumber.message}</p>
              )}
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={updateLoading}
              className="w-full py-2.5 bg-aura-teal-600 hover:bg-aura-teal-700 text-white font-bold rounded-xl text-xs transition flex items-center justify-center gap-1.5 cursor-pointer disabled:opacity-50"
            >
              {updateLoading ? <Loader size={16} className="animate-spin" /> : 'Save Changes'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default ProfileModal;
