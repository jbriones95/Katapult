package com.gezimos.katapult.ui

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gezimos.katapult.R
import androidx.compose.ui.unit.dp
import com.gezimos.katapult.MainViewModel
import com.gezimos.katapult.Screen
import com.gezimos.katapult.util.PrefsManager

@Composable
fun App(
    viewModel: MainViewModel,
    imagePicker: ActivityResultLauncher<String>,
    iconPicker: ActivityResultLauncher<Array<String>>,
) {
    val iconShape = if (viewModel.roundedIcons) RoundedIconShape else CircleShape
    val smallShape = if (viewModel.roundedIcons) RoundedSmallShape else CircleShape
    val badgeShape = if (viewModel.roundedIcons) RoundedBadgeShape else CircleShape
    val dark = when (viewModel.themeMode) {
        com.gezimos.katapult.util.PrefsManager.THEME_DARK -> true
        com.gezimos.katapult.util.PrefsManager.THEME_SYSTEM -> isSystemInDarkTheme()
        else -> false
    }
    val metrics = UiMetrics.forConfiguration(LocalConfiguration.current)
    // Keep the user's small/large choice, but map it onto a size that fits a square screen.
    val iconSize = if (metrics.compact) {
        if (viewModel.iconSize >= PrefsManager.ICON_SIZE_LARGE) 64.dp else 56.dp
    } else {
        viewModel.iconSize.dp
    }
    CompositionLocalProvider(
        androidx.compose.foundation.LocalIndication provides NoIndication,
        LocalUiMetrics provides metrics,
        LocalIconSize provides iconSize,
        LocalIconShape provides iconShape,
        LocalSmallIconShape provides smallShape,
        LocalBadgeShape provides badgeShape,
        LocalSheetDismissSignal provides viewModel.sheetDismissSignal,
        LocalInk provides if (dark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black,
        LocalSurface provides if (dark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
        LocalIconFilter provides if (dark) EinkColorFilterDark else EinkColorFilter,
    ) {
        when (viewModel.screen) {
            Screen.ONBOARDING -> OnboardingScreen(viewModel)
            Screen.HOME -> HomeScreen(viewModel, imagePicker, iconPicker)
            Screen.ALL_APPS -> AllAppsScreen(viewModel, iconPicker)
            Screen.SETTINGS -> SettingsScreen(viewModel)
        }
        com.gezimos.katapult.lockscreen.LockscreenReEnableSheet(viewModel)
        if (viewModel.iconImportError) {
            val context = LocalContext.current
            val message = stringResource(R.string.icon_import_failed)
            LaunchedEffect(Unit) {
                android.widget.Toast.makeText(
                    context, message, android.widget.Toast.LENGTH_LONG,
                ).show()
                viewModel.iconImportError = false
            }
        }
    }
}
