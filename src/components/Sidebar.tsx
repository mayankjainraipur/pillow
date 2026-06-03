/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useRef, useState } from 'react';
import {
  FolderOpen,
  FolderDot,
  FileText,
  Lock,
  Unlock,
  Archive,
  Trash2,
  Tag,
  Key,
  ShieldCheck,
  ShieldAlert,
  Sliders,
  Settings,
  Download,
  Upload,
  User,
  Activity,
  LogOut,
  X,
  Plus
} from 'lucide-react';
import { AppSettings, Note } from '../types';

interface SidebarProps {
  currentFolder: 'active' | 'archive' | 'trash' | 'locked';
  setCurrentFolder: (folder: 'active' | 'archive' | 'trash' | 'locked') => void;
  activeTagFilter: string | null;
  setActiveTagFilter: (tag: string | null) => void;
  allTags: string[];
  settings: AppSettings;
  onOpenPinSetup: () => void;
  onDisablePin: () => void;
  onLockSession: () => void;
  onExportBackup: () => void;
  onImportBackup: (importedNotes: Note[], userSettings?: Partial<AppSettings>) => void;
  onClose?: () => void;
  allNotesCount: number;
  lockedNotesCount: number;
  archiveNotesCount: number;
  trashNotesCount: number;
}

export default function Sidebar({
  currentFolder,
  setCurrentFolder,
  activeTagFilter,
  setActiveTagFilter,
  allTags,
  settings,
  onOpenPinSetup,
  onDisablePin,
  onLockSession,
  onExportBackup,
  onImportBackup,
  onClose,
  allNotesCount,
  lockedNotesCount,
  archiveNotesCount,
  trashNotesCount
}: SidebarProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [importStatus, setImportStatus] = useState<'idle' | 'success' | 'error'>('idle');

  const handleImportFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const text = event.target?.result as string;
        const parsed = JSON.parse(text);

        if (Array.isArray(parsed)) {
          // Backward compatibility or legacy format
          onImportBackup(parsed);
          setImportStatus('success');
        } else if (parsed && Array.isArray(parsed.notes)) {
          // Standard full backup format
          onImportBackup(parsed.notes, parsed.settings);
          setImportStatus('success');
        } else {
          setImportStatus('error');
        }
      } catch (err) {
        console.error('Import backup file error:', err);
        setImportStatus('error');
      }

      // Reset file input
      if (fileInputRef.current) fileInputRef.current.value = '';
      setTimeout(() => setImportStatus('idle'), 4000);
    };
    reader.readAsText(file);
  };

  return (
    <div id="pillow-sidebar" className="h-full flex flex-col justify-between bg-[#1e1e20] text-[#ECECEF] w-full p-6 font-sans">
      {/* Scrollable Main Segment */}
      <div className="flex-1 overflow-y-auto scrollbar-none flex flex-col gap-6">
        
        {/* Pillow Branding */}
        <div className="flex items-center justify-between pb-4 border-b border-zinc-800/80 shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 bg-gradient-to-tr from-amber-400 to-orange-400 rounded-2xl flex items-center justify-center font-bold text-black text-lg select-none shadow-[0_4px_12px_rgba(251,191,36,0.2)]">
              P
            </div>
            <div>
              <h2 className="text-base font-bold tracking-tight text-white flex items-center gap-1.5">
                Pillow Notes
              </h2>
              <p className="text-[10px] text-zinc-500 font-medium uppercase tracking-wider">Premium Edition</p>
            </div>
          </div>
          {onClose && (
            <button
              onClick={onClose}
              id="sidebar-close-x-btn"
              className="p-1 px-1.5 bg-zinc-800/50 hover:bg-zinc-850 rounded-lg text-zinc-400 hover:text-white transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* User profile banner */}
        <div className="px-4 py-3 bg-zinc-900/50 hover:bg-zinc-900 border border-zinc-800/40 rounded-2xl flex items-center gap-3 transition">
          <div className="w-9 h-9 bg-zinc-800 rounded-full flex items-center justify-center text-zinc-400 font-mono text-sm uppercase">
            {settings.userName.substring(0, 2) || 'PI'}
          </div>
          <div className="overflow-hidden">
            <p className="text-xs font-semibold text-zinc-300 truncate">Workspace Safe</p>
            <p className="text-[10px] text-zinc-500 truncate">{settings.userName}</p>
          </div>
        </div>

        {/* Folders List */}
        <div>
          <span className="text-[10px] font-bold tracking-wider text-zinc-500 uppercase block mb-2 px-1">
            Spaces & Folders
          </span>

          <nav className="flex flex-col gap-1">
            {/* Active Shelf */}
            <button
              onClick={() => {
                setCurrentFolder('active');
                setActiveTagFilter(null);
                if (onClose) onClose();
              }}
              id="shelf-active-btn"
              className={`flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                currentFolder === 'active' && !activeTagFilter
                  ? 'bg-[#4c3e34] text-white shadow-md'
                  : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900/50'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <FileText className="w-4 h-4" />
                <span>Pillow Active notes</span>
              </div>
              <span className="bg-zinc-800 font-mono text-[9px] text-zinc-400 px-1.5 py-0.5 rounded-md">
                {allNotesCount}
              </span>
            </button>

            {/* Secure Vault Shelf */}
            <button
              onClick={() => {
                setCurrentFolder('locked');
                setActiveTagFilter(null);
                if (onClose) onClose();
              }}
              id="shelf-locked-btn"
              className={`flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                currentFolder === 'locked' && !activeTagFilter
                  ? 'bg-amber-600 text-white shadow-md'
                  : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900/50'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <Lock className="w-4 h-4" />
                <span>Confidential Vault</span>
              </div>
              <span className="bg-zinc-800 font-mono text-[9px] text-zinc-400 px-1.5 py-0.5 rounded-md">
                {lockedNotesCount}
              </span>
            </button>

            {/* Archived Shelf */}
            <button
              onClick={() => {
                setCurrentFolder('archive');
                setActiveTagFilter(null);
                if (onClose) onClose();
              }}
              id="shelf-archive-btn"
              className={`flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                currentFolder === 'archive' && !activeTagFilter
                  ? 'bg-[#3d424b] text-white shadow-md'
                  : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900/50'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <Archive className="w-4 h-4" />
                <span>Sleeping Archive</span>
              </div>
              <span className="bg-zinc-800 font-mono text-[9px] text-zinc-400 px-1.5 py-0.5 rounded-md">
                {archiveNotesCount}
              </span>
            </button>

            {/* Trash Shelf */}
            <button
              onClick={() => {
                setCurrentFolder('trash');
                setActiveTagFilter(null);
                if (onClose) onClose();
              }}
              id="shelf-trash-btn"
              className={`flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                currentFolder === 'trash' && !activeTagFilter
                  ? 'bg-rose-950/40 text-rose-300 border border-rose-900/20 shadow-md'
                  : 'text-zinc-400 hover:text-rose-400 hover:bg-zinc-900/50'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <Trash2 className="w-4 h-4" />
                <span>Trash Bin</span>
              </div>
              <span className="bg-rose-950/60 font-mono text-[9px] text-rose-400/80 px-1.5 py-0.5 rounded-md border border-rose-900/30">
                {trashNotesCount}
              </span>
            </button>
          </nav>
        </div>

        {/* Dynamic Tag Filters */}
        {allTags.length > 0 && (
          <div>
            <span className="text-[10px] font-bold tracking-wider text-zinc-500 uppercase block mb-2 px-1">
              Filter by labels
            </span>
            <div className="flex flex-wrap gap-1.5 max-h-40 overflow-y-auto pr-1">
              {allTags.map((tag) => {
                const isSelected = activeTagFilter === tag;
                return (
                  <button
                    key={tag}
                    onClick={() => {
                      setActiveTagFilter(isSelected ? null : tag);
                      if (onClose) onClose();
                    }}
                    id={`sidebar-tag-pill-${tag}`}
                    className={`flex items-center gap-1 px-2.5 py-1 text-xs rounded-lg transition-all ${
                      isSelected
                        ? 'bg-amber-400 text-black font-semibold'
                        : 'bg-zinc-900 text-zinc-400 hover:bg-zinc-850 hover:text-zinc-200'
                    }`}
                  >
                    <Tag className="w-3 h-3 shrink-0 opacity-70" />
                    <span>#{tag}</span>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* Security Settings Area */}
        <div className="pt-4 border-t border-zinc-800/80">
          <span className="text-[10px] font-bold tracking-wider text-zinc-500 uppercase block mb-2 px-1">
            Note Encryption & Security
          </span>

          <div className="flex flex-col gap-1.5">
            {settings.pinLock ? (
              <>
                <div className="px-3 py-1.5 rounded-xl bg-emerald-950/20 text-emerald-400 text-[11px] font-semibold border border-emerald-900/30 flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4" />
                  <span>PIN Security Active</span>
                </div>

                <button
                  onClick={onLockSession}
                  id="lock-session-now-btn"
                  className="flex items-center gap-2.5 w-full text-left px-3 py-2 text-xs text-zinc-400 hover:text-white hover:bg-zinc-900 rounded-lg transition font-medium"
                >
                  <Lock className="w-3.5 h-3.5" />
                  <span>Lock Vault Session Now</span>
                </button>

                <button
                  onClick={onDisablePin}
                  id="disable-pin-lock-btn"
                  className="flex items-center gap-2.5 w-full text-left px-3 py-2 text-xs text-rose-400 hover:text-rose-300 hover:bg-zinc-900 rounded-lg transition font-medium"
                >
                  <ShieldAlert className="w-3.5 h-3.5" />
                  <span>Disable PIN Security</span>
                </button>
              </>
            ) : (
              <>
                <div className="px-3 py-1.5 rounded-xl bg-amber-950/20 text-amber-500 text-[11px] font-medium border border-amber-900/30 flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4 text-amber-500" />
                  <span>No Lock PIN configured</span>
                </div>

                <button
                  onClick={onOpenPinSetup}
                  id="create-lock-pin-btn"
                  className="flex items-center gap-2.5 w-full text-left px-3 py-2 text-xs text-amber-400 hover:text-amber-300 hover:bg-zinc-900 rounded-lg transition font-semibold"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Setup Security PIN Lock</span>
                </button>
              </>
            )}
          </div>
        </div>

        {/* Local Backup Manager */}
        <div className="pt-4 border-t border-zinc-800/80">
          <span className="text-[10px] font-bold tracking-wider text-zinc-500 uppercase block mb-2 px-1">
            Export & Backup Utilities
          </span>

          <div className="flex flex-col gap-2">
            <button
              onClick={onExportBackup}
              id="export-backup-disk-btn"
              className="flex items-center gap-2 px-3 py-2 w-full text-left bg-zinc-900 hover:bg-zinc-850 hover:text-white rounded-xl text-xs text-zinc-300 transition"
              title="Download backup file"
            >
              <Download className="w-4 h-4 text-amber-400" />
              <span>Download Local Backup</span>
            </button>

            <button
              onClick={() => fileInputRef.current?.click()}
              id="trigger-import-file-btn"
              className="flex items-center gap-2 px-3 py-2 w-full text-left bg-zinc-900 hover:bg-zinc-850 hover:text-white rounded-xl text-xs text-zinc-300 transition"
              title="Select backup file"
            >
              <Upload className="w-4 h-4 text-emerald-400" />
              <span>Upload Pillow Backup</span>
            </button>

            <input
              ref={fileInputRef}
              type="file"
              accept=".json"
              onChange={handleImportFileChange}
              className="hidden"
            />

            {importStatus === 'success' && (
              <div className="p-2 text-center text-[10px] bg-emerald-950/40 border border-emerald-900/20 text-emerald-400 rounded-lg animate-pulse">
                Backup restored successfully!
              </div>
            )}
            {importStatus === 'error' && (
              <div className="p-2 text-center text-[10px] bg-rose-950/40 border border-rose-900/20 text-rose-450 rounded-lg">
                Unsupported/Invalid backup code!
              </div>
            )}
          </div>
        </div>

      </div>

      {/* Comfort footer signature */}
      <footer className="pt-4 mt-4 border-t border-zinc-800/80 shrink-0 text-center">
        <p className="text-[10px] text-zinc-600 font-mono tracking-wider">
          PILLOW • 100% SECURE & OFFLINE
        </p>
      </footer>

    </div>
  );
}
