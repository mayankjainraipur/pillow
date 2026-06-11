package com.pillow.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState

private enum class ToolbarGroup { FORMAT, INSERT, BLOCKS }

/**
 * The grouped bottom toolbar for the note editor. The always-visible main row has
 * four groups — Format, Insert, Blocks, Voice. Selecting Format/Insert/Blocks
 * reveals that group's sub-actions in a row directly above; Voice opens the
 * recording panel via [onVoice].
 */
@Composable
fun EditorToolbar(
    state: RichTextState,
    onPickImage: () -> Unit,
    onPickFile: () -> Unit,
    onAddLink: () -> Unit,
    onInsertTable: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeGroup by remember { mutableStateOf<ToolbarGroup?>(ToolbarGroup.FORMAT) }
    val span = state.currentSpanStyle

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Sub-row for the active group.
            activeGroup?.let { group ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    when (group) {
                        ToolbarGroup.FORMAT -> {
                            ToolItem(Icons.Filled.FormatBold, "Bold",
                                active = span.fontWeight == FontWeight.Bold) {
                                state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            }
                            ToolItem(Icons.Filled.FormatItalic, "Italic",
                                active = span.fontStyle == FontStyle.Italic) {
                                state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            }
                            ToolItem(Icons.Filled.FormatUnderlined, "Underline",
                                active = span.textDecoration == TextDecoration.Underline) {
                                state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                            }
                            ToolItem(Icons.Filled.Title, "Heading 1",
                                active = span.fontSize == 28.sp) {
                                state.toggleSpanStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold))
                            }
                            ToolItem(Icons.Filled.TextFields, "Heading 2",
                                active = span.fontSize == 22.sp) {
                                state.toggleSpanStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                            }
                            ToolItem(Icons.AutoMirrored.Filled.FormatListBulleted, "List",
                                active = state.isUnorderedList) {
                                state.toggleUnorderedList()
                            }
                            ToolItem(Icons.Filled.Checklist, "Checklist", active = false) {
                                state.replaceSelectedText("- [ ] ")
                            }
                        }
                        ToolbarGroup.INSERT -> {
                            ToolItem(Icons.Filled.Image, "Image", active = false, onClick = onPickImage)
                            ToolItem(Icons.Filled.Link, "Link", active = false, onClick = onAddLink)
                            ToolItem(Icons.Filled.AttachFile, "File", active = false, onClick = onPickFile)
                        }
                        ToolbarGroup.BLOCKS -> {
                            ToolItem(Icons.Filled.Code, "Code", active = state.isCodeSpan) {
                                state.toggleCodeSpan()
                            }
                            ToolItem(Icons.Filled.FormatQuote, "Quote", active = false) {
                                state.replaceSelectedText("> ")
                            }
                            ToolItem(Icons.Filled.TableChart, "Table", active = false, onClick = onInsertTable)
                        }
                    }
                }
                HorizontalDivider()
            }

            // Always-visible main group row.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                MainGroup(Icons.Filled.TextFields, "Format",
                    selected = activeGroup == ToolbarGroup.FORMAT) {
                    activeGroup = if (activeGroup == ToolbarGroup.FORMAT) null else ToolbarGroup.FORMAT
                }
                MainGroup(Icons.Filled.Add, "Insert",
                    selected = activeGroup == ToolbarGroup.INSERT) {
                    activeGroup = if (activeGroup == ToolbarGroup.INSERT) null else ToolbarGroup.INSERT
                }
                MainGroup(Icons.Filled.Code, "Blocks",
                    selected = activeGroup == ToolbarGroup.BLOCKS) {
                    activeGroup = if (activeGroup == ToolbarGroup.BLOCKS) null else ToolbarGroup.BLOCKS
                }
                MainGroup(Icons.Filled.Mic, "Voice", selected = false, onClick = onVoice)
            }
        }
    }
}

@Composable
private fun ToolItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = if (active) {
            IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        } else {
            IconButtonDefaults.iconButtonColors()
        }
    ) {
        Icon(imageVector = icon, contentDescription = label)
    }
}

@Composable
private fun MainGroup(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            colors = if (selected) {
                IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            } else {
                IconButtonDefaults.iconButtonColors()
            }
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
