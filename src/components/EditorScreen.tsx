/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { motion } from 'motion/react';
import {
  ArrowLeft,
  Pin,
  Lock,
  Unlock,
  Archive,
  Trash2,
  Check,
  CheckSquare,
  Square,
  Plus,
  Tag,
  Clock,
  Flame,
  FileText,
  Share2,
  Trash,
  X,
  Type,
  ListTodo
} from 'lucide-react';
import { Note, ChecklistItem, VoiceAttachment } from '../types';
import { PASTEL_THEMES } from '../themes';
import VoiceRecorder from './VoiceRecorder';
import AudioPlayer from './AudioPlayer';

interface EditorScreenProps {
  note: Note | null; // null if creating a new note
  onSave: (noteData: Partial<Note>) => void;
  onBack: () => void;
  onDelete?: () => void;
  onArchiveToggle?: () => void;
  pinLockSetupAvailable: boolean; // True if a PIN is configured in settings
  onPromptPinSetup: () => void; // Triggered if they want to lock but have no PIN
}

export default function EditorScreen({
  note,
  onSave,
  onBack,
  onDelete,
  onArchiveToggle,
  pinLockSetupAvailable,
  onPromptPinSetup
}: EditorScreenProps) {
  const [title, setTitle] = useState(note?.title || '');
  const [content, setContent] = useState(note?.content || '');
  const [colorKey, setColorKey] = useState(note?.colorKey || 'cream');
  const [isPinned, setIsPinned] = useState(note?.isPinned || false);
  const [isLocked, setIsLocked] = useState(note?.isLocked || false);
  const [isChecklistMode, setIsChecklistMode] = useState(note?.isChecklistMode || false);
  
  // Checklist states
  const [checklist, setChecklist] = useState<ChecklistItem[]>(note?.checklist || []);
  const [newCheckItemText, setNewCheckItemText] = useState('');

  // Voice attachments
  const [voiceAttachments, setVoiceAttachments] = useState<VoiceAttachment[]>(note?.voiceAttachments || []);

  // Tag list
  const [tags, setTags] = useState<string[]>(note?.tags || []);
  const [newTagInput, setNewTagInput] = useState('');

  const currentTheme = PASTEL_THEMES[colorKey] || PASTEL_THEMES.cream;

  // Sync edits in state to avoid losing anything
  useEffect(() => {
    // If the note loaded changed, adjust
    if (note) {
      setTitle(note.title);
      setContent(note.content);
      setColorKey(note.colorKey);
      setIsPinned(note.isPinned);
      setIsLocked(note.isLocked);
      setIsChecklistMode(note.isChecklistMode);
      setChecklist(note.checklist || []);
      setVoiceAttachments(note.voiceAttachments || []);
      setTags(note.tags || []);
    }
  }, [note?.id]);

  // Handle auto-saving on close or save trigger
  const handleSaveTrigger = () => {
    // Generate final title if blank to stay clean
    let finalTitle = title.trim();
    if (!finalTitle && !content.trim() && checklist.length === 0 && voiceAttachments.length === 0) {
      // Empty note, do nothing
      onBack();
      return;
    }
    if (!finalTitle) {
      finalTitle = content.trim().split('\n')[0].substring(0, 24) || 'Untitled Note';
    }

    onSave({
      title: finalTitle,
      content,
      colorKey,
      isPinned,
      isLocked,
      isChecklistMode,
      checklist,
      voiceAttachments,
      tags,
      timestamp: Date.now()
    });
    onBack();
  };

  // Checklist Actions
  const handleToggleChecklistMode = () => {
    if (!isChecklistMode && content && checklist.length === 0) {
      // Migrate content lines to checklist items seamlessly
      const items = content
        .split('\n')
        .filter((line) => line.trim().length > 0)
        .map((line) => ({
          id: `item-${Date.now()}-${Math.random()}`,
          text: line.trim(),
          done: false
        }));
      setChecklist(items);
      setContent('');
    } else if (isChecklistMode && checklist.length > 0 && !content) {
      // Seamlessly migrate checklist back to content list text
      const textLines = checklist.map((item) => `${item.done ? '✓ ' : '• '}${item.text}`).join('\n');
      setContent(textLines);
    }
    setIsChecklistMode(!isChecklistMode);
  };

  const handleAddCheckItem = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCheckItemText.trim()) return;
    const newItem: ChecklistItem = {
      id: `item-${Date.now()}-${Math.random()}`,
      text: newCheckItemText.trim(),
      done: false
    };
    setChecklist([...checklist, newItem]);
    setNewCheckItemText('');
  };

  const handleToggleCheckItem = (id: string) => {
    setChecklist(
      checklist.map((item) => (item.id === id ? { ...item, done: !item.done } : item))
    );
  };

  const handleDeleteCheckItem = (id: string) => {
    setChecklist(checklist.filter((item) => item.id !== id));
  };

  // Locking actions
  const handleToggleLock = () => {
    if (!isLocked && !pinLockSetupAvailable) {
      // Needs PIN setup first!
      onPromptPinSetup();
    } else {
      setIsLocked(!isLocked);
    }
  };

  // Add tag
  const handleAddTag = (e: React.FormEvent) => {
    e.preventDefault();
    const tag = newTagInput.trim().toLowerCase();
    if (tag && !tags.includes(tag)) {
      setTags([...tags, tag]);
    }
    setNewTagInput('');
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setTags(tags.filter((t) => t !== tagToRemove));
  };

  // Voice note attachments
  const handleAddVoiceAttachment = (newAudio: VoiceAttachment) => {
    setVoiceAttachments([...voiceAttachments, newAudio]);
  };

  const handleDeleteVoiceAttachment = (id: string) => {
    setVoiceAttachments(voiceAttachments.filter((audio) => audio.id !== id));
  };

  // Stats Counters
  const charCount = isChecklistMode
    ? checklist.reduce((sum, item) => sum + item.text.length, 0)
    : content.length;
  const wordCount = isChecklistMode
    ? checklist.reduce((sum, item) => sum + item.text.split(/\s+/).filter(Boolean).length, 0)
    : content.split(/\s+/).filter(Boolean).length;
  const readTimeMins = Math.ceil(wordCount / 200) || 1;

  const quickSuggestionTags = ['Idea', 'Work', 'Personal', 'Shopping', 'Travel', 'Mind'];

  return (
    <div
      id="pillow-editor-screen"
      className={`absolute inset-0 flex flex-col justify-between transition-colors duration-500 rounded-[40px] overflow-hidden ${currentTheme.bg} ${currentTheme.text}`}
    >
      {/* Dynamic Header Toolbar */}
      <header className="h-16 px-6 flex items-center justify-between border-b border-black/[0.04] shrink-0">
        <button
          onClick={handleSaveTrigger}
          id="editor-back-save-btn"
          className="p-2 -ml-2 rounded-full hover:bg-black/[0.04] transition-colors flex items-center gap-1.5 focus:outline-none"
        >
          <ArrowLeft className="w-5 h-5" />
          <span className="text-xs font-semibold uppercase tracking-wider">Save</span>
        </button>

        {/* Note Title or Actions */}
        <div className="flex items-center gap-1">
          {/* Pin Trigger */}
          <button
            onClick={() => setIsPinned(!isPinned)}
            id="editor-pin-toggle-btn"
            className={`p-2.5 rounded-xl transition-all ${
              isPinned
                ? 'bg-[#4c3e34] text-[#FAF7F2] scale-105 shadow-sm'
                : 'hover:bg-black/[0.04] text-stone-500'
            }`}
            title={isPinned ? 'Unpin from Top' : 'Pin to Top'}
          >
            <Pin className="w-4.5 h-4.5 fill-current" />
          </button>

          {/* Secure Vault Lock Toggle */}
          <button
            onClick={handleToggleLock}
            id="editor-lock-toggle-btn"
            className={`p-2.5 rounded-xl transition-all ${
              isLocked
                ? 'bg-amber-600/90 text-white scale-105 shadow-sm'
                : 'hover:bg-black/[0.04] text-stone-500'
            }`}
            title={isLocked ? 'Unlock private note' : 'Secure vault lock with PIN'}
          >
            {isLocked ? <Lock className="w-4.5 h-4.5" /> : <Unlock className="w-4.5 h-4.5" />}
          </button>

          {/* Archive Action */}
          {onArchiveToggle && (
            <button
              onClick={onArchiveToggle}
              id="editor-archive-toggle-btn"
              className="p-2.5 rounded-xl hover:bg-black/[0.04] text-stone-500 transition-colors"
              title={note?.folder === 'archive' ? 'Unarchive and keep Active' : 'Archive and sleep note'}
            >
              <Archive className="w-4.5 h-4.5" />
            </button>
          )}

          {/* Trash/Delete Action */}
          {onDelete && (
            <button
              onClick={() => {
                if (window.confirm('Move this premium reflection to Trash?')) {
                  onDelete();
                }
              }}
              id="editor-trash-btn"
              className="p-2.5 rounded-xl hover:bg-rose-50 text-stone-400 hover:text-rose-600 transition-colors"
              title="Move to Trash"
            >
              <Trash2 className="w-4.5 h-4.5" />
            </button>
          )}
        </div>
      </header>

      {/* Editor Body Area */}
      <main className="flex-1 overflow-y-auto px-6 py-4 flex flex-col focus-within:outline-none scrollbar-thin">
        {/* Title Input Field */}
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Give a Cozy Name..."
          id="editor-note-title-input"
          className="w-full text-2xl font-bold bg-transparent border-none placeholder-stone-400/70 focus:outline-none focus:ring-0 mb-4 shrink-0"
        />

        {/* Quick Toolbar for content helper: checklist toggle, character statistics */}
        <div className="flex items-center gap-2 mb-4">
          <button
            onClick={handleToggleChecklistMode}
            id="editor-checklist-mode-toggle"
            className="flex items-center gap-1.5 px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider rounded-xl bg-black/[0.04] hover:bg-black/[0.08] transition-colors focus:outline-none text-stone-600"
          >
            {isChecklistMode ? (
              <>
                <Type className="w-3.5 h-3.5 text-[#8C7A65]" />
                <span>Text Mode</span>
              </>
            ) : (
              <>
                <ListTodo className="w-3.5 h-3.5 text-[#8C7A65]" />
                <span>List Mode</span>
              </>
            )}
          </button>

          <span className="text-[10px] text-stone-400 font-medium">|</span>

          <div className="flex items-center gap-1 text-[11px] text-stone-500 uppercase tracking-wider font-semibold">
            <Clock className="w-3.5 h-3.5" />
            <span>{readTimeMins} min read</span>
          </div>
        </div>

        {/* Content Region: Raw text editor OR fully-functional checklist builder */}
        <div className="flex-1 flex flex-col min-h-[140px]">
          {isChecklistMode ? (
            /* Checklist Mode View */
            <div id="editor-checklist-builder-view" className="flex flex-col gap-2">
              {checklist.map((item) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between p-2 bg-white/40 border border-black/[0.03] rounded-xl group transition-all hover:bg-white/60"
                >
                  <div
                    onClick={() => handleToggleCheckItem(item.id)}
                    className="flex items-center gap-3 cursor-pointer flex-1 select-none mr-2"
                  >
                    {item.done ? (
                      <CheckSquare className="w-5 h-5 text-stone-600 shrink-0" />
                    ) : (
                      <Square className="w-5 h-5 text-stone-400 shrink-0" />
                    )}
                    <span className={`text-sm tracking-wide ${item.done ? 'line-through opacity-50' : ''}`}>
                      {item.text}
                    </span>
                  </div>
                  <button
                    onClick={() => handleDeleteCheckItem(item.id)}
                    className="p-1 text-stone-400 hover:text-rose-500 transition-colors opacity-80 group-hover:opacity-100"
                    title="Remove item"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              ))}

              {/* Add checklist input form */}
              <form onSubmit={handleAddCheckItem} className="flex gap-2 mt-2">
                <input
                  type="text"
                  value={newCheckItemText}
                  onChange={(e) => setNewCheckItemText(e.target.value)}
                  placeholder="Add tasks to this pillow list..."
                  className="flex-1 bg-white/50 border border-black/[0.05] rounded-xl px-4 py-2 text-sm focus:outline-none focus:bg-white/80 transition-colors"
                />
                <button
                  type="submit"
                  className="p-2.5 bg-[#4c3e34] text-white hover:bg-[#3d3129] rounded-xl flex items-center justify-center transition-all focus:outline-none"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </form>
            </div>
          ) : (
            /* Standard cozy memo editor text area */
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Begin pouring your intimate thoughts into pillow..."
              id="editor-content-textarea"
              className="w-full flex-1 bg-transparent border-none focus:outline-none focus:ring-0 text-sm leading-relaxed placeholder-stone-400/80 resize-none min-h-[160px]"
            />
          )}
        </div>

        {/* Audio Memos/Voice attachments container */}
        <div id="editor-voice-attachments-grid" className="mt-6">
          {voiceAttachments.length > 0 && (
            <div className="flex flex-col gap-1">
              <span className="text-[10px] font-bold uppercase tracking-wider text-stone-400">Recorded Audio Thoughts</span>
              {voiceAttachments.map((attachment) => (
                <AudioPlayer
                  key={attachment.id}
                  attachment={attachment}
                  onDelete={handleDeleteVoiceAttachment}
                  accentTextColor={currentTheme.secondary}
                />
              ))}
            </div>
          )}

          {/* Integrated quick voice memo recorder */}
          <VoiceRecorder onAddAttachment={handleAddVoiceAttachment} accentTextColor={currentTheme.secondary} />
        </div>

        {/* Tags Section */}
        <div id="editor-tags-module" className="mt-6 pt-4 border-t border-black/[0.04]">
          <div className="flex items-center gap-1.5 text-xs font-semibold text-stone-400 mb-2">
            <Tag className="w-4 h-4" />
            <span className="uppercase tracking-wider">Note labels & metadata</span>
          </div>

          <div className="flex flex-wrap gap-1.5 mb-3">
            {tags.map((tag) => (
              <span
                key={tag}
                className="flex items-center gap-1 bg-black/[0.04] text-stone-700 text-xs pl-2.5 pr-1.5 py-1 rounded-full border border-black/[0.02]"
              >
                <span>#{tag}</span>
                <button
                  onClick={() => handleRemoveTag(tag)}
                  className="p-0.5 hover:bg-black/[0.06] rounded-full text-stone-400 hover:text-stone-700 transition"
                >
                  <X className="w-3 h-3" />
                </button>
              </span>
            ))}
          </div>

          {/* Add tag form */}
          <form onSubmit={handleAddTag} className="flex gap-2">
            <input
              type="text"
              value={newTagInput}
              onChange={(e) => setNewTagInput(e.target.value)}
              placeholder="Tag label (e.g. mindfulness)"
              className="bg-white/40 border border-black/[0.05] rounded-xl px-3 py-1.5 text-xs focus:outline-none focus:bg-white/80 transition-colors"
            />
            <button
              type="submit"
              className="px-3 bg-black/[0.05] hover:bg-black/[0.08] rounded-xl text-xs font-medium transition"
            >
              Add label
            </button>
          </form>

          {/* Quick choices list */}
          <div className="flex flex-wrap gap-1 mt-3.5">
            {quickSuggestionTags
              .filter((st) => !tags.includes(st.toLowerCase()))
              .map((st) => (
                <button
                  key={st}
                  type="button"
                  onClick={() => setTags([...tags, st.toLowerCase()])}
                  className="text-[10px] bg-black/[0.02] hover:bg-black/[0.05] text-stone-500 border border-black/[0.02] px-2 py-0.5 rounded-lg font-medium transition"
                >
                  + {st}
                </button>
              ))}
          </div>
        </div>
      </main>

      {/* Cozy Theme Palette Selection Drawer (Drawer Pill Bar at bottom) */}
      <footer className="px-6 py-4 bg-white/30 backdrop-blur-md border-t border-black/[0.03] flex items-center justify-between select-none shrink-0">
        <span className="text-[11px] font-bold uppercase tracking-wider text-stone-500/70">
          Wallpaper Theme
        </span>

        <div className="flex items-center gap-2 overflow-x-auto py-1 scrollbar-none max-w-[280px]">
          {Object.values(PASTEL_THEMES).map((theme) => {
            const isSelected = colorKey === theme.key;
            return (
              <button
                key={theme.key}
                onClick={() => setColorKey(theme.key)}
                id={`theme-pill-${theme.key}`}
                className={`w-6 h-6 rounded-full border shrink-0 transition-transform ${theme.bg} ${
                  isSelected
                    ? 'scale-125 border-[#4c3e34] ring-2 ring-[#4c3e34]/15 shadow-sm'
                    : 'border-black/[0.1] hover:scale-110'
                }`}
                title={theme.name}
              />
            );
          })}
        </div>
      </footer>
    </div>
  );
}
