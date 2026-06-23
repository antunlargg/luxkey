package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.example.R
import com.example.data.BacklightRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BacklightWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            val repository = BacklightRepository(context.applicationContext)
            val goAsyncResult = goAsync()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.toggleBacklight()
                    
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val thisWidget = ComponentName(context, BacklightWidgetProvider::class.java)
                    val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                    for (appWidgetId in allWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                } finally {
                    goAsyncResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.example.ACTION_WIDGET_TOGGLE"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val repository = BacklightRepository(context.applicationContext)
            
            CoroutineScope(Dispatchers.IO).launch {
                val config = repository.getOrInitializeConfig()
                val isDeactivated = config.isDeactivated
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                
                // If minWidth is below 130dp, treat it as a small widget and show button only
                val isSmall = minWidth > 0 && minWidth < 130
                
                val views = RemoteViews(context.packageName, R.layout.backlight_widget_layout)
                
                if (isSmall) {
                    views.setViewVisibility(R.id.widget_icon, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_text_container, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_btn_toggle, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_btn_toggle_small, android.view.View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_icon, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_text_container, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_btn_toggle, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_btn_toggle_small, android.view.View.GONE)
                }
                
                views.setTextViewText(
                    R.id.widget_status,
                    if (isDeactivated) "OFF" else "ON"
                )
                
                val toggleText = if (isDeactivated) "ENABLE" else "DISABLE"
                views.setTextViewText(R.id.widget_btn_toggle, toggleText)
                views.setTextViewText(R.id.widget_btn_toggle_small, toggleText)

                val toggleIntent = Intent(context, BacklightWidgetProvider::class.java).apply {
                    action = ACTION_TOGGLE
                }
                
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    toggleIntent,
                    flags
                )
                views.setOnClickPendingIntent(R.id.widget_btn_toggle, pendingIntent)
                views.setOnClickPendingIntent(R.id.widget_btn_toggle_small, pendingIntent)
                
                val mainIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (mainIntent != null) {
                    val mainPendingIntent = PendingIntent.getActivity(
                        context,
                        1,
                        mainIntent,
                        flags
                    )
                    views.setOnClickPendingIntent(R.id.widget_icon, mainPendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
