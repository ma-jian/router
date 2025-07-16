package com.mm.router.interceptor

import android.content.Intent
import com.mm.router.annotation.model.RouterMeta


/**
 * 拦截器接口逻辑处理器,自动执行和处理所有拦截器请求
 * @since 1.0.2
 */
class RealInterceptorChain(
    private val router: RouterMeta,
    private val interceptors: List<Interceptor>,
    private val index: Int,
    private val interceptorBuilder: InterceptorBuilder,
) : Interceptor.Chain {

    internal fun copy(
        index: Int = this.index,
        router: RouterMeta = this.router,
    ) = RealInterceptorChain(router, interceptors, index, interceptorBuilder)

    override fun path(): String = router.path

    override fun proceed(meta: RouterMeta, intent: Intent) {
        if (index < interceptors.size) {
            // Call the next interceptor in the chain.
            val chain = copy(index = index + 1, meta)
            val nextInterceptor = interceptors[index]
            nextInterceptor.intercept(chain, intent)
        } else {
            interceptorBuilder.proceed?.invoke(meta)
        }
    }

    override fun interrupt(reason: String?) {
        val interceptor = interceptors[index - 1]
        interceptorBuilder.interrupt?.invoke(reason, interceptor::class.java.name)
    }

    override fun interceptors(): List<Interceptor> = interceptors

    override fun des(): String = router.des ?: ""

    override fun routerMeta(): RouterMeta = router
}