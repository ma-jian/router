package com.mm.annotation.model

import com.mm.annotation.interceptor.Interceptor

/**
 * 拦截器数据
 * @param path 路径，用于页面过滤需要使用的拦截器
 * @param priority 路由器的优先级，数字越大优先级越高
 * @param interceptor 拦截器实体
 * @param des 描述文件，用于注释
 */
class InterceptorMeta private constructor(
    val path: String, val priority: Int, val interceptor: Interceptor, val des: String
) {

    companion object {
        @JvmStatic
        fun build(path: String, priority: Int = 0, interceptor: Interceptor, des: String = ""): InterceptorMeta {
            return InterceptorMeta(path, priority, interceptor, des)
        }
    }
}