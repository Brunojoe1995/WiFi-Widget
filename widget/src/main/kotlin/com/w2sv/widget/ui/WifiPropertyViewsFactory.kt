package com.w2sv.widget.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.subscript
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.common.utils.valueEnabledKeys
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.R
import com.w2sv.widget.data.appearance
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import slimber.log.i
import javax.inject.Inject

class WifiPropertyViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository,
    private val viewDataFactory: WidgetWifiProperty.ViewData.Factory,
) : RemoteViewsService.RemoteViewsFactory {

    override fun onCreate() {}

    private lateinit var viewData: List<WidgetWifiProperty.ViewData>
    private lateinit var widgetColors: WidgetColors

    override fun onDataSetChanged() {
        i { "${this::class.simpleName}.onDataSetChanged" }

        viewData = runBlocking {
            viewDataFactory(
                properties = widgetRepository.wifiPropertyEnablementMap.valueEnabledKeys,
                ipSubProperties = widgetRepository.ipSubPropertyEnablementMap
                    .valueEnabledKeys
                    .toSet(),
            )
                .toList()
        }
            .also { i { "Set propertyViewData=$it" } }
        widgetColors = widgetRepository.appearance.getColors(context)
    }

    override fun getCount(): Int = viewData.size

    override fun getViewAt(position: Int): RemoteViews =
        inflatePropertyLayout(
            viewData = viewData[position],
            packageName = context.packageName,
            widgetColors = widgetColors
        )

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        viewData[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}

private fun inflatePropertyLayout(
    viewData: WidgetWifiProperty.ViewData,
    packageName: String,
    widgetColors: WidgetColors
): RemoteViews =
    RemoteViews(packageName, R.layout.wifi_property)
        .apply {
            setTextView(
                viewId = R.id.property_label_tv,
                text = if (viewData is WidgetWifiProperty.ViewData.NonIP)
                    viewData.label
                else
                    buildSpannedString {
                        append(IP_LABEL)
                        subscript {
                            scale(0.8f) {
                                append(viewData.label)
                            }
                        }
                    },
                color = widgetColors.primary,
            )
            setTextView(
                viewId = R.id.property_value_tv,
                text = viewData.value,
                color = widgetColors.secondary,
            )

            (viewData as? WidgetWifiProperty.ViewData.IPProperty)?.prefixLengthText?.let { prefixLengthText ->
                setViewVisibility(R.id.prefix_length_row, View.VISIBLE)

                setTextView(
                    viewId = R.id.prefix_length_tv,
                    text = prefixLengthText,
                    color = widgetColors.secondary,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setColorStateList(
                        R.id.prefix_length_tv,
                        "setBackgroundTintList",
                        ColorStateList.valueOf(widgetColors.ipSubPropertyBackgroundColor),
                    )
                } else {
                    setBackgroundColor(
                        R.id.prefix_length_tv,
                        widgetColors.ipSubPropertyBackgroundColor
                    )
                }
            } ?: setViewVisibility(R.id.prefix_length_row, View.GONE)
        }

private const val IP_LABEL = "IP"
