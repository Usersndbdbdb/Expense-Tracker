
package com.tj.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tj.expensetracker.navigation.AppNavigation
import com.tj.expensetracker.ui.settings.ThemeMode
import com.tj.expensetracker.ui.theme.AppVisualStyle
import com.tj.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences = remember {
                getSharedPreferences("app_settings", MODE_PRIVATE)
            }

            var themeMode by remember {
                mutableStateOf(
                    runCatching {
                        ThemeMode.valueOf(
                            preferences.getString(
                                "theme_mode",
                                ThemeMode.SYSTEM.name
                            ) ?: ThemeMode.SYSTEM.name
                        )
                    }.getOrDefault(ThemeMode.SYSTEM)
                )
            }

            var visualStyle by remember {
                mutableStateOf(
                    runCatching {
                        AppVisualStyle.valueOf(
                            preferences.getString(
                                "visual_style",
                                AppVisualStyle.CLASSIC.name
                            ) ?: AppVisualStyle.CLASSIC.name
                        )
                    }.getOrDefault(AppVisualStyle.CLASSIC)
                )
            }

            ExpenseTrackerTheme(
                themeMode = themeMode,
                visualStyle = visualStyle
            ) {
                AppNavigation(
                    themeMode = themeMode,
                    onThemeModeChange = { selectedMode ->
                        preferences.edit()
                            .putString("theme_mode", selectedMode.name)
                            .apply()

                        themeMode = selectedMode
                    },
                    visualStyle = visualStyle,
                    onVisualStyleChange = { selectedStyle ->
                        preferences.edit()
                            .putString("visual_style", selectedStyle.name)
                            .apply()

                        visualStyle = selectedStyle
                    }
                )
            }
        }
    }
}
