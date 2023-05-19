package com.mm.router_compiler

import com.google.auto.service.AutoService
import com.mm.annotation.Autowired
import com.mm.router_compiler.BaseAbstractProcessor.Companion.KEY_MODULE_NAME
import com.mm.router_compiler.processor.AutowiredProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * [Autowired]自动赋值处理器
 */
@AutoService(Processor::class) //自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_8) //java版本支持
@SupportedOptions(KEY_MODULE_NAME) //支持的配置参数
class AutowiredAnnotationProcessor : BaseAbstractProcessor() {

    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isNotEmpty()) {
            AutowiredProcessor().process(roundEnv, this)
            return true
        }
        return false
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types: MutableSet<String> = LinkedHashSet()
        types.add(Autowired::class.java.canonicalName)
        return types
    }

}