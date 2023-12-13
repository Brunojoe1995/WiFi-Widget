package com.w2sv.wifiwidget.ui.screens.home.components.widget

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.HomeScreenCard
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.WidgetConfigurationDialog
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel

@Composable
fun WidgetCard(
    modifier: Modifier = Modifier,
    homeScreenVM: HomeScreenViewModel = viewModel(),
    widgetVM: WidgetViewModel = viewModel(),
) {
    HomeScreenCard(
        modifier = modifier,
        content = {
            IconHeader(
                iconRes = R.drawable.ic_widgets_24,
                headerRes = R.string.widget,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                PinWidgetButton(
                    onClick = {
                        widgetVM.attemptWidgetPin()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(60.dp),
                )

                Spacer(modifier = Modifier.width(32.dp))

                WidgetConfigurationDialogButton(
                    onClick = {
                        homeScreenVM.setShowWidgetConfigurationDialog(true)
                    },
                    modifier = Modifier.size(32.dp),
                )
            }
        },
    )

    if (homeScreenVM.showWidgetConfigurationDialog.collectAsStateWithLifecycle().value) {
        WidgetConfigurationDialog(
            closeDialog = {
                homeScreenVM.setShowWidgetConfigurationDialog(false)
            },
        )
    }
}

@Composable
private fun PinWidgetButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.pin),
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun WidgetConfigurationDialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.inflate_the_widget_configuration_dialog),
            modifier = modifier,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
