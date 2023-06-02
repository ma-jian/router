package com.mm.router.interceptor

import androidx.activity.result.ActivityResult
import com.mm.annotation.model.RouterMeta


/**
 * dsl builder
 */
class InterceptorBuilder {
    internal var proceed: (RouterMeta.() -> Unit)? = null
    internal var interrupt: (() -> Unit)? = null

    fun proceed(action: RouterMeta.() -> Unit) {
        this.proceed = action
    }

    fun interrupt(action: () -> Unit) {
        this.interrupt = action
    }
}


class ActivityResultBuilder {
    internal var arrivaled: ((ActivityResult) -> Unit)? = null
    internal var interrupt: (() -> Unit)? = null

    fun onArrival(action: (ActivityResult) -> Unit) {
        this.arrivaled = action
    }

    fun onInterrupt(action: () -> Unit) {
        this.interrupt = action
    }
}
