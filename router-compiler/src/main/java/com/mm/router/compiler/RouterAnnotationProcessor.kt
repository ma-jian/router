package com.mm.router.compiler

import com.google.auto.service.AutoService
import com.mm.router.annotation.RouterPath
import com.mm.router.compiler.BaseAbstractProcessor.Companion.KEY_MODULE_NAME
import com.mm.router.compiler.processor.RouterPathProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.element.TypeElement

/**
 * 路由注册处理器
 */
@AutoService(Processor::class)
@SupportedOptions(KEY_MODULE_NAME) //支持的配置参数
class RouterAnnotationProcessor : BaseAbstractProcessor() {

    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isNotEmpty()) {
            RouterPathProcessor().process(roundEnv, this)
            return true
        }
        return false
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(RouterPath::class.java.canonicalName)
    }

}