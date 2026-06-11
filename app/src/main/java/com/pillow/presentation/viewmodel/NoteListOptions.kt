package com.pillow.presentation.viewmodel

/** How the notes list is ordered. Applied in-memory on top of the loaded list. */
enum class SortOption(val label: String) {
    LAST_MODIFIED("Last Modified"),
    DATE_CREATED("Date Created"),
    ALPHA_AZ("Alphabetical A-Z"),
    ALPHA_ZA("Alphabetical Z-A"),
    PINNED_FIRST("Pinned First")
}

/**
 * Which subset of notes the home list shows. [ALL] and [ARCHIVED] switch the
 * underlying data source; the rest are in-memory predicates over the active list.
 */
enum class NoteFilter(val label: String) {
    ALL("All Notes"),
    WITH_IMAGES("With Images"),
    WITH_ATTACHMENTS("With Attachments"),
    CHECKLIST("Checklist Notes"),
    SHARED("Shared Notes"),
    ARCHIVED("Archived Notes")
}
