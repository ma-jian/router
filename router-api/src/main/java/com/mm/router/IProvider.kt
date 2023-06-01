package com.mm.router

import android.content.Context

/**
 * Provider interface, base of other interface.
 */
interface IProvider {
    /**
     * Do your init work in this method, it well be call when processor has been load.
     *
     * 默认在provider创建的时候执行该方法
     * @param context ctx
     */
    fun init(context: Context) {}
}