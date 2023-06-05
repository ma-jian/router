package com.mm.router

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference
import java.util.ServiceLoader

/**
 * activity生命周期监听代理
 * @since 1.0
 */

internal class RouterActivityLifecycle private constructor(application: Application) : Application.ActivityLifecycleCallbacks {
    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    private val activityStack = ArrayDeque<Activity>()

    fun getActivityStack() = activityStack

    val activity: Activity?
        get() = activityStack.firstOrNull()

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activityStack.contains(activity)) {
            activityStack.remove(activity)
        }
        if (activity is FragmentActivity) {
            activityStack.addFirst(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        activityStack.remove(activity)
    }

    companion object {
        @JvmField
        var delegate: WeakReference<RouterActivityLifecycle> = WeakReference(null)

        /**
         * spi注册搜集路由和拦截器
         * @param application
         */
        fun register(application: Application) = RouterActivityLifecycle(application).apply {
            delegate = WeakReference(this)
            //自动注册路由器
            val loader = ServiceLoader.load(IRouterRulesCreator::class.java)
            for (aLoader in loader) Router.addRouterRule(aLoader)
            //自动注册拦截器
            val interceptors = ServiceLoader.load(IRouterInterceptor::class.java)
            for (inter in interceptors) Router.addRouterInterceptors(inter)
        }
    }
}
