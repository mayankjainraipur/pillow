package com.pillow.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.pillow.domain.model.Note

/** Renders a note as a Markdown document: an H1 title (when present) then the body. */
fun buildNoteMarkdown(note: Note): String = buildString {
    val title = note.title.trim()
    if (title.isNotEmpty()) {
        append("# ").append(title).append("\n\n")
    }
    append(note.content)
}

/** Opens the Android share sheet with the note rendered as plain text. */
fun shareNoteText(context: Context, note: Note) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Note" })
        putExtra(Intent.EXTRA_TEXT, buildNoteMarkdown(note))
    }
    context.startActivity(Intent.createChooser(intent, "Share note"))
}

/** Writes the note's Markdown to a user-chosen document Uri (from CreateDocument). */
fun writeNoteToUri(context: Context, note: Note, uri: Uri) {
    context.contentResolver.openOutputStream(uri)?.use { out ->
        out.write(buildNoteMarkdown(note).toByteArray(Charsets.UTF_8))
    }
}

/** A safe `.md` file name derived from the note title. */
fun suggestedFileName(note: Note): String {
    val base = note.title.trim().ifEmpty { "note" }
        .replace(Regex("[^A-Za-z0-9 _-]"), "")
        .trim()
        .ifEmpty { "note" }
        .take(50)
    return "$base.md"
}

/**
 * Strips common Markdown markers so list/card previews read cleanly instead of
 * showing raw `**bold**`, `# heading`, `- [ ]`, etc. Best-effort, not a full parser.
 */
fun stripMarkdown(text: String): String {
    var s = text
    s = s.replace(Regex("(?m)^\\s{0,3}#{1,6}\\s+"), "")        // headings
    s = s.replace(Regex("(?m)^\\s*>\\s?"), "")                  // block quotes
    s = s.replace(Regex("(?m)^\\s*[-*]\\s+\\[[ xX]]\\s*"), "")  // task list markers
    s = s.replace(Regex("(?m)^\\s*[-*+]\\s+"), "")              // bullet markers
    s = s.replace(Regex("(\\*\\*|__)(.*?)\\1"), "$2")          // bold
    s = s.replace(Regex("(\\*|_)(.*?)\\1"), "$2")              // italic
    s = s.replace(Regex("`{1,3}([^`]*)`{1,3}"), "$1")          // inline/code
    return s.trim()
}
