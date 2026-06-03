/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface ChecklistItem {
  id: string;
  text: string;
  done: boolean;
}

export interface VoiceAttachment {
  id: string;
  blobUrl: string;
  duration: number; // in seconds
  createdAt: number;
}

export interface Note {
  id: string;
  title: string;
  content: string;
  timestamp: number;
  colorKey: string; // key of the PastelTheme list
  isPinned: boolean;
  isArchived: boolean;
  isLocked: boolean; // Secured by user lock PIN
  isChecklistMode: boolean;
  checklist: ChecklistItem[];
  voiceAttachments: VoiceAttachment[];
  tags: string[];
  folder: 'active' | 'archive' | 'trash';
}

export interface PastelTheme {
  key: string;
  name: string;
  bg: string;
  card: string;
  border: string;
  secondary: string;
  text: string;
}

export interface AppSettings {
  userName: string;
  pinLock: string | null; // NULL if disabled, 4-digit string if enabled
  isLockedSession: boolean; // if screen lock is currently active
  viewMode: 'grid' | 'list';
  sortBy: 'updated-desc' | 'updated-asc' | 'title-asc' | 'title-desc';
}
