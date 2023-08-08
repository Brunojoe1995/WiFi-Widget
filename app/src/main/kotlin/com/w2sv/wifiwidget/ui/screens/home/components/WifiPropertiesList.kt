package com.w2sv.wifiwidget.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.data.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.ExtendedSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import kotlinx.coroutines.launch

@Stable
data class WifiPropertyViewData(val property: WifiProperty, val value: WifiProperty.Value)

@Composable
fun WifiPropertiesList(
    propertiesViewData: List<WifiPropertyViewData>,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                JostText(
                    text = stringResource(id = R.string.properties),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                JostText(
                    text = stringResource(R.string.click_to_copy_to_clipboard),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        items(propertiesViewData) { viewData ->
            val propertyName = stringResource(id = viewData.property.viewData.labelRes)

            when (val value = viewData.value) {
                is WifiProperty.Value.Singular -> {
                    WifiPropertyRow(
                        propertyName = propertyName,
                        value = value.value,
                        snackbarHostState = snackbarHostState
                    )
                }

                is WifiProperty.Value.IPAddresses -> {
                    if (value.addresses.size > 1) {
                        value.addresses.forEachIndexed { i, address ->
                            WifiPropertyRow(
                                propertyName = "$propertyName #${i + 1}",
                                value = address.textualRepresentation,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    } else {
                        WifiPropertyRow(
                            propertyName = propertyName,
                            value = value.addresses.first().textualRepresentation,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiPropertyRow(
    propertyName: String,
    value: String,
    snackbarHostState: SnackbarHostState
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable {
                clipboardManager.setText(AnnotatedString(value))
                scope.launch {
                    snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                        ExtendedSnackbarVisuals(
                            message = "Copied $propertyName to clipboard",
                            kind = SnackbarKind.Success
                        )
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        JostText(
            text = propertyName,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        JostText(
            text = value,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}