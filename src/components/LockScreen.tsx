/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Lock, Delete, ShieldAlert, ArrowLeft, KeyRound } from 'lucide-react';

interface LockScreenProps {
  storedPin: string | null;
  onSuccess: (pin: string) => void;
  onCancel: () => void;
  isCancelable?: boolean;
  actionType: 'unlock' | 'setup' | 'disable';
}

export default function LockScreen({
  storedPin,
  onSuccess,
  onCancel,
  isCancelable = false,
  actionType = 'unlock'
}: LockScreenProps) {
  const [pin, setPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');
  const [isConfirming, setIsConfirming] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [shakeTrigger, setShakeTrigger] = useState(false);

  const getTitle = () => {
    if (actionType === 'setup') {
      return isConfirming ? 'Confirm Pillow-PIN' : 'Set your Pillow-PIN';
    }
    if (actionType === 'disable') {
      return 'Enter PIN to disable lock';
    }
    return 'Pillow is Locked';
  };

  const getSubtitle = () => {
    if (actionType === 'setup') {
      return isConfirming
        ? 'Re-enter your 4-digit code to double-check.'
        : 'Protect your intimate reflections and vaults.';
    }
    return 'Please enter your 4-digit security PIN.';
  };

  const handleKeyPress = (num: string) => {
    if (pin.length < 4) {
      setErrorMsg('');
      const newPin = pin + num;
      setPin(newPin);

      // Trigger automatic validation when 4 digits are completed
      if (newPin.length === 4) {
        // Complete digit validation after short visual feedback
        setTimeout(() => {
          validatePin(newPin);
        }, 150);
      }
    }
  };

  const handleDelete = () => {
    if (pin.length > 0) {
      setPin(pin.slice(0, -1));
      setErrorMsg('');
    }
  };

  const triggerError = (msg: string) => {
    setErrorMsg(msg);
    setShakeTrigger(true);
    setPin('');
    setTimeout(() => setShakeTrigger(false), 500);
  };

  const validatePin = (inputPin: string) => {
    if (actionType === 'unlock' || actionType === 'disable') {
      if (inputPin === storedPin) {
        onSuccess(inputPin);
      } else {
        triggerError('Incorrect security PIN. Please try again.');
      }
    } else if (actionType === 'setup') {
      if (!isConfirming) {
        // First code entered, now confirm
        setConfirmPin(inputPin);
        setIsConfirming(true);
        setPin('');
      } else {
        // Confirming code match
        if (inputPin === confirmPin) {
          onSuccess(inputPin);
        } else {
          setIsConfirming(false);
          setConfirmPin('');
          triggerError('PINs did not match. Let\'s try again.');
        }
      }
    }
  };

  const numberKeys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '', '0', 'delete'];

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.95 }}
      id="pillow-lock-screen"
      className="absolute inset-0 bg-[#161618] text-white flex flex-col justify-between p-6 z-50 rounded-[40px] overflow-hidden"
    >
      {/* Header */}
      <div className="flex items-center justify-between mt-6">
        {isCancelable ? (
          <button
            onClick={onCancel}
            id="lock-screen-back-btn"
            className="p-3 bg-[#242426] rounded-full text-zinc-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
        ) : (
          <div className="w-10 h-10" />
        )}
        <div className="text-zinc-500 font-mono text-xs flex items-center gap-1.5 bg-[#242426] px-3 py-1 rounded-full">
          <KeyRound className="w-3.5 h-3.5 text-zinc-400" />
          <span>VAULT PROTECT</span>
        </div>
        <div className="w-10 h-10" />
      </div>

      {/* Main Status */}
      <div className="flex flex-col items-center justify-center flex-1 my-4">
        <motion.div
          animate={shakeTrigger ? { x: [-10, 10, -8, 8, -5, 5, 0] } : {}}
          transition={{ duration: 0.4 }}
          className="flex flex-col items-center text-center"
        >
          <div className="w-16 h-16 bg-[#2B2B30] rounded-full flex items-center justify-center mb-6 shadow-xl border border-zinc-700/50">
            <Lock className={`w-8 h-8 ${errorMsg ? 'text-rose-400' : 'text-amber-300'} animate-pulse`} />
          </div>

          <h2 className="text-2xl font-bold tracking-tight mb-2">{getTitle()}</h2>
          <p className="text-sm text-zinc-400 max-w-[240px]">{getSubtitle()}</p>

          {/* Dots Indicator */}
          <div className="flex gap-4 my-8">
            {[0, 1, 2, 3].map((index) => {
              const active = pin.length > index;
              return (
                <div
                  key={index}
                  className={`w-4 h-4 rounded-full transition-all duration-200 border-2 ${
                    active
                      ? 'bg-amber-300 border-amber-300 scale-110 shadow-[0_0_8px_rgba(252,211,77,0.5)]'
                      : 'border-zinc-700 bg-transparent'
                  }`}
                />
              );
            })}
          </div>

          {/* Error Message */}
          <div className="h-6">
            <AnimatePresence>
              {errorMsg && (
                <motion.div
                  initial={{ opacity: 0, y: -5 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0 }}
                  className="flex items-center gap-1.5 text-xs text-rose-400 font-medium"
                >
                  <ShieldAlert className="w-4.5 h-4.5 shrink-0" />
                  <span>{errorMsg}</span>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </motion.div>
      </div>

      {/* Touchpad / Pin Keys */}
      <div className="grid grid-cols-3 gap-y-4 gap-x-8 max-w-xs mx-auto mb-10 w-full">
        {numberKeys.map((key, index) => {
          if (key === '') {
            return <div key={`empty-${index}`} />;
          }

          if (key === 'delete') {
            return (
              <button
                key="delete"
                onClick={handleDelete}
                id="lock-screen-keypoint-del"
                className="flex items-center justify-center py-4 text-zinc-400 hover:text-white transition-colors text-lg font-medium"
              >
                <Delete className="w-6 h-6" />
              </button>
            );
          }

          return (
            <motion.button
              key={key}
              whileTap={{ scale: 0.9 }}
              onClick={() => handleKeyPress(key)}
              id={`lock-screen-keypoint-${key}`}
              className="py-4 text-2xl font-semibold bg-[#242426] hover:bg-[#2C2C2F] text-zinc-200 rounded-full flex items-center justify-center aspect-square shadow-md border border-zinc-800/40 transition-colors"
            >
              {key}
            </motion.button>
          );
        })}
      </div>
    </motion.div>
  );
}
