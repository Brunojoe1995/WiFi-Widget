package com.w2sv.widget.data

import com.w2sv.common.utils.mapFlow
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.model.WidgetRefreshing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WidgetModuleWidgetRepository @Inject constructor(widgetRepository: WidgetRepository) :
    WidgetRepository by widgetRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val widgetAppearance by lazy {
        combine(
            coloringConfig,
            opacity,
            fontSize,
            propertyValueAlignment,
            bottomRowElementEnablementMap.mapFlow(),
            transform = { t1, t2, t3, t4, t5 ->
                WidgetAppearance(
                    coloringConfig = t1,
                    backgroundOpacity = t2,
                    fontSize = t3,
                    propertyValueAlignment = t4,
                    bottomBar = WidgetBottomBarElement(t5)
                )
            }
        )
            .stateInWithBlockingInitial(scope)
    }

    val refreshing by lazy {
        combine(
            refreshingParametersEnablementMap.mapFlow(),
            refreshInterval
        ) { t1, t2 -> WidgetRefreshing(t1, t2) }
            .stateInWithBlockingInitial(scope)
    }
}
