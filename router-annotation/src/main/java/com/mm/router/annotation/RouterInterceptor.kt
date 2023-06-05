package com.mm.router.annotation

/**
 * 路由拦截器
 * @param value 拦截器 path，用于过滤拦截器
 * @param priority 拦截器执行优先级，数值越大优先级越高
 * @param des 描述
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS
)
annotation class RouterInterceptor(
    val value: String = "", val priority: Int = 0, val des: String = ""
)