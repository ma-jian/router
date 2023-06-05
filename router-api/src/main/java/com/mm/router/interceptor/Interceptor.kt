package com.mm.router.interceptor

import android.content.Intent
import com.mm.router.annotation.model.RouterMeta


/**
 * 路由拦截器接口
 */
fun interface Interceptor {

    /**
     * 拦截器入口
     */
    fun intercept(chain: Chain, intent: Intent)

    companion object {
        inline operator fun invoke(crossinline block: (chain: Chain, intent: Intent) -> Unit): Interceptor =
            Interceptor { chain, intent -> block(chain, intent) }
    }

    interface Chain {

        fun path(): String

        /**
         * 继续执行下一个拦截器
         * @param meta 传递给下个拦截器的路由信息
         */
        fun proceed(meta: RouterMeta, intent: Intent)

        /**
         * 中断路由操作
         */
        fun interrupt()

        fun interceptors(): List<Interceptor>

        fun des(): String

        fun routerMeta(): RouterMeta
    }
}