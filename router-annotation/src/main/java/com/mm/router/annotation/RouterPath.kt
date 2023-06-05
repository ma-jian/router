package com.mm.router.annotation

/**
 * Router path
 * @param value path of router
 * @param interceptor 指定当前页面使用的拦截器，为空则全部应用
 * @param des description
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RouterPath(val value: String, val interceptor: Array<String> = [], val des: String = "")