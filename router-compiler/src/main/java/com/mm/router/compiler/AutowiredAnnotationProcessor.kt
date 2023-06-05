package com.mm.router.compiler

import com.google.auto.service.AutoService
import com.mm.router.annotation.Autowired
import com.mm.router.compiler.BaseAbstractProcessor.Companion.KEY_MODULE_NAME
import com.mm.router.compiler.processor.AutowiredProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.element.TypeElement

/**
 * [Autowired]自动赋值处理器
 */
@AutoService(Processor::class) //自动生成 javax.annotation.processing.IProcessor 文件
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
        return setOf(Autowired::class.java.canonicalName)
    }

}