package com.mm.router.compiler

import com.google.auto.service.AutoService
import com.mm.router.annotation.RouterInterceptor
import com.mm.router.compiler.BaseAbstractProcessor.Companion.KEY_MODULE_NAME
import com.mm.router.compiler.processor.InterceptorProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.element.TypeElement

/**
 * 拦截器处理
 */
@AutoService(Processor::class)
@SupportedOptions(KEY_MODULE_NAME)
class InterceptorAnnotationProcessor : BaseAbstractProcessor() {

    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isNotEmpty()) {
            InterceptorProcessor().process(roundEnv, this)
            return true
        }
        return false
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(RouterInterceptor::class.java.canonicalName)
    }

}