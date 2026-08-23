import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useDispatch, useSelector } from 'react-redux';
import { profileSchema } from '../../utils/schemas';
import { 
  useGetProfileQuery, 
  useUpdateProfileMutation,
  useGetPreferencesQuery,
  useUpdatePreferencesMutation
} from '../../api/userApi';
import { 
  useGetUploadUrlMutation, 
  useConfirmUploadMutation 
} from '../../api/mediaApi';
import { updateUserProfileState } from '../../store/slices/authSlice';
import { closeModal } from '../../store/slices/uiSlice';
import { X, Settings, Loader, Upload, Check } from 'lucide-react';

const PRESETS_AVATARS = [
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Felix',
  'https://api.dicebear.com/7.x/bottts/svg?seed=Oscar',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Scooter',
  'https://api.dicebear.com/7.x/lorelei/svg?seed=Pepper',
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Jack',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Buddy'
];

const THEME_OPTIONS = [
  { name: 'Calm Teal', value: 'theme-teal', color: 'bg-teal-500' },
  { name: 'Sunset Rose', value: 'theme-rose', color: 'bg-rose-500' },
  { name: 'Lavender Haze', value: 'theme-lavender', color: 'bg-purple-500' },
  { name: 'Forest Green', value: 'theme-green', color: 'bg-emerald-500' },
  { name: 'Classic Doodle', value: 'theme-doodle', color: 'bg-amber-500' },
  { name: 'Classic Slate', value: 'theme-slate', color: 'bg-slate-500' }
];

export const ProfileModal = () => {
  const dispatch = useDispatch();
  const isOpen = useSelector((state) => state.ui.modals.profile);
  const currentUser = useSelector((state) => state.auth.user);

  // Sync profile details
  const { data: profileRes, isLoading: profileLoading } = useGetProfileQuery(undefined, { skip: !isOpen });
  const [updateProfile, { isLoading: updateLoading }] = useUpdateProfileMutation();

  // Sync preference details
  const { data: prefRes } = useGetPreferencesQuery(undefined, { skip: !isOpen });
  const [updatePreferences] = useUpdatePreferencesMutation();

  // File Upload Mutations
  const [getUploadUrl] = useGetUploadUrlMutation();
  const [confirmUpload] = useConfirmUploadMutation();

  const profile = profileRes?.data || currentUser || {};
  const [avatarUrlVal, setAvatarUrlVal] = useState(profile.avatarUrl || PRESETS_AVATARS[0]);
  const [selectedTheme, setSelectedTheme] = useState('theme-slate');
  const [uploadingFile, setUploadingFile] = useState(false);

  useEffect(() => {
    if (profile.avatarUrl) {
      setAvatarUrlVal(profile.avatarUrl);
    }
  }, [profile.avatarUrl]);

  useEffect(() => {
    if (prefRes?.data?.theme) {
      setSelectedTheme(prefRes.data.theme);
    }
  }, [prefRes]);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(profileSchema),
    values: {
      displayName: profile.displayName || '',
      username: profile.username || '',
      phoneNumber: profile.phoneNumber || '',
      avatarUrl: avatarUrlVal,
      bio: profile.bio || '',
      statusMessage: profile.statusMessage || '',
    }
  });

  const handleAvatarSelect = (url) => {
    setAvatarUrlVal(url);
    setValue('avatarUrl', url);
  };

  const handleCustomPhotoUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadingFile(true);
    try {
      // 1. Get mock pre-signed upload URL from media-service
      const uploadUrlRes = await getUploadUrl({
        fileName: file.name,
        fileType: file.type,
        fileSize: file.size
      }).unwrap();

      const { mediaId, uploadUrl } = uploadUrlRes.data;

      // 2. Put file content to mock storage endpoint
      await fetch(uploadUrl, {
        method: 'PUT',
        body: file,
        headers: {
          'Content-Type': file.type
        }
      });

      // 3. Confirm upload
      const confirmRes = await confirmUpload(mediaId).unwrap();
      const finalUrl = confirmRes.data.url;

      setAvatarUrlVal(finalUrl);
      setValue('avatarUrl', finalUrl);
    } catch (err) {
      console.error('Failed to upload custom avatar', err);
    } finally {
      setUploadingFile(false);
    }
  };

  const onSubmit = async (data) => {
    try {
      const profileData = { ...data, avatarUrl: avatarUrlVal };
      const res = await updateProfile(profileData).unwrap();
      dispatch(updateUserProfileState(res.data));

      // Save preference theme
      await updatePreferences({ 
        theme: selectedTheme,
        notificationsEnabled: prefRes?.data?.notificationsEnabled !== false,
        soundEnabled: prefRes?.data?.soundEnabled !== false
      }).unwrap();

      dispatch(closeModal('profile'));
    } catch (err) {
      console.error('Failed to update profile settings', err);
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      dispatch(closeModal('profile'));
    }
  };

  if (!isOpen) return null;

  return (
    <div onClick={handleBackdropClick} className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 p-6 flex flex-col max-h-[85vh] overflow-y-auto">
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
            
            {/* Avatar Section */}
            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">
                Profile Photo
              </label>
              
              <div className="flex items-center gap-4 mb-3">
                <img 
                  src={avatarUrlVal} 
                  alt="Avatar Preview" 
                  className="w-16 h-16 rounded-full border border-slate-200 dark:border-slate-800 object-cover bg-slate-100 dark:bg-slate-950" 
                />
                
                <label className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 rounded-xl text-xs font-semibold cursor-pointer transition">
                  {uploadingFile ? (
                    <Loader size={14} className="animate-spin" />
                  ) : (
                    <Upload size={14} />
                  )}
                  <span>Upload Photo</span>
                  <input 
                    type="file" 
                    accept="image/*" 
                    className="hidden" 
                    onChange={handleCustomPhotoUpload}
                    disabled={uploadingFile}
                  />
                </label>
              </div>

              {/* Presets Gallery */}
              <div className="bg-slate-50 dark:bg-slate-950 p-3 rounded-xl border border-slate-100 dark:border-slate-850">
                <span className="block text-[10px] text-slate-400 font-semibold mb-2 uppercase">Or Choose an Avatar Preset</span>
                <div className="grid grid-cols-6 gap-2">
                  {PRESETS_AVATARS.map((url, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => handleAvatarSelect(url)}
                      className={`relative rounded-full overflow-hidden border-2 aspect-square cursor-pointer bg-white transition hover:scale-105 ${
                        avatarUrlVal === url ? 'border-aura-teal-500 ring-2 ring-aura-teal-500/20' : 'border-transparent'
                      }`}
                    >
                      <img src={url} alt={`Preset ${idx + 1}`} className="w-full h-full object-cover" />
                      {avatarUrlVal === url && (
                        <div className="absolute inset-0 bg-aura-teal-500/20 flex items-center justify-center">
                          <Check size={14} className="text-aura-teal-600 font-bold" />
                        </div>
                      )}
                    </button>
                  ))}
                </div>
              </div>
            </div>

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
                Username
              </label>
              <input
                type="text"
                placeholder="johndoe"
                {...register('username')}
                className="w-full px-4 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
              />
              {errors.username && (
                <p className="text-[10px] text-red-500 mt-1">{errors.username.message}</p>
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

            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                Status Message
              </label>
              <input
                type="text"
                placeholder="Available"
                {...register('statusMessage')}
                className="w-full px-4 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
              />
              {errors.statusMessage && (
                <p className="text-[10px] text-red-500 mt-1">{errors.statusMessage.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                Bio
              </label>
              <textarea
                placeholder="Tell others about yourself..."
                rows={3}
                {...register('bio')}
                className="w-full px-4 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500 resize-none"
              />
              {errors.bio && (
                <p className="text-[10px] text-red-500 mt-1">{errors.bio.message}</p>
              )}
            </div>

            {/* Default Chat Theme Section */}
            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">
                Default Chat Theme
              </label>
              <div className="grid grid-cols-3 gap-2">
                {THEME_OPTIONS.map((themeObj) => (
                  <button
                    key={themeObj.value}
                    type="button"
                    onClick={() => setSelectedTheme(themeObj.value)}
                    className={`flex items-center gap-2 p-2 rounded-xl border text-left cursor-pointer transition ${
                      selectedTheme === themeObj.value 
                        ? 'border-aura-teal-500 bg-aura-teal-50/10' 
                        : 'border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-950'
                    }`}
                  >
                    <span className={`w-4 h-4 rounded-full ${themeObj.color} flex-shrink-0`} />
                    <span className="text-[10px] font-semibold text-slate-700 dark:text-slate-350 truncate">{themeObj.name}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={updateLoading || uploadingFile}
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
