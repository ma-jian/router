package com.mm.router

import com.mm.annotation.model.RouterMeta

/**
 * 路由拦截接口，支持自定义路由的拦截跳转
 * @since 1.0
 */
interface IRouterInterceptor {

    fun intercept(interceptors: HashMap<String, RouterMeta>)
}