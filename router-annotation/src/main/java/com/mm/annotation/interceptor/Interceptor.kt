package com.mm.annotation.interceptor

import com.mm.annotation.model.RouterMeta


/**
 * 路由拦截器接口
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

        /**
         * 继续执行下一个拦截器
         * @param meta 传递给下个拦截器的路由信息
         */
        fun proceed(meta: RouterMeta)

        /**
         * 中断路由操作
         */
        fun interrupt()

        fun interceptors(): List<Interceptor>

        fun des(): String

        fun routerMeta(): RouterMeta
    }
}