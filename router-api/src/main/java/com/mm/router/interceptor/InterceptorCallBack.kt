package com.mm.router.interceptor

import com.mm.annotation.model.RouterMeta


/**
 * 拦截器接口
 */
interface InterceptorCallBack {

    fun proceed(meta: RouterMeta)

    fun interrupt()
}