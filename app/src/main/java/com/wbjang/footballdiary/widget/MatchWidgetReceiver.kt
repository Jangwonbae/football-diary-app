package com.wbjang.footballdiary.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MatchWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = MatchWidget()

    @Inject lateinit var widgetScheduler: WidgetScheduler

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        widgetScheduler.scheduleUpdate()
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        widgetScheduler.scheduleUpdate()
    }
}
