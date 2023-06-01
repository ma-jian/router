package com.mm.annotation

import kotlin.reflect.KClass

/**
 * 服务提供声明,对外提供接口
 * @param returnType 实现类接口，并通过当前接口获取实现类
 * @param des 描述
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS
)
annotation class ServiceProvider(
    val returnType: KClass<*>,
    val des: String = ""
)