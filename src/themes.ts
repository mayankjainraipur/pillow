/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { PastelTheme, Note } from './types';

export const PASTEL_THEMES: Record<string, PastelTheme> = {
  cream: {
    key: 'cream',
    name: 'Warm Cream',
    bg: 'bg-[#FAF7F2]',
    card: 'bg-[#FDFBF7]',
    border: 'border-[#EBE2D5]',
    secondary: 'text-[#8C7A65]',
    text: 'text-[#4A3E31]'
  },
  peach: {
    key: 'peach',
    name: 'Cozy Peach',
    bg: 'bg-[#FCF0EB]',
    card: 'bg-[#FDF5F1]',
    border: 'border-[#F4DFD4]',
    secondary: 'text-[#A0705E]',
    text: 'text-[#533429]'
  },
  mint: {
    key: 'mint',
    name: 'Fresh Mint',
    bg: 'bg-[#EBF5F0]',
    card: 'bg-[#F1FAF6]',
    border: 'border-[#D4EADF]',
    secondary: 'text-[#5E8C73]',
    text: 'text-[#294B37]'
  },
  lavender: {
    key: 'lavender',
    name: 'Dreamy Lavender',
    bg: 'bg-[#F2EDF7]',
    card: 'bg-[#F6F2FC]',
    border: 'border-[#E3DBEF]',
    secondary: 'text-[#7D649B]',
    text: 'text-[#3B2952]'
  },
  sky: {
    key: 'sky',
    name: 'Breezy Sky',
    bg: 'bg-[#EDF3F7]',
    card: 'bg-[#F3F8FC]',
    border: 'border-[#DAE6EF]',
    secondary: 'text-[#617F9A]',
    text: 'text-[#243F57]'
  },
  rose: {
    key: 'rose',
    name: 'Dusty Rose',
    bg: 'bg-[#F8ECEE]',
    card: 'bg-[#FAF2F3]',
    border: 'border-[#ECD4D7]',
    secondary: 'text-[#9F656D]',
    text: 'text-[#4E292E]'
  },
  charcoal: {
    key: 'charcoal',
    name: 'Night Sleep',
    bg: 'bg-[#212123]',
    card: 'bg-[#29292D]',
    border: 'border-[#3D3D43]',
    secondary: 'text-[#A5A5AA]',
    text: 'text-[#ECECEF]'
  }
};

export const INITIAL_NOTES: Note[] = [
  {
    id: 'welcome-note',
    title: '✨ Welcome to Pillow',
    content: 'Pillow is a premium, high-class tactile notes app built to mimic physical comfort.\n\nEvery pixel is crafted to feel soft, responsive, and secure. Explore key elements like checking of checklist tasks, changing background themes at a touch, adding voice attachments, or locking highly sensitive pages under your secure PIN code!\n\nTap on any note to open the rich writer mode.',
    timestamp: Date.now() - 10 * 60 * 1000,
    colorKey: 'cream',
    isPinned: true,
    isArchived: false,
    isLocked: false,
    isChecklistMode: false,
    checklist: [],
    voiceAttachments: [],
    tags: ['Guide', 'Pillow'],
    folder: 'active'
  },
  {
    id: 'daily-rituals',
    title: '🌙 Evening Wind-down Checklist',
    content: 'A simple ritual to enter sleep mode with a clear mind.',
    timestamp: Date.now() - 3600000 * 3,
    colorKey: 'lavender',
    isPinned: true,
    isArchived: false,
    isLocked: false,
    isChecklistMode: true,
    checklist: [
      { id: 'item1', text: 'Put away all primary digital screens (Blue light off)', done: true },
      { id: 'item2', text: 'Review highlights & make space for tomorrow', done: false },
      { id: 'item3', text: 'Write reflections or quick logs into Pillow', done: false },
      { id: 'item4', text: '10 minutes deep diaphragmatic breathing', done: false }
    ],
    voiceAttachments: [],
    tags: ['Mindfulness', 'Ritual'],
    folder: 'active'
  },
  {
    id: 'secure-vault',
    title: '🔑 Highly Confidential Safe-Keep',
    content: 'This note is locked using Pillow\'s military-grade offline session encryption.\n\nYou must enter your 4-digit PIN lock code to inspect these confidential accounts and master keys. Never share your master pillow code with anyone else!',
    timestamp: Date.now() - 3600000 * 24,
    colorKey: 'peach',
    isPinned: false,
    isArchived: false,
    isLocked: true,
    isChecklistMode: false,
    checklist: [],
    voiceAttachments: [],
    tags: ['Security', 'Keys'],
    folder: 'active'
  },
  {
    id: 'inspiration-quote',
    title: '💭 Premium Focus & Creative Musings',
    content: '"The quieter you become, the more you are able to hear." — Ram Dass\n\nIdeas can strike in dark rooms or busy subways. Hit the microphone button inside a note editor to record thoughts on-the-go and replay them naturally inside Pillow.',
    timestamp: Date.now() - 3600000 * 48,
    colorKey: 'sky',
    isPinned: false,
    isArchived: false,
    isLocked: false,
    isChecklistMode: false,
    checklist: [],
    voiceAttachments: [],
    tags: ['Quotes', 'Inspiration'],
    folder: 'active'
  }
];
