package com.mm.annotation.model

import com.mm.annotation.interceptor.Interceptor

/**
 * Date : 2023/5/30
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