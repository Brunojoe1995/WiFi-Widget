package com.w2sv.wifiwidget.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.w2sv.wifiwidget.R

@Composable
fun ElevatedCardDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    columnModifier: Modifier = Modifier,
    header: DialogHeaderProperties? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            modifier = modifier,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = columnModifier
            ) {
                header?.let { DialogHeader(properties = it) }
                content()
            }
        }
    }
}

@Immutable
data class DialogHeaderProperties(
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
)

@Composable
private fun DialogHeader(properties: DialogHeaderProperties, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        properties.icon?.let {
            it.invoke()
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            text = properties.title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CancelApplyButtonRow(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    applyButtonEnabled: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DialogButton(onClick = onCancel) {
            Text(text = stringResource(R.string.cancel))
        }
        Spacer(modifier = Modifier.width(16.dp))
        DialogButton(onClick = onApply, enabled = applyButtonEnabled) {
            Text(text = stringResource(R.string.apply))
        }
    }
}
