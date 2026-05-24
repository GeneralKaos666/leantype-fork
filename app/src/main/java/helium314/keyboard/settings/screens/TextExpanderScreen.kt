/*
 * Copyright (C) 2026 LeanBitLab
 * SPDX-License-Identifier: GPL-3.0-only
 */
package helium314.keyboard.settings.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.TextExpanderUtils
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.SearchScreen
import helium314.keyboard.settings.SettingsActivity
import helium314.keyboard.settings.dialogs.ThreeButtonAlertDialog
import helium314.keyboard.settings.preferences.SwitchPreference

@Composable
fun TextExpanderScreen(onClickBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.prefs()

    // Trigger recomposition on preference changes
    val prefUpdateState = (context as? SettingsActivity)?.prefChanged?.collectAsState()
    
    var prefixText by remember(prefUpdateState?.value) {
        mutableStateOf(TextExpanderUtils.getPrefix(context))
    }
    
    var isExpanderEnabled by remember(prefUpdateState?.value) {
        mutableStateOf(TextExpanderUtils.isEnabled(context))
    }

    var shortcutsMap by remember(prefUpdateState?.value) {
        mutableStateOf(TextExpanderUtils.getShortcuts(context))
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingShortcut by remember { mutableStateOf("") }
    var editingTemplate by remember { mutableStateOf("") }
    var originalShortcutToEdit by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        SearchScreen(
            onClickBack = onClickBack,
            title = {
                Text(
                    text = "Text Expander",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            },
            filteredItems = { emptyList<Int>() },
            itemContent = { },
            content = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Informational Card
                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Dynamic Variables Supported",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Use these placeholders in your templates:\n" +
                                        "• %date% : Inserts current date (YYYY-MM-DD)\n" +
                                        "• %time% : Inserts current time (HH:MM)\n" +
                                        "• %clipboard% : Inserts clipboard text",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 1. Master Switch Toggle
                    SwitchPreference(
                        name = "Enable Text Expander",
                        key = TextExpanderUtils.PREF_ENABLED,
                        default = false,
                        description = "Auto-expand shortcuts on space or punctuation natively and securely.",
                        onCheckedChange = { isExpanderEnabled = it }
                    )

                    // 2. Custom Prefix Configuration
                    OutlinedTextField(
                        value = prefixText,
                        onValueChange = {
                            prefixText = it
                            prefs.edit { putString(TextExpanderUtils.PREF_PREFIX, it) }
                        },
                        label = { Text("Shortcut Prefix (e.g. '..', '.', ';', or blank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isExpanderEnabled
                    )

                    // 3. Section Title / Header for shortcuts
                    Text(
                        text = "Custom Shortcuts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // 4. List of saved shortcuts
                    if (shortcutsMap.isEmpty()) {
                        androidx.compose.material3.Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No shortcuts configured yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Tap the '+' floating action button below to add your first text template!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        shortcutsMap.forEach { (shortcut, template) ->
                            ShortcutItem(
                                shortcut = shortcut,
                                template = template,
                                prefix = prefixText,
                                onEdit = {
                                    editingShortcut = shortcut
                                    editingTemplate = template
                                    originalShortcutToEdit = shortcut
                                    showAddDialog = true
                                },
                                onDelete = {
                                    val updated = shortcutsMap.toMutableMap()
                                    updated.remove(shortcut)
                                    shortcutsMap = updated
                                    TextExpanderUtils.saveShortcuts(context, updated)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        )

        // Floating Action Button to Add New Shortcut
        if (isExpanderEnabled) {
            ExtendedFloatingActionButton(
                onClick = {
                    editingShortcut = ""
                    editingTemplate = ""
                    originalShortcutToEdit = null
                    showAddDialog = true
                },
                text = { Text("Add Shortcut") },
                icon = { Icon(painter = painterResource(R.drawable.ic_edit), "Add Shortcut") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(all = 16.dp)
                    .then(Modifier.safeDrawingPadding())
            )
        }
    }

    // Add / Edit Shortcut Dialog
    if (showAddDialog) {
        val focusRequester = remember { FocusRequester() }
        val isEditMode = originalShortcutToEdit != null
        
        ThreeButtonAlertDialog(
            onDismissRequest = { showAddDialog = false },
            onConfirmed = {
                val updated = shortcutsMap.toMutableMap()
                if (isEditMode && originalShortcutToEdit != editingShortcut) {
                    updated.remove(originalShortcutToEdit)
                }
                updated[editingShortcut.trim()] = editingTemplate
                shortcutsMap = updated
                TextExpanderUtils.saveShortcuts(context, updated)
                showAddDialog = false
            },
            checkOk = { editingShortcut.trim().isNotEmpty() && editingTemplate.isNotEmpty() },
            confirmButtonText = if (isEditMode) "Save" else "Add",
            neutralButtonText = if (isEditMode) "Delete" else null,
            onNeutral = {
                if (isEditMode) {
                    val updated = shortcutsMap.toMutableMap()
                    updated.remove(originalShortcutToEdit)
                    shortcutsMap = updated
                    TextExpanderUtils.saveShortcuts(context, updated)
                }
                showAddDialog = false
            },
            title = {
                Text(text = if (isEditMode) "Edit Shortcut" else "Add Shortcut")
            },
            content = {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = editingShortcut,
                        onValueChange = { editingShortcut = it.replace(" ", "") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        label = { Text("Shortcut (e.g. 'brb', 'em')") }
                    )
                    
                    OutlinedTextField(
                        value = editingTemplate,
                        onValueChange = { editingTemplate = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        label = { Text("Template Expansion") },
                        placeholder = { Text("Be right back! or My email is %clipboard%") }
                    )
                }
            }
        )
    }
}

@Composable
private fun ShortcutItem(
    shortcut: String,
    template: String,
    prefix: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$prefix$shortcut",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.ic_bin),
                    contentDescription = "Delete shortcut",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
