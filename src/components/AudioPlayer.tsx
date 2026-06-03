/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useRef, useEffect } from 'react';
import { Play, Pause, Trash2, Volume2, Music, Clock } from 'lucide-react';
import { VoiceAttachment } from '../types';

interface AudioPlayerProps {
  key?: string;
  attachment: VoiceAttachment;
  onDelete: (id: string) => void;
  accentTextColor: string;
}

export default function AudioPlayer({ attachment, onDelete, accentTextColor }: AudioPlayerProps) {
  const [isPlaying, setIsPlaying] = useState(false);
  const [progress, setProgress] = useState(0);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    const audio = new Audio(attachment.blobUrl);
    audioRef.current = audio;

    const handleTimeUpdate = () => {
      if (audio.duration) {
        setProgress((audio.currentTime / audio.duration) * 100);
      }
    };

    const handleEnded = () => {
      setIsPlaying(false);
      setProgress(0);
    };

    audio.addEventListener('timeupdate', handleTimeUpdate);
    audio.addEventListener('ended', handleEnded);

    return () => {
      audio.pause();
      audio.removeEventListener('timeupdate', handleTimeUpdate);
      audio.removeEventListener('ended', handleEnded);
    };
  }, [attachment.blobUrl]);

  const handlePlayPause = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!audioRef.current) return;

    if (isPlaying) {
      audioRef.current.pause();
      setIsPlaying(false);
    } else {
      audioRef.current.play().catch((err) => {
        console.error('Audio playback failed:', err);
      });
      setIsPlaying(true);
    }
  };

  const formatDuration = (secs: number) => {
    const mins = Math.floor(secs / 60);
    const remaining = Math.round(secs % 60);
    return `${mins}:${remaining.toString().padStart(2, '0')}s`;
  };

  return (
    <div id={`voice-memo-player-${attachment.id}`} className="mt-2 p-3 bg-white/70 border border-black/[0.04] rounded-xl flex items-center justify-between gap-3 shadow-sm hover:shadow transition-all">
      <div className="flex items-center gap-3 flex-1 overflow-hidden">
        <button
          onClick={handlePlayPause}
          id={`play-pause-btn-${attachment.id}`}
          className="w-9 h-9 bg-[#4c3e34] hover:bg-[#3d3129] text-white rounded-full flex items-center justify-center shrink-0 shadow-sm transition-transform active:scale-90"
        >
          {isPlaying ? <Pause className="w-4 h-4 fill-white" /> : <Play className="w-4 h-4 fill-white ml-0.5" />}
        </button>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1.5 text-xs font-semibold text-stone-700">
            <Music className="w-3.5 h-3.5 text-amber-600 shrink-0" />
            <span className="truncate">Voice Attachment</span>
            <span className="text-[10px] font-mono text-stone-400">({formatDuration(attachment.duration)})</span>
          </div>

          {/* Simple progress track bar */}
          <div className="w-full bg-stone-200/80 h-1 rounded-full mt-1.5 overflow-hidden">
            <div
              className="bg-[#8C7A65] h-full transition-all duration-100"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>
      </div>

      <button
        type="button"
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          onDelete(attachment.id);
        }}
        id={`voice-player-del-btn-${attachment.id}`}
        className="p-2 text-stone-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-colors shrink-0"
        title="Delete Voice Memo"
      >
        <Trash2 className="w-4 h-4" />
      </button>
    </div>
  );
}
