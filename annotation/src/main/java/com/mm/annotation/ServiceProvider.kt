package com.mm.annotation

import kotlin.reflect.KClass

/**
 * 服务提供声明,对外提供可靠能力
 * @param path service path
 * @param returnType 实现类接口
 * @param params 构造参数类型
 * @param des 描述
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS
)
annotation class ServiceProvider(
    val returnType: KClass<*>,
    val params: Array<KClass<*>> = [],
    val des: String = ""
)