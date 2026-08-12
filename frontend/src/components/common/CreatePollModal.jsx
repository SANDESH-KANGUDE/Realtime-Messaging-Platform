import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useSendMessageMutation } from '../../api/messageApi';
import { closeModal } from '../../store/slices/uiSlice';
import { X, Plus, Trash2, BarChart2, Loader } from 'lucide-react';

export const CreatePollModal = () => {
  const dispatch = useDispatch();
  const isOpen = useSelector((state) => state.ui.modals.createPoll);
  const activeChatId = useSelector((state) => state.chat.activeChatId);

  const [question, setQuestion] = useState('');
  const [options, setOptions] = useState(['', '']);
  const [errorMsg, setErrorMsg] = useState('');

  const [sendPoll, { isLoading }] = useSendMessageMutation();

  const handleAddOption = () => {
    if (options.length >= 10) return;
    setOptions([...options, '']);
  };

  const handleRemoveOption = (index) => {
    if (options.length <= 2) return;
    setOptions(options.filter((_, idx) => idx !== index));
  };

  const handleOptionChange = (index, value) => {
    const updated = [...options];
    updated[index] = value;
    setOptions(updated);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    if (question.trim().length < 5) {
      setErrorMsg('Question must be at least 5 characters');
      return;
    }

    const filteredOptions = options.map((opt) => opt.trim()).filter((opt) => opt.length > 0);
    if (filteredOptions.length < 2) {
      setErrorMsg('At least 2 non-empty options are required');
      return;
    }

    try {
      const payload = {
        chatId: activeChatId,
        type: 'POLL',
        pollQuestion: question.trim(),
        pollOptions: filteredOptions,
      };

      await sendPoll(payload).unwrap();
      handleClose();
    } catch (err) {
      console.error(err);
      setErrorMsg('Failed to create poll message');
    }
  };

  const handleClose = () => {
    setQuestion('');
    setOptions(['', '']);
    setErrorMsg('');
    dispatch(closeModal('createPoll'));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 p-6 flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800 mb-4 flex-shrink-0">
          <div className="flex items-center gap-2">
            <BarChart2 className="text-aura-teal-500" size={20} />
            <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100">Create Group Poll</h3>
          </div>
          <button 
            onClick={handleClose}
            className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {errorMsg && (
          <div className="bg-red-50 dark:bg-red-950/30 text-red-500 text-xs p-3 rounded-lg border border-red-200 dark:border-red-900/50 mb-4 flex-shrink-0">
            {errorMsg}
          </div>
        )}

        {/* Content Form */}
        <form onSubmit={handleSubmit} className="space-y-4 overflow-y-auto pr-1 flex-1">
          <div>
            <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
              Ask Question
            </label>
            <input
              type="text"
              placeholder="e.g. Which design direction should we take?"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              className="w-full px-4 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
              required
            />
          </div>

          <div className="space-y-2">
            <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
              Choices Options
            </label>
            
            <div className="space-y-2 max-h-[220px] overflow-y-auto pr-1">
              {options.map((option, index) => (
                <div key={index} className="flex items-center gap-2">
                  <input
                    type="text"
                    placeholder={`Option ${index + 1}`}
                    value={option}
                    onChange={(e) => handleOptionChange(index, e.target.value)}
                    className="flex-1 px-4 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-aura-teal-500"
                    required
                  />
                  {options.length > 2 && (
                    <button
                      type="button"
                      onClick={() => handleRemoveOption(index)}
                      className="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-950/20 rounded-lg cursor-pointer"
                      title="Remove Option"
                    >
                      <Trash2 size={14} />
                    </button>
                  )}
                </div>
              ))}
            </div>

            {options.length < 10 && (
              <button
                type="button"
                onClick={handleAddOption}
                className="w-full mt-2 py-2 flex items-center justify-center gap-1 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700/80 rounded-xl text-xs font-semibold cursor-pointer"
              >
                <Plus size={14} />
                Add Option Choice
              </button>
            )}
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-2.5 bg-aura-teal-600 hover:bg-aura-teal-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex-shrink-0"
          >
            {isLoading ? <Loader size={16} className="animate-spin" /> : 'Publish Poll'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreatePollModal;
