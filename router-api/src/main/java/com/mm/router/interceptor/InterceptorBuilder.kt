package com.mm.router.interceptor

import com.mm.router.annotation.model.RouterMeta


/**
 * dsl builder
 */
class InterceptorBuilder {
    internal var proceed: (RouterMeta.() -> Unit)? = null
    internal var interrupt: ((String?, String) -> Unit)? = null

    fun proceed(action: RouterMeta.() -> Unit) {
        this.proceed = action
    }

    fun interrupt(action: (String?, String) -> Unit) {
        this.interrupt = action
    }
}