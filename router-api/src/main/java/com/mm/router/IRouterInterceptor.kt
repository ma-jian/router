package com.mm.router

import com.mm.annotation.model.InterceptorMeta

/**
 * @since 1.0
 * 路由拦截接口，支持自定义路由的拦截跳转，修改传递参数
 */
interface IRouterInterceptor {

    fun intercept(interceptors: HashMap<String, InterceptorMeta>)
}