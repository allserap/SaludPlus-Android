package com.citas.medicas

import android.app.Activity
import android.app.Application
import android.os.Bundle

object ActivityProvider {
    var currentActivity: Activity? = null
}

class CitasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                ActivityProvider.currentActivity = activity
            }
            override fun onActivityPaused(activity: Activity) {
                if (ActivityProvider.currentActivity === activity) {
                    ActivityProvider.currentActivity = null
                }
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}