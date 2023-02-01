package com.w2sv.wifiwidget.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme

@Composable
fun PropertiesConfigurationDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonEnabled: Boolean
) {
    Dialog(onDismissRequest = onCancel) {
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // gradient background
                modifier = Modifier.background(
                    Brush.linearGradient(
                        listOf(
                            colorResource(id = R.color.mischka_dark),
                            colorResource(id = R.color.mischka)
                        ),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
            ) {
                JostText(
                    text = stringResource(id = R.string.configure_properties),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(
                        top = 24.dp,
                        bottom = 12.dp,
                        start = 18.dp,
                        end = 18.dp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(
                    Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                PropertyRows()
                Row(
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ElevatedButton(onClick = onCancel) {
                        JostText(text = "Cancel")
                    }
                    ElevatedButton(onClick = onConfirm, enabled = confirmButtonEnabled) {
                        JostText(text = "Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.PropertyRows() {
    val viewModel: HomeActivity.ViewModel = viewModel()
    val context = LocalContext.current

    var infoDialogPropertyIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    infoDialogPropertyIndex?.let {
        PropertyInfoDialog(it) {
            infoDialogPropertyIndex = null
        }
    }

    StatelessPropertyColumn(
        propertyChecked = { property ->
            viewModel.widgetPropertyStates.getValue(property)
        },
        onCheckedChange = { property, newValue ->
            if (!viewModel.onChangePropertyState(property, newValue))
                context.showToast(context.getString(R.string.uncheck_all_properties_toast))
        },
        inflateInfoDialog = { propertyIndex ->
            infoDialogPropertyIndex = propertyIndex
        }
    )
}

@Composable
private fun ColumnScope.StatelessPropertyColumn(
    propertyChecked: (String) -> Boolean,
    onCheckedChange: (String, Boolean) -> Unit,
    inflateInfoDialog: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 26.dp)
            .verticalScroll(rememberScrollState())
            .weight(1f, fill = false)
    ) {
        stringArrayResource(id = R.array.wifi_properties)
            .forEachIndexed { propertyIndex, property ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    JostText(
                        text = property,
                        modifier = Modifier.weight(1f, fill = true),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp
                    )
                    Checkbox(
                        checked = propertyChecked(property),
                        onCheckedChange = { onCheckedChange(property, it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    IconButton(onClick = {
                        inflateInfoDialog(propertyIndex)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Click to toggle the property info dialog",
                            modifier = Modifier.size(
                                dimensionResource(id = R.dimen.size_icon)
                            ),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
    }
}

@Preview
@Composable
private fun Preview() {
    WifiWidgetTheme {
        Column {
            StatelessPropertyColumn({ true }, { _, _ -> }, {})
        }
    }
}