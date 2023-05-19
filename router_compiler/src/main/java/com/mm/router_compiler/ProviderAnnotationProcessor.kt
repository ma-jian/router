package com.mm.router_compiler

import com.google.auto.service.AutoService
import com.mm.annotation.ServiceProvider
import com.mm.router_compiler.processor.ServiceProviderProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 自定义Provider服务处理器
 */
@AutoService(Processor::class) //自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_8) //java版本支持
@SupportedOptions(BaseAbstractProcessor.KEY_MODULE_NAME) //支持的配置参数
class ProviderAnnotationProcessor : BaseAbstractProcessor() {

    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isNotEmpty()) {
            ServiceProviderProcessor().process(roundEnv, this)
            return true
        }
        return false
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types: MutableSet<String> = LinkedHashSet()
        types.add(ServiceProvider::class.java.canonicalName)
        return types
    }

}