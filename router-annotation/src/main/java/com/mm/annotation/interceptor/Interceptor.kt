package com.mm.annotation.interceptor

import com.mm.annotation.model.RouterMeta


/**
 * 拦截器接口
 */
fun interface Interceptor {

    /**
     * 拦截器入口
     */
    fun intercept(chain: Chain)

    companion object {
        inline operator fun invoke(crossinline block: (chain: Chain) -> Unit): Interceptor = Interceptor { block(it) }
    }

    interface Chain {

        fun path(): String

        //执行下一个拦截器
        fun proceed(meta: RouterMeta)

        //中断路由
        fun interrupt()

        fun interceptors(): List<Interceptor>

        fun des(): String

        fun routerMeta(): RouterMeta
    }
}