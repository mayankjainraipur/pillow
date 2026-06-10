package com.pillow.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around [MediaRecorder] that records AAC audio into the app's
 * private files directory. One recording at a time.
 */
@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt: Long = 0L

    val isRecording: Boolean get() = recorder != null

    /** Begins recording and returns the destination file. */
    fun start(): File {
        stop() // defensive: never leak a previous recorder

        val dir = File(context.filesDir, "voice_memos").apply { mkdirs() }
        val file = File(dir, "memo_${System.currentTimeMillis()}.m4a")

        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        rec.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        recorder = rec
        outputFile = file
        startedAt = System.currentTimeMillis()
        return file
    }

    /**
     * Stops recording. Returns the recorded file and its duration in ms, or null
     * if nothing was being recorded.
     */
    fun stop(): RecordingResult? {
        val rec = recorder ?: return null
        val file = outputFile
        val durationMs = System.currentTimeMillis() - startedAt

        recorder = null
        outputFile = null

        return try {
            rec.stop()
            rec.release()
            if (file != null) RecordingResult(file, durationMs) else null
        } catch (e: RuntimeException) {
            // stop() throws if start() captured no audio; discard the partial file.
            rec.release()
            file?.delete()
            null
        }
    }

    data class RecordingResult(val file: File, val durationMs: Long)
}
