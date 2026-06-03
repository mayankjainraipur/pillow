/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import {
  Menu,
  Search,
  Plus,
  Compass,
  Lock,
  Archive,
  Trash2,
  Settings,
  Grid,
  List,
  Sparkles,
  RefreshCw,
  FolderLock,
  ChevronDown,
  X,
  FileText
} from 'lucide-react';

import { Note, AppSettings } from './types';
import { PASTEL_THEMES, INITIAL_NOTES } from './themes';
import DeviceFrame from './components/DeviceFrame';
import LockScreen from './components/LockScreen';
import Sidebar from './components/Sidebar';
import NoteCard from './components/NoteCard';
import EditorScreen from './components/EditorScreen';

const STORAGE_NOTES_KEY = 'pillow_notes_db_v1';
const STORAGE_SETTINGS_KEY = 'pillow_settings_db_v1';

export default function App() {
  // --- Rehydration & Initialization ---
  const [notes, setNotes] = useState<Note[]>(() => {
    try {
      const stored = localStorage.getItem(STORAGE_NOTES_KEY);
      if (stored) {
        return JSON.parse(stored);
      }
    } catch (e) {
      console.error('Failed to restore notes from storage:', e);
    }
    return INITIAL_NOTES;
  });

  const [settings, setSettings] = useState<AppSettings>(() => {
    try {
      const stored = localStorage.getItem(STORAGE_SETTINGS_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        return {
          ...parsed,
          // Re-lock the session on initial app load for security
          isLockedSession: parsed.pinLock ? true : false
        };
      }
    } catch (e) {
      console.error('Failed to restore settings from storage:', e);
    }
    return {
      userName: 'Sleepy Dreamer',
      pinLock: null,
      isLockedSession: false,
      viewMode: 'grid',
      sortBy: 'updated-desc'
    };
  });

  // --- Sync State to Local Storage ---
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_NOTES_KEY, JSON.stringify(notes));
    } catch (e) {
      console.error('Failed to sync notes state to storage:', e);
    }
  }, [notes]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_SETTINGS_KEY, JSON.stringify(settings));
    } catch (e) {
      console.error('Failed to sync settings state to storage:', e);
    }
  }, [settings]);

  // --- Screen State Machines ---
  const [currentFolder, setCurrentFolder] = useState<'active' | 'archive' | 'trash' | 'locked'>('active');
  const [activeTagFilter, setActiveTagFilter] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  
  const [selectedNote, setSelectedNote] = useState<Note | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isFullWidth, setIsFullWidth] = useState(false);
  const [showSidebar, setShowSidebar] = useState(false);

  // Security flow state triggers
  const [securityOverlay, setSecurityOverlay] = useState<{
    isActive: boolean;
    actionType: 'unlock' | 'setup' | 'disable';
    targetNoteId?: string; // used if unlocking a specific note
  } | null>(null);

  // Initialize session lock state on boot if a PIN exists
  useEffect(() => {
    if (settings.pinLock) {
      setSecurityOverlay({
        isActive: true,
        actionType: 'unlock'
      });
    }
  }, []);

  // --- Reset Application helper ---
  const handleResetApplication = () => {
    localStorage.removeItem(STORAGE_NOTES_KEY);
    localStorage.removeItem(STORAGE_SETTINGS_KEY);
    setNotes(INITIAL_NOTES);
    setSettings({
      userName: 'Sleepy Dreamer',
      pinLock: null,
      isLockedSession: false,
      viewMode: 'grid',
      sortBy: 'updated-desc'
    });
    setCurrentFolder('active');
    setActiveTagFilter(null);
    setSearchQuery('');
    setSelectedNote(null);
    setIsEditing(false);
    setSecurityOverlay(null);
  };

  // --- Computed metadata arrays ---
  const allTags = useMemo(() => {
    const tagsSet = new Set<string>();
    notes.forEach((note) => {
      if (note.folder !== 'trash') {
        note.tags.forEach((tag) => tagsSet.add(tag.toLowerCase()));
      }
    });
    return Array.from(tagsSet);
  }, [notes]);

  // Filtered and Sorted list selector
  const processedNotes = useMemo(() => {
    let list = [...notes];

    // Filter by Folder Shelf
    if (currentFolder === 'active') {
      list = list.filter((n) => n.folder === 'active');
    } else if (currentFolder === 'archive') {
      list = list.filter((n) => n.folder === 'archive');
    } else if (currentFolder === 'trash') {
      list = list.filter((n) => n.folder === 'trash');
    } else if (currentFolder === 'locked') {
      list = list.filter((n) => n.isLocked && n.folder !== 'trash');
    }

    // Filter by Tag label
    if (activeTagFilter) {
      list = list.filter((n) => n.tags.map((t) => t.toLowerCase()).includes(activeTagFilter.toLowerCase()));
    }

    // Filter by text search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      list = list.filter(
        (n) => n.title.toLowerCase().includes(q) || n.content.toLowerCase().includes(q)
      );
    }

    // Sort Order Options (Updated Dates / Alphabetical title)
    list.sort((a, b) => {
      if (settings.sortBy === 'updated-desc') return b.timestamp - a.timestamp;
      if (settings.sortBy === 'updated-asc') return a.timestamp - b.timestamp;
      if (settings.sortBy === 'title-asc') return a.title.localeCompare(b.title);
      if (settings.sortBy === 'title-desc') return b.title.localeCompare(a.title);
      return b.timestamp - a.timestamp;
    });

    return list;
  }, [notes, currentFolder, activeTagFilter, searchQuery, settings.sortBy]);

  // Split into Pinned vs Regular lists for comfortable view layout
  const pinnedCollection = useMemo(() => {
    return processedNotes.filter((n) => n.isPinned);
  }, [processedNotes]);

  const regularCollection = useMemo(() => {
    return processedNotes.filter((n) => !n.isPinned);
  }, [processedNotes]);

  // --- Folder Counts ---
  const activeCount = notes.filter((n) => n.folder === 'active').length;
  const lockedCount = notes.filter((n) => n.isLocked && n.folder !== 'trash').length;
  const archiveCount = notes.filter((n) => n.folder === 'archive').length;
  const trashCount = notes.filter((n) => n.folder === 'trash').length;

  // --- Core CRUD Handlers ---
  const handleSaveNote = (updatedProperties: Partial<Note>) => {
    if (selectedNote) {
      // Editing an existing note
      setNotes(
        notes.map((n) =>
          n.id === selectedNote.id ? { ...n, ...updatedProperties, timestamp: Date.now() } as Note : n
        )
      );
    } else {
      // Creating a new note
      const newNote: Note = {
        id: `note-${Date.now()}`,
        title: updatedProperties.title || 'Cozy Reflection',
        content: updatedProperties.content || '',
        timestamp: Date.now(),
        colorKey: updatedProperties.colorKey || 'cream',
        isPinned: updatedProperties.isPinned || false,
        isArchived: false,
        isLocked: updatedProperties.isLocked || false,
        isChecklistMode: updatedProperties.isChecklistMode || false,
        checklist: updatedProperties.checklist || [],
        voiceAttachments: updatedProperties.voiceAttachments || [],
        tags: updatedProperties.tags || [],
        folder: 'active'
      };
      setNotes([newNote, ...notes]);
    }
  };

  const handleDeleteTrigger = (currentNoteId: string) => {
    const target = notes.find((n) => n.id === currentNoteId);
    if (!target) return;

    if (target.folder === 'trash') {
      // Permanent hard delete
      setNotes(notes.filter((n) => n.id !== currentNoteId));
    } else {
      // Move to Trash bin (retains values but changes folder status)
      setNotes(
        notes.map((n) =>
          n.id === currentNoteId ? { ...n, folder: 'trash', isPinned: false } as Note : n
        )
      );
    }
    setIsEditing(false);
    setSelectedNote(null);
  };

  const handleArchiveToggleTrigger = (currentNoteId: string) => {
    setNotes(
      notes.map((n) => {
        if (n.id === currentNoteId) {
          const isCurrentlyArchived = n.folder === 'archive';
          return {
            ...n,
            folder: isCurrentlyArchived ? 'active' : 'archive',
            isPinned: false
          } as Note;
        }
        return n;
      })
    );
    setIsEditing(false);
    setSelectedNote(null);
  };

  const handleTogglePinOnCard = (targetNoteId: string) => {
    setNotes(
      notes.map((n) => (n.id === targetNoteId ? { ...n, isPinned: !n.isPinned } as Note : n))
    );
  };

  const handleOpenNote = (note: Note) => {
    if (note.isLocked && settings.pinLock) {
      // Prompt lock challenge PIN code for specific note vault entry
      setSecurityOverlay({
        isActive: true,
        actionType: 'unlock',
        targetNoteId: note.id
      });
    } else {
      setSelectedNote(note);
      setIsEditing(true);
    }
  };

  const handleCreateNewNoteTrigger = () => {
    setSelectedNote(null);
    setIsEditing(true);
  };

  // --- Security PIN Screen Callbacks ---
  const handleSecuritySuccess = (pin: string) => {
    if (securityOverlay?.actionType === 'unlock') {
      if (securityOverlay.targetNoteId) {
        // Unlocked a specific confidential note
        const target = notes.find((n) => n.id === securityOverlay.targetNoteId);
        if (target) {
          setSelectedNote(target);
          setIsEditing(true);
        }
      } else {
        // App-wide boot unlocked session
        setSettings({ ...settings, isLockedSession: false });
      }
    } else if (securityOverlay?.actionType === 'setup') {
      // Successfully registered a new PIN code
      setSettings({ ...settings, pinLock: pin, isLockedSession: false });
    } else if (securityOverlay?.actionType === 'disable') {
      // PIN configuration removed safely
      setSettings({ ...settings, pinLock: null, isLockedSession: false });
      // Remove locked tags from notes so they do not hang locked
      setNotes(notes.map((n) => ({ ...n, isLocked: false })));
    }
    setSecurityOverlay(null);
  };

  // --- Backup Managers ---
  const handleExportBackupFile = () => {
    try {
      const payload = {
        app: 'Pillow Notes',
        timestamp: Date.now(),
        settings: {
          userName: settings.userName,
          pinLock: settings.pinLock,
          sortBy: settings.sortBy,
          viewMode: settings.viewMode
        },
        notes: notes
      };
      const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `pillow_backup_${new Date().toISOString().slice(0, 10)}.json`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      alert('Could not compile local backup file.');
    }
  };

  const handleImportBackupFile = (importedNotes: Note[], restoredSettings?: Partial<AppSettings>) => {
    if (importedNotes && Array.isArray(importedNotes)) {
      setNotes(importedNotes);
    }
    if (restoredSettings) {
      setSettings((prev) => ({
        ...prev,
        ...restoredSettings,
        // Enforce restoring safety locks
        isLockedSession: restoredSettings.pinLock ? true : false
      }));
      if (restoredSettings.pinLock) {
        setSecurityOverlay({
          isActive: true,
          actionType: 'unlock'
        });
      }
    }
  };

  // --- Computed Layout Label ---
  const folderTitle = {
    active: 'Active Thoughts',
    archive: 'Deep Restful Archive',
    trash: 'Discarded Trash Bin',
    locked: 'Confidential Secure Vault'
  }[currentFolder];

  return (
    <DeviceFrame
      isFullWidth={isFullWidth}
      setIsFullWidth={setIsFullWidth}
      onResetApp={handleResetApplication}
    >
      <div id="pillow-container" className="h-full relative overflow-hidden bg-[#FAF7F2] text-[#4A3E31] flex">
        
        {/* --- Side Navigation Panel (Persistent left on wide screen, absolute slide on phone screen) --- */}
        <AnimatePresence>
          {showSidebar && (
            <motion.div
              initial={{ x: '-100%' }}
              animate={{ x: 0 }}
              exit={{ x: '-100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 220 }}
              className="absolute inset-y-0 left-0 w-[290px] z-30 shadow-2xl mr-4 shrink-0 h-full"
            >
              <Sidebar
                currentFolder={currentFolder}
                setCurrentFolder={setCurrentFolder}
                activeTagFilter={activeTagFilter}
                setActiveTagFilter={setActiveTagFilter}
                allTags={allTags}
                settings={settings}
                allNotesCount={activeCount}
                lockedNotesCount={lockedCount}
                archiveNotesCount={archiveCount}
                trashNotesCount={trashCount}
                onOpenPinSetup={() => setSecurityOverlay({ isActive: true, actionType: 'setup' })}
                onDisablePin={() => setSecurityOverlay({ isActive: true, actionType: 'disable' })}
                onLockSession={() => setSettings({ ...settings, isLockedSession: true })}
                onExportBackup={handleExportBackupFile}
                onImportBackup={handleImportBackupFile}
                onClose={() => setShowSidebar(false)}
              />
            </motion.div>
          )}
        </AnimatePresence>

        {/* Sidebar overlay backdrop for phone sizes */}
        {showSidebar && (
          <div
            onClick={() => setShowSidebar(false)}
            id="sidebar-overlay-backdrop"
            className="absolute inset-0 bg-black/40 backdrop-blur-sm z-20 cursor-pointer"
          />
        )}

        {/* --- Primary Workspace View Area --- */}
        <div className="flex-1 flex flex-col h-full overflow-hidden relative">
          
          {/* Main List Top Header Appbar */}
          <header className="h-16 px-6 border-b border-black/[0.04] flex items-center justify-between shrink-0 bg-white/45 backdrop-blur-md">
            <div className="flex items-center gap-3">
              <button
                onClick={() => setShowSidebar(!showSidebar)}
                id="appbar-hamburger-menu"
                className="p-2 -ml-2 rounded-xl hover:bg-black/5 transition-colors focus:outline-none"
                title="Open Sidebar"
              >
                <Menu className="w-5 h-5" />
              </button>

              <div className="flex items-center gap-1.5 font-sans">
                <span className="text-[#8C7A65] text-[18px] font-bold tracking-tight">Pillow</span>
                <span className="text-[10px] uppercase font-bold text-stone-400 border border-stone-200 bg-white px-2 py-0.5 rounded-full select-none">Notes</span>
              </div>
            </div>

            {/* View Grid Layout Toggles */}
            <div className="flex items-center gap-1">
              <button
                onClick={() =>
                  setSettings({ ...settings, sortBy: settings.sortBy === 'updated-desc' ? 'title-asc' : 'updated-desc' })
                }
                id="sorting-toggle-btn"
                className="p-2 text-stone-500 hover:text-stone-800 rounded-xl hover:bg-black/5 transition text-xs font-semibold flex items-center gap-1 border border-stone-200/40"
              >
                <span>{settings.sortBy === 'updated-desc' ? 'By Date 🕒' : 'Alpha 🔤'}</span>
              </button>

              <button
                onClick={() => setSettings({ ...settings, viewMode: settings.viewMode === 'grid' ? 'list' : 'grid' })}
                id="viewmode-toggle-btn"
                className="p-2 text-stone-500 hover:text-stone-800 rounded-xl hover:bg-black/5 transition"
                title={settings.viewMode === 'grid' ? 'Switch to List view' : 'Switch to Grid view'}
              >
                {settings.viewMode === 'grid' ? <List className="w-4.5 h-4.5" /> : <Grid className="w-4.5 h-4.5" />}
              </button>
            </div>
          </header>

          {/* Quick Realtime Floating Search Field */}
          <div className="px-6 pt-4 pb-2 shrink-0 flex flex-col gap-2 bg-[#FAF7F2]">
            <div className="relative w-full">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-stone-400" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search cozy notes, passwords or checklists..."
                id="cozy-app-filter-search-input"
                className="w-full bg-white border border-stone-250 rounded-2xl pl-10 pr-9 py-2.5 text-xs focus:outline-none focus:ring-1 focus:ring-amber-500/20 placeholder-stone-400/80 shade-sm"
              />
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery('')}
                  id="search-clear-x-btn"
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-stone-400 hover:text-stone-700"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
            </div>

            {/* active filters bar */}
            {(activeTagFilter || searchQuery) && (
              <div className="flex items-center justify-between py-1 px-1 bg-amber-50 rounded-xl border border-amber-100/60 max-w-full">
                <div className="flex items-center gap-1.5 overflow-hidden text-[10px] uppercase font-bold text-amber-800 ml-1.5">
                  <Sparkles className="w-3.5 h-3.5 shrink-0 animate-spin" />
                  <span className="truncate">
                    Filters: {activeTagFilter ? `#${activeTagFilter}` : ''} {searchQuery ? `"${searchQuery}"` : ''}
                  </span>
                </div>
                <button
                  onClick={() => {
                    setActiveTagFilter(null);
                    setSearchQuery('');
                  }}
                  id="reset-cozy-filters-bubble"
                  className="p-1 px-2 text-[9px] font-extrabold uppercase bg-amber-200/60 hover:bg-amber-300 text-amber-900 rounded-lg shrink-0 transition"
                >
                  Reset
                </button>
              </div>
            )}
          </div>

          {/* Main scroll list containing headers and grid cards */}
          <main className="flex-1 overflow-y-auto px-6 pb-24 scrollbar-thin">
            
            {/* Context Heading Space banner info */}
            <div className="my-3 flex items-center justify-between">
              <h1 className="text-lg font-bold tracking-tight text-[#4c3e34]">
                {folderTitle}
              </h1>
              <span className="text-[10px] font-bold text-stone-400 bg-stone-100 px-2.5 py-0.5 rounded-full border border-stone-200">
                {processedNotes.length} notes
              </span>
            </div>

            {/* Handle State Empty */}
            {processedNotes.length === 0 && (
              <div id="notes-empty-state-card" className="my-16 py-12 px-6 bg-white/40 border border-dashed border-stone-300 rounded-[32px] text-center flex flex-col items-center justify-center max-w-sm mx-auto">
                <div className="w-16 h-16 rounded-full bg-stone-100 flex items-center justify-center mb-4">
                  <Compass className="w-8 h-8 text-stone-450 animate-bounce" />
                </div>
                <h3 className="font-bold text-sm text-stone-700 mb-1">Your pillow is cozy & empty</h3>
                <p className="text-xs text-stone-500 max-w-[220px]">
                  No active memories are sleeping in this drawer. Tap the comfortable draft button down below to start.
                </p>
              </div>
            )}

            {/* --- LIST LAYOUT COZY MASONRY (Pinned Block + Regular Block) --- */}
            {processedNotes.length > 0 && (
              <div className="flex flex-col gap-6">
                
                {/* 1. Pinned Notes Block (Separated structure for high quality hierarchy) */}
                {pinnedCollection.length > 0 && (
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-wider text-stone-400 block mb-2 px-1">
                      Pinned Memories
                    </span>
                    <div
                      className={`grid gap-4 ${
                        settings.viewMode === 'grid' ? 'grid-cols-2' : 'grid-cols-1'
                      }`}
                    >
                      {pinnedCollection.map((note) => (
                        <NoteCard
                          key={note.id}
                          note={note}
                          onClick={() => handleOpenNote(note)}
                          onTogglePin={() => handleTogglePinOnCard(note.id)}
                        />
                      ))}
                    </div>
                  </div>
                )}

                {/* 2. Regular Notes Group */}
                {regularCollection.length > 0 && (
                  <div>
                    {pinnedCollection.length > 0 && (
                      <span className="text-[10px] font-bold uppercase tracking-wider text-stone-400 block mb-2 px-1">
                        Recent Thoughts
                      </span>
                    )}
                    <div
                      className={`grid gap-4 ${
                        settings.viewMode === 'grid' ? 'grid-cols-2' : 'grid-cols-1'
                      }`}
                    >
                      {regularCollection.map((note) => (
                        <NoteCard
                          key={note.id}
                          note={note}
                          onClick={() => handleOpenNote(note)}
                          onTogglePin={() => handleTogglePinOnCard(note.id)}
                        />
                      ))}
                    </div>
                  </div>
                )}

              </div>
            )}

          </main>

          {/* Elegant float CTA button trigger */}
          <div className="absolute bottom-6 right-6 z-10 shrink-0 select-none">
            <motion.button
              whileHover={{ scale: 1.08 }}
              whileTap={{ scale: 0.95 }}
              onClick={handleCreateNewNoteTrigger}
              id="floating-create-new-note-btn"
              className="w-14 h-14 bg-[#4c3e34] text-[#FAF7F2] hover:bg-[#3d3129] rounded-[24px] shadow-lg flex items-center justify-center transition-all focus:outline-none"
              title="Create a New Cozy Reflection"
            >
              <Plus className="w-7 h-7" />
            </motion.button>
          </div>

        </div>

        {/* --- Editor Screen Component Modal Frame Overlay --- */}
        <AnimatePresence>
          {isEditing && (
            <motion.div
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              transition={{ type: 'spring', damping: 28, stiffness: 220 }}
              className="absolute inset-0 z-40 bg-[#FAF7F2] rounded-[40px] overflow-hidden shadow-2xl h-full"
            >
              <EditorScreen
                note={selectedNote}
                pinLockSetupAvailable={settings.pinLock ? true : false}
                onPromptPinSetup={() => setSecurityOverlay({ isActive: true, actionType: 'setup' })}
                onSave={handleSaveNote}
                onBack={() => setIsEditing(false)}
                onDelete={selectedNote ? () => handleDeleteTrigger(selectedNote.id) : undefined}
                onArchiveToggle={
                  selectedNote ? () => handleArchiveToggleTrigger(selectedNote.id) : undefined
                }
              />
            </motion.div>
          )}
        </AnimatePresence>

        {/* --- Security PIN Lock Overlay Panel --- */}
        <AnimatePresence>
          {securityOverlay?.isActive && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="absolute inset-0 z-50 rounded-[40px] overflow-hidden"
            >
              <LockScreen
                storedPin={settings.pinLock}
                actionType={securityOverlay.actionType}
                isCancelable={securityOverlay.actionType !== 'unlock' || securityOverlay.targetNoteId ? true : false}
                onSuccess={handleSecuritySuccess}
                onCancel={() => setSecurityOverlay(null)}
              />
            </motion.div>
          )}
        </AnimatePresence>

        {/* System initial boot session locked blocker view */}
        <AnimatePresence>
          {settings.isLockedSession && settings.pinLock && !securityOverlay?.isActive && (
            <div
              onClick={() => {
                // Ensure they can unlock
                setSecurityOverlay({
                  isActive: true,
                  actionType: 'unlock'
                });
              }}
              id="boot-app-restricted-lockscreen"
              className="absolute inset-0 z-45 bg-[#161618] flex flex-col items-center justify-center text-center p-6 rounded-[40px] cursor-pointer"
            >
              <div className="w-16 h-16 bg-[#2B2B30] rounded-full flex items-center justify-center mb-4 border border-zinc-700/50">
                <Lock className="w-7 h-7 text-amber-300 animate-pulse" />
              </div>
              <h2 className="text-xl font-bold text-white mb-1.5">Pillow Safe Secure</h2>
              <p className="text-xs text-zinc-400 max-w-[200px]">Session is locked. Tap anywhere or click compile to unlock with PIN.</p>
            </div>
          )}
        </AnimatePresence>

      </div>
    </DeviceFrame>
  );
}
