package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import android.content.Context
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.utils.enumerationTag
import com.w2sv.networking.IPAddress
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppFontText
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.InBetweenSpaced
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class WifiPropertyViewData(
    val property: WidgetWifiProperty,
    val value: WidgetWifiProperty.Value
)

@Composable
fun WifiPropertiesList(
    propertiesViewData: List<WifiPropertyViewData>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppFontText(
                    text = stringResource(id = R.string.properties),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                AppFontText(
                    text = stringResource(R.string.click_to_copy_to_clipboard),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }
        items(propertiesViewData) { viewData ->
            val propertyName = stringResource(id = viewData.property.viewData.labelRes)

            when (val value = viewData.value) {
                is WidgetWifiProperty.Value.String -> {
                    WifiPropertyRow(
                        propertyName = propertyName,
                        value = value.value,
                    )
                }

                is WidgetWifiProperty.Value.IPAddresses -> {
                    if (value.addresses.size > 1) {
                        value.addresses.forEachIndexed { i, address ->
                            WifiPropertyRow(
                                propertyName = "$propertyName ${enumerationTag(i)}",
                                value = address.hostAddressRepresentation,
                            )
                            IPSubPropertiesRow(ipAddress = address)
                        }
                    } else if (value.addresses.size == 1) {
                        val address = value.addresses.first()
                        WifiPropertyRow(
                            propertyName = propertyName,
                            value = address.hostAddressRepresentation,
                        )
                        IPSubPropertiesRow(ipAddress = address)
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
    modifier: Modifier = Modifier,
    clipboardManager: ClipboardManager = LocalClipboardManager.current,
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable {
                clipboardManager.setText(AnnotatedString(value))
                scope.launch {
                    snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                        AppSnackbarVisuals(
                            message = context.getString(R.string.copied_to_clipboard, propertyName),
                            kind = SnackbarKind.Success,
                        ),
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppFontText(
            text = propertyName,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        AppFontText(
            text = value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun IPSubPropertiesRow(ipAddress: IPAddress, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        InBetweenSpaced(
            elements = ipAddress.getViewProperties(true).toList(),
            makeElement = {
                IPSubPropertyText(text = it)
            },
            spacer = {
                Spacer(
                    modifier = Modifier.width(6.dp),
                )
            },
        )
    }
}

@Composable
private fun IPSubPropertyText(text: String) {
    AppFontText(
        text = text,
        modifier = Modifier
            .border(
                width = 1.1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(vertical = 2.dp, horizontal = 6.dp),
        fontSize = 11.sp,
    )
}
