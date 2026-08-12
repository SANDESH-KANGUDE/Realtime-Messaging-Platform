import React from 'react';
import { useVotePollMutation } from '../../api/messageApi';
import { useSelector } from 'react-redux';
import { Check } from 'lucide-react';

export const PollView = ({ message }) => {
  const [votePoll, { isLoading }] = useVotePollMutation();
  const currentUserId = useSelector((state) => state.auth.user?.userId);

  const { pollQuestion, pollOptions = [], pollVotes = [] } = message;

  // Calculate total votes and vote count per option
  const totalVotes = pollVotes.length;
  
  const optionVotesCount = pollOptions.map((_, index) => {
    return pollVotes.filter((v) => v.optionIndex === index).length;
  });

  const userVoteOptionIndex = pollVotes.find((v) => v.userId === currentUserId)?.optionIndex;

  const handleVote = async (optionIndex) => {
    if (isLoading) return;
    try {
      await votePoll({ messageId: message.id, optionIndex }).unwrap();
    } catch (err) {
      console.error('Failed to submit poll vote', err);
    }
  };

  return (
    <div className="w-full bg-slate-100/50 dark:bg-slate-900/40 p-4 rounded-xl border border-slate-200/50 dark:border-slate-800/50 my-2 space-y-3">
      <h4 className="font-bold text-slate-800 dark:text-slate-200 text-sm md:text-base">
        {pollQuestion}
      </h4>
      <div className="space-y-2">
        {pollOptions.map((option, index) => {
          const voteCount = optionVotesCount[index] || 0;
          const percentage = totalVotes > 0 ? Math.round((voteCount / totalVotes) * 100) : 0;
          const isSelected = userVoteOptionIndex === index;

          return (
            <button
              key={index}
              onClick={() => handleVote(index)}
              disabled={isLoading}
              className={`w-full text-left p-3 rounded-lg border transition-all duration-150 flex flex-col relative overflow-hidden cursor-pointer ${
                isSelected
                  ? 'border-aura-teal-500 bg-aura-teal-500/10'
                  : 'border-slate-200 dark:border-slate-800 bg-white/40 dark:bg-slate-900/20 hover:bg-slate-100 dark:hover:bg-slate-800/40'
              }`}
            >
              {/* Progress visual background */}
              <div 
                className="absolute top-0 left-0 bottom-0 bg-aura-teal-500/10 transition-all duration-300"
                style={{ width: `${percentage}%` }}
              />

              <div className="flex justify-between items-center w-full z-10 text-sm font-semibold">
                <span className="text-slate-700 dark:text-slate-300 truncate mr-2">
                  {option}
                </span>
                <span className="text-xs text-slate-500 dark:text-slate-400 flex items-center gap-1">
                  {voteCount} votes ({percentage}%)
                  {isSelected && <Check size={14} className="text-aura-teal-500" />}
                </span>
              </div>
            </button>
          );
        })}
      </div>
      <div className="text-[10px] text-slate-400 text-right">
        Total: {totalVotes} votes
      </div>
    </div>
  );
};

export default PollView;
