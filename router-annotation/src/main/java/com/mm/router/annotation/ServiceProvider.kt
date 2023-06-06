package com.mm.router.annotation

/**
 * 服务提供声明,对外提供接口
 * @param value 服务接口实现类地址
 * @param des 描述
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS
)
annotation class ServiceProvider(
    val value: String,
    val des: String = ""
)