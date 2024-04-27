package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.w2sv.wheelpicker.WheelPicker
import com.w2sv.wheelpicker.WheelPickerState
import com.w2sv.wheelpicker.rememberWheelPickerState
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import slimber.log.i
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun RefreshIntervalConfigurationDialog(
    intervalMinutes: Int,
    setInterval: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var duration by remember(intervalMinutes) {
        mutableStateOf(intervalMinutes.minutes)
    }
    var isInvalidSelection by rememberSaveable {
        mutableStateOf(false)
    }

    ConfigurationDialog(
        onDismissRequest = onDismissRequest,
        onApplyButtonPress = { setInterval(duration.inWholeMinutes.toInt()) },
        applyButtonEnabled = !isInvalidSelection && duration.inWholeMinutes.toInt() != intervalMinutes,
        title = stringResource(R.string.refresh_interval),
        modifier = modifier
    ) {
        val hourPickerState = rememberWheelPickerState(
            itemCount = 24,
            startIndex = duration.inWholeHours.toInt(),
            unfocusedItemCountToEitherSide = 2
        )
        val minutePickerState =
            rememberWheelPickerState(
                itemCount = 60,
                startIndex = duration.inWholeMinutes.toInt() % 60,
                unfocusedItemCountToEitherSide = 2
            )

        LaunchedEffect(hourPickerState.snappedIndex, minutePickerState.snappedIndex) {
            i { "${hourPickerState.snappedIndex} ${minutePickerState.snappedIndex}" }

            isInvalidSelection =
                hourPickerState.snappedIndex == 0 && minutePickerState.snappedIndex < 15
            if (!isInvalidSelection) {
                duration =
                    hourPickerState.snappedIndex.hours + hourPickerState.snappedIndex.minutes
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WheelPickerRow(
                hourWheelPickerState = hourPickerState,
                minuteWheelPickerState = minutePickerState,
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(visible = isInvalidSelection) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Interval shorter than 15 minutes not possible!",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun WheelPickerRow(
    hourWheelPickerState: WheelPickerState,
    minuteWheelPickerState: WheelPickerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        WheelPicker(
            state = hourWheelPickerState,
            itemSize = itemSize
        ) {
            Text(text = it.toString())
        }
        Text(text = "h")
        Spacer(modifier = Modifier.width(8.dp))
        WheelPicker(
            state = minuteWheelPickerState,
            itemSize = itemSize
        ) {
            Text(text = it.toString())
        }
        Text(text = "m")
    }
}

private val itemSize = DpSize(32.dp, 42.dp)