package com.mm.router_compiler

import com.google.auto.service.AutoService
import com.mm.annotation.RouterPath
import com.mm.router_compiler.BaseAbstractProcessor.Companion.KEY_MODULE_NAME
import com.mm.router_compiler.processor.RouterPathProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 路由注册处理器
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
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
        val types: MutableSet<String> = LinkedHashSet()
        types.add(RouterPath::class.java.canonicalName)
        return types
    }

}