package com.mm.router

import android.util.Log
import com.mm.annotation.RouterInterceptor
import com.mm.annotation.interceptor.Interceptor


/**
 * Date : 2023/5/30
 */
@RouterInterceptor("/router/path/match", priority = 1)
class PathInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain) {
        val path = chain.path()
        val interceptorClass = chain.interceptors()
        val des = chain.des()
        val meta = chain.routerMeta()
        Log.e("Router_", "meta: ${meta.type}; ${meta.path}; ${meta.destination}; ${meta.interceptors.contentToString()}; ${meta.des}")
        if (meta.path == "com.mm.second") {
            chain.interrupt()
            Router.init().open("com.mm.main").navigation() {
                chain.proceed(meta)
            }
            return
        }
        chain.proceed(meta)
    }
}