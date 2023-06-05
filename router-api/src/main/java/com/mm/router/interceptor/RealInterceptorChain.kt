package com.mm.router.interceptor

import android.content.Intent
import com.mm.router.annotation.model.RouterMeta


/**
 * 拦截器接口逻辑处理器,自动执行和处理所有拦截器请求
 * @since 1.0.2
 */
class RealInterceptorChain(
    private inline val router: RouterMeta,
    private inline val interceptors: List<Interceptor>,
    private inline val index: Int,
    private inline val interceptorBuilder: InterceptorBuilder
) : Interceptor.Chain {

    internal fun copy(
        index: Int = this.index,
        router: RouterMeta = this.router,
    ) = RealInterceptorChain(router, interceptors, index, interceptorBuilder)

    override fun path(): String = router.path

    override fun proceed(meta: RouterMeta, intent: Intent) {
        if (index < interceptors.size) {
            // Call the next interceptor in the chain.
            val next = copy(index = index + 1, meta)
            val interceptor = interceptors[index]
            interceptor.intercept(next, intent)
        } else {
            interceptorBuilder.proceed?.invoke(meta)
        }
    }

    override fun interrupt() {
        interceptorBuilder.interrupt?.invoke()
    }

    override fun interceptors(): List<Interceptor> = interceptors

    override fun des(): String = router.des ?: ""

    override fun routerMeta(): RouterMeta = router
}