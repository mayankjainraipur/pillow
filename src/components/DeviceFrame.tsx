/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { Smartphone, MonitorPlay, Wifi, Battery, RefreshCw } from 'lucide-react';

interface DeviceFrameProps {
  children: React.ReactNode;
  isFullWidth: boolean;
  setIsFullWidth: (value: boolean) => void;
  onResetApp: () => void;
}

export default function DeviceFrame({
  children,
  isFullWidth,
  setIsFullWidth,
  onResetApp
}: DeviceFrameProps) {
  const [timeStr, setTimeStr] = useState('12:00 PM');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTimeStr(
        now.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit', hour12: true })
      );
    };
    updateTime();
    const interval = setInterval(updateTime, 15000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div id="pillow-device-wrapper" className="min-h-screen bg-zinc-900 text-zinc-100 flex flex-col items-center justify-center p-2 md:p-6 font-sans">
      {/* Top Banner Control Panel */}
      <div className="w-full max-w-lg mb-4 flex items-center justify-between px-4 py-2 bg-zinc-800/80 backdrop-blur-md rounded-2xl border border-zinc-700/50 shadow-lg">
        <div className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded-full bg-indigo-400 animate-pulse" />
          <h1 className="font-semibold text-sm tracking-tight text-zinc-200">
            Pillow • Premium Android View
          </h1>
        </div>

        <div className="flex gap-2">
          {/* Refresh Applet State */}
          <button
            onClick={() => {
              if (window.confirm('Reset app data to default premium demonstration list?')) {
                onResetApp();
              }
            }}
            id="reset-state-banner-btn"
            title="Reset to default Pillow notes list"
            className="p-1 px-2 text-xs bg-zinc-700/50 hover:bg-zinc-700 hover:text-white rounded-lg flex items-center gap-1.5 transition-all text-zinc-300"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Reset</span>
          </button>

          {/* Toggle Full Screen / Simulated Phone */}
          <button
            onClick={() => setIsFullWidth(!isFullWidth)}
            id="toggle-full-frame-btn"
            className="p-1 px-2.5 text-xs font-medium bg-[#4f46e5]/90 hover:bg-[#4f46e5] text-white rounded-lg flex items-center gap-1.5 transition-all shadow-sm"
          >
            {isFullWidth ? (
              <>
                <Smartphone className="w-3.5 h-3.5" />
                <span>Mobile Frame</span>
              </>
            ) : (
              <>
                <MonitorPlay className="w-3.5 h-3.5" />
                <span>Full Web</span>
              </>
            )}
          </button>
        </div>
      </div>

      {isFullWidth ? (
        /* Full Desktop/IFrame Scale Workspace */
        <div id="full-workspace-view" className="w-full max-w-5xl h-[85vh] bg-[#FAF7F2] text-zinc-800 rounded-3xl border border-zinc-700/50 shadow-2xl relative overflow-hidden">
          {children}
        </div>
      ) : (
        /* Interactive Smartphone Mock Frame */
        <div
          id="simulated-smartphone-frame"
          className="relative w-full max-w-[420px] aspect-[9/19] h-[840px] bg-zinc-950 p-[12px] rounded-[52px] shadow-[0_25px_60px_-15px_rgba(0,0,0,0.9)] border-[5px] border-zinc-800/80 flex flex-col overflow-hidden ring-[12px] ring-zinc-900/60 ring-offset-4 ring-offset-zinc-950"
        >
          {/* Dynamic Notch/Speaker Island */}
          <div className="absolute top-4 left-1/2 -translate-x-1/2 w-32 h-6 bg-black rounded-full z-50 flex items-center justify-center p-1.5 gap-2 shadow-inner">
            <div className="w-12 h-1 bg-zinc-800 rounded-full" />
            <div className="w-2 h-2 bg-zinc-900 rounded-full border border-zinc-800" />
          </div>

          {/* Device Screen Area */}
          <div className="w-full h-full rounded-[40px] overflow-hidden bg-[#FAF7F2] flex flex-col relative">
            
            {/* Soft Android Status Bar */}
            <div className="h-9 w-full bg-transparent px-6 flex items-center justify-between text-[#4A3E31] text-xs font-medium select-none z-45 shrink-0 pt-1.5">
              <span>{timeStr.replace(/ [AP]M$/, '')}</span>
              <div className="flex items-center gap-1.5">
                <Wifi className="w-3.5 h-3.5" />
                <span className="text-[10px] font-mono tracking-tight">5G</span>
                <span className="text-[9px] font-mono tracking-tight bg-[#4A3E31]/10 px-1 rounded">V_M</span>
                <Battery className="w-4 h-4" />
              </div>
            </div>

            {/* Simulated Content Area */}
            <div className="flex-1 flex flex-col overflow-hidden relative">
              {children}
            </div>

            {/* Active Gesture Home Pill Pill */}
            <div className="h-6 w-full flex items-center justify-center bg-transparent shrink-0">
              <div className="w-28 h-1 bg-[#4A3E31]/20 rounded-full shadow-inner hover:bg-[#4A3E31]/40 transition-colors" />
            </div>

          </div>
        </div>
      )}
    </div>
  );
}
