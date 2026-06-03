/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { motion } from 'motion/react';
import { Pin, Lock, Calendar, ListTodo, CircleCheck, Volume2, Tag } from 'lucide-react';
import { Note } from '../types';
import { PASTEL_THEMES } from '../themes';

interface NoteCardProps {
  key?: string;
  note: Note;
  onClick: () => void;
  onTogglePin: (e: React.MouseEvent) => void;
}

export default function NoteCard({ note, onClick, onTogglePin }: NoteCardProps) {
  const currentTheme = PASTEL_THEMES[note.colorKey] || PASTEL_THEMES.cream;

  // Render a human-friendly timestamp
  const formatTime = (time: number) => {
    const diff = Date.now() - time;
    if (diff < 60000) return 'Just now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
    
    const date = new Date(time);
    return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
  };

  const getChecklistSummary = () => {
    if (!note.isChecklistMode || !note.checklist.length) return null;
    const completed = note.checklist.filter((item) => item.done).length;
    return {
      done: completed,
      total: note.checklist.length,
      percentage: Math.round((completed / note.checklist.length) * 100)
    };
  };

  const checkInfo = getChecklistSummary();

  return (
    <motion.div
      layout
      whileHover={{ y: -4, scale: 1.01 }}
      whileTap={{ scale: 0.98 }}
      onClick={onClick}
      id={`note-card-${note.id}`}
      className={`relative w-full overflow-hidden p-5 rounded-[28px] border cursor-pointer group flex flex-col justify-between transition-all duration-300 shadow-sm hover:shadow-md ${currentTheme.bg} ${currentTheme.border} ${currentTheme.text}`}
    >
      {/* Top Banner with lock & pin togglers */}
      <div className="flex items-start justify-between gap-2 mb-2.5">
        <h3 className="font-bold text-base leading-tight tracking-tight line-clamp-2">
          {note.isLocked ? 'Confidential Safe Note' : note.title || 'Untitled note'}
        </h3>

        <div className="flex items-center gap-1 shrink-0">
          {note.isLocked && (
            <div className="p-1 px-1.5 bg-amber-500/10 text-amber-600 rounded-lg flex items-center justify-center">
              <Lock className="w-3.5 h-3.5 animate-pulse" />
            </div>
          )}

          {/* Pin Button */}
          <button
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              onTogglePin(e);
            }}
            id={`note-card-pin-btn-${note.id}`}
            className={`p-1.5 rounded-lg transition-transform ${
              note.isPinned
                ? 'opacity-100 text-[#4c3e34]'
                : 'opacity-0 group-hover:opacity-60 text-stone-400 hover:scale-110 hover:opacity-100'
            }`}
            title={note.isPinned ? 'Unpin' : 'Pin to top'}
          >
            <Pin className="w-3.5 h-3.5 fill-current" />
          </button>
        </div>
      </div>

      {/* Main Snippet Container */}
      <div className="flex-1 mb-4">
        {note.isLocked ? (
          /* Secure placeholder content so it does not leak sensitive information */
          <p className="text-xs italic text-stone-500/70 font-medium">
            This private note is encrypted under session lock. Tap and verify with security PIN to inspect and compile content.
          </p>
        ) : note.isChecklistMode ? (
          /* Checklist preview block */
          <div className="flex flex-col gap-1 my-1.5">
            {note.checklist.slice(0, 3).map((item) => (
              <div key={item.id} className="flex items-center gap-2 text-xs truncate">
                <span className={`w-3.5 h-3.5 rounded border border-black/10 shrink-0 flex items-center justify-center ${item.done ? 'bg-black/[0.06]' : 'bg-white/40'}`}>
                  {item.done && <span className="w-1.5 h-1.5 bg-stone-700 rounded-full" />}
                </span>
                <span className={`truncate ${item.done ? 'line-through opacity-40' : ''}`}>
                  {item.text}
                </span>
              </div>
            ))}
            {note.checklist.length > 3 && (
              <span className="text-[10px] text-stone-400 font-bold self-start mt-0.5">
                + {note.checklist.length - 3} more lists
              </span>
            )}
          </div>
        ) : (
          /* Plain raw text content preview snippet */
          <p className="text-xs leading-relaxed font-normal opacity-85 break-words line-clamp-5 whitespace-pre-wrap">
            {note.content}
          </p>
        )}
      </div>

      {/* Footer statistics & tags */}
      <footer className="mt-auto pt-2.5 border-t border-black/[0.03] flex flex-col gap-2">
        
        {/* Dynamic Badges like checklists, voice audio count */}
        <div className="flex flex-wrap items-center gap-2 text-[10px] uppercase font-bold tracking-wide">
          <div className="flex items-center gap-1 opacity-60">
            <Calendar className="w-3.5 h-3.5" />
            <span>{formatTime(note.timestamp)}</span>
          </div>

          {checkInfo && (
            <div className="flex items-center gap-1 text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded-md border border-emerald-100 font-mono">
              <CircleCheck className="w-3 h-3 text-emerald-600" />
              <span>{checkInfo.done}/{checkInfo.total} Done ({checkInfo.percentage}%)</span>
            </div>
          )}

          {note.voiceAttachments && note.voiceAttachments.length > 0 && (
            <div className="flex items-center gap-1.5 text-blue-700 bg-blue-50 px-1.5 py-0.5 rounded-md border border-blue-100 font-mono">
              <Volume2 className="w-3 h-3 text-blue-600" />
              <span>{note.voiceAttachments.length} Voice</span>
            </div>
          )}
        </div>

        {/* Display tags labels preview */}
        {note.tags && note.tags.length > 0 && (
          <div className="flex flex-wrap gap-1 max-w-full overflow-hidden">
            {note.tags.slice(0, 2).map((tag) => (
              <span
                key={tag}
                className="text-[9px] font-bold text-stone-500/80 bg-black/[0.03] border border-black/[0.01] px-1.5 py-0.5 rounded-md truncate max-w-[80px]"
              >
                #{tag}
              </span>
            ))}
            {note.tags.length > 2 && (
              <span className="text-[8px] font-bold text-stone-400 bg-transparent px-1">
                +{note.tags.length - 2}
              </span>
            )}
          </div>
        )}

      </footer>
    </motion.div>
  );
}
