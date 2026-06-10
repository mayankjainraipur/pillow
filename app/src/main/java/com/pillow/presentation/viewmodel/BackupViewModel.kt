package com.pillow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillow.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Coordinates backup/restore. The actual file IO (writing to a picked Uri, reading
 * a file, sharing) is supplied by the screen as suspend lambdas, so this ViewModel
 * stays free of Android Uri/Context concerns while still owning status reporting.
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _statusState = MutableStateFlow<String?>(null)
    val statusState: StateFlow<String?> = _statusState.asStateFlow()

    fun clearStatus() {
        _statusState.value = null
    }

    /** Build the backup blob and hand it to [write] (which persists it to a Uri). */
    fun exportTo(write: suspend (ByteArray) -> Unit) {
        viewModelScope.launch {
            try {
                write(backupManager.exportToBytes())
                _statusState.value = "Backup saved successfully"
            } catch (e: Exception) {
                _statusState.value = "Export failed: ${e.message}"
            }
        }
    }

    /** Read the backup blob via [read] and merge it into the database. */
    fun importFrom(read: suspend () -> ByteArray?) {
        viewModelScope.launch {
            try {
                val bytes = read()
                if (bytes == null || bytes.isEmpty()) {
                    _statusState.value = "Could not read the selected file"
                    return@launch
                }
                val count = backupManager.importFromBytes(bytes)
                _statusState.value = "Restored $count note(s)"
            } catch (e: Exception) {
                _statusState.value = "Restore failed: ${e.message}"
            }
        }
    }

    /** Build the backup blob and hand it to [share] (which fires a share intent). */
    fun shareVia(share: suspend (ByteArray) -> Unit) {
        viewModelScope.launch {
            try {
                share(backupManager.exportToBytes())
            } catch (e: Exception) {
                _statusState.value = "Share failed: ${e.message}"
            }
        }
    }
}
