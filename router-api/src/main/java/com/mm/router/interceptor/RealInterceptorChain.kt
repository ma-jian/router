package com.mm.router.interceptor

import com.mm.annotation.interceptor.Interceptor
import com.mm.annotation.model.RouterMeta

/**
 * 拦截器处理器
 */
class RealInterceptorChain(
    private inline val router: RouterMeta,
    private inline val interceptors: List<Interceptor>,
    private inline val index: Int,
    private inline val proceed: RouterMeta.() -> Unit,
    private inline val interrupt: () -> Unit
) : Interceptor.Chain {

    internal fun copy(
        index: Int = this.index,
        router: RouterMeta = this.router,
    ) = RealInterceptorChain(router, interceptors, index, proceed, interrupt)

    override fun path(): String = router.path

    override fun proceed(meta: RouterMeta) {
        if (index < interceptors.size) {
            // Call the next interceptor in the chain.
            val next = copy(index = index + 1, meta)
            val interceptor = interceptors[index]
            interceptor.intercept(next)
        } else {
            proceed.invoke(meta)
        }
    }

    override fun interrupt() {
        interrupt.invoke()
    }

    override fun interceptors(): List<Interceptor> = interceptors

    override fun des(): String = router.des ?: ""

    override fun routerMeta(): RouterMeta = router
}