package com.mm.router_app.interceptor

import android.content.Intent
import android.util.Log
import com.mm.router.Router
import com.mm.router.annotation.RouterInterceptor
import com.mm.router.interceptor.Interceptor


/**
 * Date : 2023/5/30
 */
@RouterInterceptor("/router/path/match", priority = 1, des = "路由拦截器")
class PathInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain, intent: Intent) {
        val path = chain.path()
        val interceptorClass = chain.interceptors()
        val des = chain.des()
        val meta = chain.routerMeta()
        Log.e(
            "Router_",
            "meta: ${meta.type}; ${meta.path}; ${meta.destination}; ${meta.interceptors.contentToString()}; ${meta.des}\n"
                    + "intent: ${intent.extras}"
        )
        if (meta.path == "com.mm.second") {
            chain.interrupt()
            Router.init().open("com.mm.main").navigation() {
                chain.proceed(meta, intent)
            }
            return
        }
        chain.proceed(meta, intent)
    }
}