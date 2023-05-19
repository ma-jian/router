package com.mm.router

import android.content.Context
import android.content.Intent

/**
 * @since 1.0
 * 路由拦截接口，支持自定义路由的拦截跳转，修改传递参数
 */
interface IRouterInterceptor {
    /**
     * @param context     上下文
     * @param url         路由地址
     * @param matchIntent 匹配到的路由intent
     * @return 新的跳转intent
     */
    fun intercept(context: Context, url: String, matchIntent: Intent): Intent?
}