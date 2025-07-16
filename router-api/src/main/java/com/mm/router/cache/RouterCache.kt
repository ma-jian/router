package com.mm.router.cache

import android.util.LruCache
import com.mm.router.annotation.model.RouterMeta
import com.mm.router.interceptor.Interceptor
import java.util.concurrent.ConcurrentHashMap

/**
 * 路由缓存管理器
 * @since 1.1
 */
class RouterCache(private val maxSize: Int = 100) {

    // 路由元数据缓存
    private val routeMetaCache = LruCache<String, RouterMeta>(maxSize)

    // 拦截器实例缓存（单例模式）
    private val interceptorCache = ConcurrentHashMap<String, Interceptor>()

    /**
     * 缓存路由元数据
     */
    fun putRouteMeta(path: String, meta: RouterMeta) {
        if (path.isNotBlank()) {
            routeMetaCache.put(path, meta)
        }
    }

    /**
     * 获取路由元数据
     */
    fun getRouteMeta(path: String): RouterMeta? {
        return routeMetaCache.get(path)
    }

    /**
     * 缓存拦截器实例
     */
    fun putInterceptor(path: String, interceptor: Interceptor) {
        if (path.isNotBlank()) {
            interceptorCache[path] = interceptor
        }
    }

    /**
     * 获取拦截器实例
     */
    fun getInterceptor(path: String): Interceptor? {
        return interceptorCache[path]
    }

    /**
     * 预热缓存 - 批量添加常用路由
     */
    fun warmUp(routes: Map<String, RouterMeta>) {
        routes.forEach { (path, meta) ->
            putRouteMeta(path, meta)
        }
    }

    /**
     * 清除所有缓存
     */
    fun clear() {
        routeMetaCache.evictAll()
        interceptorCache.clear()
    }
}
