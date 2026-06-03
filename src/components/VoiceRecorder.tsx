/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useRef, useEffect } from 'react';
import { Mic, Square, AlertCircle, Play, Pause, Trash2, Volume2, Clock } from 'lucide-react';
import { VoiceAttachment } from '../types';

interface VoiceRecorderProps {
  onAddAttachment: (attachment: VoiceAttachment) => void;
  accentTextColor: string;
}

export default function VoiceRecorder({ onAddAttachment, accentTextColor }: VoiceRecorderProps) {
  const [isRecording, setIsRecording] = useState(false);
  const [seconds, setSeconds] = useState(0);
  const [permissionError, setPermissionError] = useState(false);
  const [audioChunks, setAudioChunks] = useState<Blob[]>([]);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const timerRef = useRef<NodeJS.Timeout | null>(null);
  const streamRef = useRef<MediaStream | null>(null);

  useEffect(() => {
    return () => {
      stopTimer();
      cleanupStream();
    };
  }, []);

  const startTimer = () => {
    setSeconds(0);
    timerRef.current = setInterval(() => {
      setSeconds((prev) => prev + 1);
    }, 1000);
  };

  const stopTimer = () => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  };

  const cleanupStream = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }
  };

  const handleStartRecording = async (e: React.MouseEvent) => {
    e.preventDefault();
    setPermissionError(false);
    setAudioChunks([]);

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;

      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;

      const chunks: Blob[] = [];
      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunks.push(event.data);
        }
      };

      mediaRecorder.onstop = () => {
        const audioBlob = new Blob(chunks, { type: 'audio/webm' });
        const blobUrl = URL.createObjectURL(audioBlob);
        
        const duration = seconds > 0 ? seconds : 1;
        const newAttachment: VoiceAttachment = {
          id: `voice-${Date.now()}`,
          blobUrl,
          duration,
          createdAt: Date.now()
        };

        onAddAttachment(newAttachment);
        cleanupStream();
      };

      mediaRecorder.start();
      setIsRecording(true);
      startTimer();
    } catch (err) {
      console.error('Microphone usage blocked or unavailable:', err);
      setPermissionError(true);
    }
  };

  const handleStopRecording = (e: React.MouseEvent) => {
    e.preventDefault();
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      stopTimer();
    }
  };

  const formatTime = (totalSecs: number) => {
    const mins = Math.floor(totalSecs / 60);
    const secs = totalSecs % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div id="voice-memo-recorder-container" className="p-4 bg-black/5 rounded-2xl border border-black/[0.06] flex flex-col gap-3 my-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5 font-medium text-xs text-stone-500">
          <Volume2 className="w-4 h-4 text-stone-400" />
          <span>VOICE NOTE ATTACHMENT</span>
        </div>
        {isRecording && (
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 bg-rose-500 rounded-full animate-ping" />
            <span className="text-xs font-mono font-bold text-rose-500">REC</span>
          </div>
        )}
      </div>

      <div className="flex items-center justify-between bg-white/60 p-3 rounded-xl border border-black/[0.04]">
        {isRecording ? (
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-rose-100/80 rounded-full flex items-center justify-center border border-rose-200">
              <Mic className="w-5 h-5 text-rose-600 animate-pulse" />
            </div>
            <div>
              <p className="text-xs font-semibold text-rose-700">Recording live clip...</p>
              <p className="text-sm font-mono font-bold text-stone-700 flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-stone-400" />
                {formatTime(seconds)}
              </p>
            </div>
          </div>
        ) : (
          <div className="flex flex-col gap-0.5">
            <p className="text-xs font-semibold text-stone-700">Capture voice thoughts</p>
            <p className="text-[11px] text-stone-500">Store instant speech memos directly inside this note.</p>
          </div>
        )}

        <div>
          {isRecording ? (
            <button
              onClick={handleStopRecording}
              id="voice-mgr-recording-stop"
              className="p-3 bg-stone-800 hover:bg-stone-900 text-white rounded-full transition-transform hover:scale-105 active:scale-95 flex items-center justify-center shadow-md shadow-stone-800/10"
              title="Stop Recording"
            >
              <Square className="w-4 h-4 fill-white text-white" />
            </button>
          ) : (
            <button
              onClick={handleStartRecording}
              id="voice-mgr-recording-start"
              className="p-3 bg-[#4c3e34] hover:bg-[#3d3129] text-[#FAF7F2] rounded-full transition-all hover:scale-105 active:scale-95 flex items-center justify-center shadow-md shadow-[#4c3e34]/15"
              title="Record Voice Memo"
            >
              <Mic className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>

      {permissionError && (
        <div className="flex items-start gap-1.5 p-2 bg-rose-50 rounded-xl border border-rose-100 text-[11px] text-rose-600">
          <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
          <span>Microphone access requested. Please enable permission in your browser to record voice notes safely in the preview iframe.</span>
        </div>
      )}
    </div>
  );
}
