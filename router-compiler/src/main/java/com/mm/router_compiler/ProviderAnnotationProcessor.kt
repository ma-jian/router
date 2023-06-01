package com.mm.router_compiler

import com.google.auto.service.AutoService
import com.mm.annotation.ServiceProvider
import com.mm.router_compiler.BaseAbstractProcessor.Companion.KEY_MODULE_NAME
import com.mm.router_compiler.processor.ServiceProviderProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.element.TypeElement

/**
 * 自定义Provider服务处理器
 */
@AutoService(Processor::class) //自动生成 javax.annotation.processing.IProcessor 文件
@SupportedOptions(KEY_MODULE_NAME) //支持的配置参数
class ProviderAnnotationProcessor : BaseAbstractProcessor() {

    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isNotEmpty()) {
            ServiceProviderProcessor().process(roundEnv, this)
            return true
        }
        return false
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(ServiceProvider::class.java.canonicalName)
    }
}