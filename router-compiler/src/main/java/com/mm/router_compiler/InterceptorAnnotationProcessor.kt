package com.mm.router_compiler

import com.google.auto.service.AutoService
import com.mm.annotation.RouterInterceptor
import com.mm.annotation.RouterPath
import com.mm.router_compiler.BaseAbstractProcessor.Companion.KEY_MODULE_NAME
import com.mm.router_compiler.processor.InterceptorProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 拦截器
 */
@AutoService(Processor::class)
@SupportedOptions(KEY_MODULE_NAME) //支持的配置参数
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