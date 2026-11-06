package com.tj.expensetracker.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tj.expensetracker.ui.theme.AppVisualStyle
import com.tj.expensetracker.ui.theme.GlassSurface
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    visualStyle: AppVisualStyle,
    onVisualStyleChange: (AppVisualStyle) -> Unit,
    onDataManagementClick: () -> Unit
) {
    var showThemeDialog by remember {
        mutableStateOf(false)
    }

    var showCurrencyDialog by remember {
        mutableStateOf(false)
    }

    var showAboutDialog by remember {
        mutableStateOf(false)
    }
    var showVisualStyleDialog by remember {
        mutableStateOf(false)
    }

    val isGlass = visualStyle == AppVisualStyle.GLASS

    Scaffold(
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings")
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                SettingsItem(
                    isGlass = isGlass,
                    icon = {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null
                        )
                    },
                    title = "Appearance",
                    subtitle = when (themeMode) {
                        ThemeMode.SYSTEM -> "System default"
                        ThemeMode.LIGHT -> "Light theme"
                        ThemeMode.DARK -> "Dark theme"
                    },
                    onClick = {
                        showThemeDialog = true
                    }
                )
            }

            item {
                SettingsItem(
                    isGlass = isGlass,
                    icon = {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null
                        )
                    },
                    title = "Visual Style",
                    subtitle = when (visualStyle) {
                        AppVisualStyle.CLASSIC -> "Classic"
                        AppVisualStyle.GLASS -> "Glass"
                    },
                    onClick = {
                        showVisualStyleDialog = true
                    }
                )
            }

            item {
                SettingsItem(
                    isGlass = isGlass,
                    icon = {
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null
                        )
                    },
                    title = "Currency",
                    subtitle = "Indian Rupee (₹ INR)",
                    onClick = {
                        showCurrencyDialog = true
                    }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Data",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                SettingsItem(
                    isGlass = isGlass,
                    icon = {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null
                        )
                    },
                    title = "Data Management",
                    subtitle = "Backup and export options",
                    onClick = onDataManagementClick
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                SettingsItem(
                    isGlass = isGlass,
                    icon = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null
                        )
                    },
                    title = "About Expense Tracker",
                    subtitle = "App information",
                    onClick = {
                        showAboutDialog = true
                    }
                )
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = {
                showThemeDialog = false
            },
            title = {
                Text("Choose Theme")
            },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeModeChange(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    onThemeModeChange(mode)
                                    showThemeDialog = false
                                }
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                when (mode) {
                                    ThemeMode.SYSTEM -> "System default"
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showThemeDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = {
                showCurrencyDialog = false
            },
            title = {
                Text("Currency")
            },
            text = {
                Text(
                    "Indian Rupee (₹ INR) is currently the default currency. " +
                            "We will connect currency preferences to transaction formatting next."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCurrencyDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = {
                showAboutDialog = false
            },
            title = {
                Text("Expense Tracker")
            },
            text = {
                Text(
                    "Track your balance, income, expenses and spending trends."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAboutDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }


    if (showVisualStyleDialog) {
        AlertDialog(
            onDismissRequest = {
                showVisualStyleDialog = false
            },
            title = {
                Text("Choose Visual Style")
            },
            text = {
                Column {
                    AppVisualStyle.entries.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onVisualStyleChange(style)
                                    showVisualStyleDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = visualStyle == style,
                                onClick = {
                                    onVisualStyleChange(style)
                                    showVisualStyleDialog = false
                                }
                            )

                            Spacer(Modifier.width(8.dp))

                            Column {
                                Text(
                                    when (style) {
                                        AppVisualStyle.CLASSIC -> "Classic"
                                        AppVisualStyle.GLASS -> "Glassmorphism"
                                    }
                                )

                                Text(
                                    text = when (style) {
                                        AppVisualStyle.CLASSIC ->
                                            "Keep the current app design"
                                        AppVisualStyle.GLASS ->
                                            "Light colors and frosted-glass cards"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showVisualStyleDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

}

@Composable
private fun SettingsItem(
    isGlass: Boolean,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    SettingsSurface(
        isGlass = isGlass,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsSurface(
    isGlass: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    if (isGlass) {
        GlassSurface(
            modifier = modifier,
            content = content
        )
    } else {
        Card(modifier = modifier) {
            Box(content = content)
        }
    }
}

